package me.ichun.mods.serverpause;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class PauseCommand {
    private static final SimpleCommandExceptionType ALREADY_PAUSED = new SimpleCommandExceptionType(Component.literal("Server already paused!"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pause")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(context -> {
                PauseState state = ServerPause.STATE;
                if (state.isPaused && !state.forcePause) {
                    throw ALREADY_PAUSED.create();
                }
                context.getSource().sendSuccess(() -> Component.literal(state.forcePause ? "Unpausing server..." : "Pausing server..."), true);
                state.toggleForcePause(context.getSource().getServer());
                return Command.SINGLE_SUCCESS;
            }));
    }
}
