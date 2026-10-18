package me.ichun.mods.serverpause;

import com.mojang.logging.LogUtils;
import me.ichun.mods.serverpause.net.ClientPausePayload;
import me.ichun.mods.serverpause.net.ServerPausePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;

public class ServerPause implements ModInitializer {
    public static final String MOD_ID = "serverpause";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static Config config;
    public static final PauseState STATE = new PauseState();

    @Override
    public void onInitialize() {
        config = Config.load();

        PayloadTypeRegistry.serverboundPlay().register(ClientPausePayload.TYPE, ClientPausePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ServerPausePayload.TYPE, ServerPausePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ClientPausePayload.TYPE,
            (payload, context) -> STATE.updatePlayerState(context.player(), payload.paused()));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> STATE.onPlayerLogin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> STATE.onPlayerLogout(handler.getPlayer()));

        ServerLifecycleEvents.SERVER_STARTING.register(server -> STATE.reset(server, true));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> STATE.reset(server, false));

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> PauseCommand.register(dispatcher));
    }
}
