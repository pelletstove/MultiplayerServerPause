package me.ichun.mods.serverpause.client;

import me.ichun.mods.serverpause.net.ClientPausePayload;
import me.ichun.mods.serverpause.net.ServerPausePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class ServerPauseClient implements ClientModInitializer {
    /** Set by the server: it is paused, so the local game should freeze too. */
    public static volatile boolean serverPause;
    private static boolean clientPaused;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ServerPausePayload.TYPE,
            (payload, context) -> serverPause = payload.paused());
        ClientTickEvents.END_CLIENT_TICK.register(ServerPauseClient::onClientTickEnd);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
    }

    private static void reset() {
        serverPause = false;
        clientPaused = false;
    }

    private static void onClientTickEnd(Minecraft mc) {
        boolean connected = mc.getConnection() != null && mc.getConnection().getConnection().isConnected();
        boolean pausing = connected && mc.gui.isPausing();
        if (clientPaused != pausing) {
            clientPaused = pausing;
            // an unpublished integrated server already pauses itself
            if (connected && !(mc.hasSingleplayerServer() && !mc.getSingleplayerServer().isPublished())) {
                ClientPlayNetworking.send(new ClientPausePayload(clientPaused));
            }
        }
    }
}
