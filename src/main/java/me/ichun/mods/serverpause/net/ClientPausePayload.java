package me.ichun.mods.serverpause.net;

import me.ichun.mods.serverpause.ServerPause;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Client to server: this client is now (un)paused. */
public record ClientPausePayload(boolean paused) implements CustomPacketPayload {
    public static final Type<ClientPausePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ServerPause.MOD_ID, "client_pause"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, ClientPausePayload> CODEC = ByteBufCodecs.BOOL.map(ClientPausePayload::new, ClientPausePayload::paused);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
