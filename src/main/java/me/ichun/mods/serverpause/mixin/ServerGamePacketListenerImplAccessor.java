package me.ichun.mods.serverpause.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    @Accessor("clientIsFloating")
    boolean serverpause$getClientIsFloating();

    @Accessor("aboveGroundTickCount")
    int serverpause$getAboveGroundTickCount();

    @Accessor("aboveGroundTickCount")
    void serverpause$setAboveGroundTickCount(int i);

    @Accessor("clientVehicleIsFloating")
    boolean serverpause$getClientVehicleIsFloating();

    @Accessor("aboveGroundVehicleTickCount")
    int serverpause$getAboveGroundVehicleTickCount();

    @Accessor("aboveGroundVehicleTickCount")
    void serverpause$setAboveGroundVehicleTickCount(int i);
}
