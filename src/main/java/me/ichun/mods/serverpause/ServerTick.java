package me.ichun.mods.serverpause;

import com.mojang.logging.LogUtils;
import me.ichun.mods.serverpause.mixin.MinecraftServerAccessor;
import me.ichun.mods.serverpause.mixin.ServerGamePacketListenerImplAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

/** Called from the tickServer mixins. Returns true when the normal tick must be skipped. */
public class ServerTick {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static boolean onTickServer(MinecraftServer server) {
        PauseState state = ServerPause.STATE;
        boolean wasPaused = state.serverPaused;
        state.serverPaused = state.isPaused;

        ProfilerFiller profiler = Profiler.get();
        if (!wasPaused && state.serverPaused) {
            profiler.push("autoSave");
            LOGGER.info("Saving and pausing game...");
            server.saveEverything(false, false, false);
            profiler.pop();
        }
        if (wasPaused && !state.serverPaused) {
            server.forceGameTimeSynchronization();
        }
        if (!state.serverPaused) {
            return false;
        }

        // Tick everything except the worlds and players, so the server still answers connects and commands.
        MinecraftServerAccessor accessor = (MinecraftServerAccessor) server;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.suspendFlushing();
        }
        profiler.push("commandFunctions");
        server.getFunctions().tick();
        profiler.popPush("connection");
        accessor.serverpause$tickConnection();

        // Undo the "flying" counters so players are not kicked for floating while paused.
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerGamePacketListenerImplAccessor conn = (ServerGamePacketListenerImplAccessor) player.connection;
            if (conn.serverpause$getClientIsFloating() && !player.isSleeping() && !player.isPassenger() && !player.isDeadOrDying()) {
                conn.serverpause$setAboveGroundTickCount(conn.serverpause$getAboveGroundTickCount() - 1);
            }
            if (conn.serverpause$getClientVehicleIsFloating() && player.getRootVehicle().getControllingPassenger() == player) {
                conn.serverpause$setAboveGroundVehicleTickCount(conn.serverpause$getAboveGroundVehicleTickCount() - 1);
            }
        }

        profiler.popPush("send chunks");
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.chunkSender.sendNextChunks(player);
            player.connection.resumeFlushing();
        }
        profiler.pop();

        if (server instanceof DedicatedServer dedicated) {
            dedicated.handleConsoleInputs();
        }
        return true;
    }
}
