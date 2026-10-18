package me.ichun.mods.serverpause.mixin;

import me.ichun.mods.serverpause.ServerTick;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    // IntegratedServer overrides tickServer, so it needs its own hook ahead of its built-in pause handling.
    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void serverpause$onTickServer(BooleanSupplier haveTime, CallbackInfo ci) {
        if (ServerTick.onTickServer((MinecraftServer) (Object) this)) {
            ci.cancel();
        }
    }
}
