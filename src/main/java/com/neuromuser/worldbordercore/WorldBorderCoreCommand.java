package com.neuromuser.worldbordercore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class WorldBorderCoreCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("worldbordercore")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn")
                        .executes(WorldBorderCoreCommand::spawn))
                .then(CommandManager.literal("clear")
                        .executes(WorldBorderCoreCommand::clear))
        );
    }

    private static int spawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        if (world.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.translatable("worldbordercore.command.spawn.wrong_dimension"));
            return 0;
        }

        WorldBorderCoreEntity existing = WorldBorderCoreManager.getCore(world);
        if (existing != null) {
            source.sendError(Text.translatable("worldbordercore.command.spawn.exists"));
            return 0;
        }

        WorldBorderCoreEntity core = WorldBorderCoreManager.spawnCore(world);
        if (core != null) {
            source.sendFeedback(() -> Text.translatable("worldbordercore.command.spawn.success"), true);
            return 1;
        } else {
            source.sendError(Text.translatable("worldbordercore.command.spawn.failed"));
            return 0;
        }
    }

    private static int clear(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        if (world.getRegistryKey() != World.OVERWORLD) {
            return 0;
        }

        int removed = WorldBorderCoreManager.clearAllCores(world);
        if (removed > 0) {
            source.sendFeedback(() -> Text.translatable("worldbordercore.command.clear.success", removed), true);
            return removed;
        } else {
            source.sendFeedback(() -> Text.translatable("worldbordercore.command.clear.none"), false);
            return 0;
        }
    }
}