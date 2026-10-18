package me.ichun.mods.serverpause;

import me.ichun.mods.serverpause.net.ServerPausePayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PauseState {
    public final Map<UUID, Boolean> pauseState = new HashMap<>();
    public boolean isPaused; // whether the server should be paused
    public boolean serverPaused; // the server's pause state as of its last tick
    public boolean forcePause;
    public boolean wasForcePaused;

    private MinecraftServer server;

    public void reset(MinecraftServer server, boolean starting) {
        this.server = starting ? server : null;
        pauseState.clear();
        isPaused = starting && ServerPause.config.pauseWhenNoPlayers && server.isDedicatedServer(); // never pause an integrated server with no players
        serverPaused = false;
        forcePause = wasForcePaused = false;
    }

    public void onPlayerLogin(ServerPlayer player) {
        pauseState.put(player.getUUID(), false);
        checkAndUpdatePauseState();

        if (shouldChat() && ServerPause.config.sendChatMessageWhenPlayerPauseStateChanges) {
            int count = pausedCount();
            if (count > 0) {
                player.sendSystemMessage(Component.literal(count + "/" + pauseState.size() + " players paused."));
            }
        }
        if (isPaused) {
            ServerPlayNetworking.send(player, new ServerPausePayload(true));
            if (shouldChat() && (forcePause || wasForcePaused || ServerPause.config.sendChatMessageWhenPauseStateChanges)) {
                player.sendSystemMessage(Component.literal("Server paused."));
            }
        }
    }

    public void onPlayerLogout(ServerPlayer player) {
        pauseState.remove(player.getUUID());
        checkAndUpdatePauseState();
    }

    public void updatePlayerState(Player player, boolean paused) {
        Boolean was = pauseState.put(player.getUUID(), paused);
        if (server != null && shouldChat() && ServerPause.config.sendChatMessageWhenPlayerPauseStateChanges && (was == null || was != paused)) {
            String prefix = "(" + pausedCount() + "/" + pauseState.size() + ") ";
            Component msg = Component.literal(prefix + player.getGameProfile().name() + (paused ? " paused." : " unpaused."));
            server.sendSystemMessage(msg);
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                p.sendSystemMessage(msg);
            }
        }
        checkAndUpdatePauseState();
    }

    public void toggleForcePause(MinecraftServer server) {
        this.server = server;
        wasForcePaused = forcePause;
        forcePause = !forcePause;
        checkAndUpdatePauseState();
    }

    public void checkAndUpdatePauseState() {
        boolean shouldPause = ServerPause.config.pauseWhenAllPlayersPaused;
        if (pauseState.isEmpty()) {
            shouldPause = (forcePause || ServerPause.config.pauseWhenNoPlayers) && server != null && server.isDedicatedServer();
        } else if (!forcePause && ServerPause.config.pauseWhenAllPlayersPaused) {
            for (boolean b : pauseState.values()) {
                if (!b) {
                    shouldPause = false;
                    break;
                }
            }
        }

        if (isPaused != shouldPause && server != null) {
            isPaused = shouldPause;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, new ServerPausePayload(isPaused));
            }
            if (shouldChat() && (forcePause || wasForcePaused || ServerPause.config.sendChatMessageWhenPauseStateChanges)) {
                Component msg = Component.literal(isPaused ? "Server paused." : "Server unpaused.");
                server.sendSystemMessage(msg);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.sendSystemMessage(msg);
                }
            }
        }
        wasForcePaused = false;
    }

    private int pausedCount() {
        int n = 0;
        for (boolean b : pauseState.values()) {
            if (b) n++;
        }
        return n;
    }

    /** No chat spam for a lone host on an integrated server. */
    private boolean shouldChat() {
        return server != null && !(!server.isDedicatedServer() && pauseState.size() == 1);
    }
}
