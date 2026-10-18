package me.ichun.mods.serverpause.mixin;

import me.ichun.mods.serverpause.client.ServerPauseClient;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    private volatile boolean pause;

    @Inject(method = "runTick",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER),
        require = 1)
    private void serverpause$onPauseCheck(boolean renderLevel, CallbackInfo ci) {
        Minecraft mc = (Minecraft) (Object) this;
        if (!pause && ServerPauseClient.serverPause && mc.getConnection() != null && mc.getConnection().getConnection().isConnected()) {
            pause = true;
        }
    }
}
