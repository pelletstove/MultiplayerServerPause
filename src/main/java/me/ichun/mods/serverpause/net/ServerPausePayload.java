package me.ichun.mods.serverpause.net;

import me.ichun.mods.serverpause.ServerPause;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server to client: the server is now (un)paused. */
public record ServerPausePayload(boolean paused) implements CustomPacketPayload {
    public static final Type<ServerPausePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ServerPause.MOD_ID, "server_pause"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, ServerPausePayload> CODEC = ByteBufCodecs.BOOL.map(ServerPausePayload::new, ServerPausePayload::paused);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
