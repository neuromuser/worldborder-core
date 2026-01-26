package com.neuromuser.worldbordercore;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class WorldBorderCoreCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("worldbordercore")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("spawn")
                        .executes(WorldBorderCoreCommand::spawn))
                .then(CommandManager.literal("clear")
                        .executes(WorldBorderCoreCommand::clear))
                .then(CommandManager.literal("count")
                        .then(CommandManager.argument("item", StringArgumentType.greedyString())
                                .executes(WorldBorderCoreCommand::countItem)))
        );
    }

    private static int spawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        if (world.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.literal("This command can only be used in the overworld."));
            return 0;
        }

        WorldBorderCoreEntity existing = WorldBorderCoreManager.getCore(world);
        if (existing != null) {
            source.sendError(Text.literal("A World Border Core already exists."));
            return 0;
        }

        WorldBorderCoreEntity core = WorldBorderCoreManager.spawnCore(world);
        if (core != null) {
            source.sendFeedback(Text.literal("Spawned World Border Core successfully!"), true);
            return 1;
        } else {
            source.sendError(Text.literal("Failed to spawn World Border Core."));
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
            source.sendFeedback(Text.literal("Removed " + removed + " entities (cores + displays)."), true);
            return removed;
        } else {
            source.sendFeedback(Text.literal("No World Border Cores to remove."), false);
            return 0;
        }
    }

    private static int countItem(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();

        if (world.getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.literal("This command can only be used in the overworld."));
            return 0;
        }

        if (!WorldScanner.isScanned()) {
            source.sendError(Text.literal("World has not been scanned yet."));
            return 0;
        }

        String itemInput = StringArgumentType.getString(context, "item");

        try {
            Identifier itemId;
            if (itemInput.contains(":")) {
                itemId = new Identifier(itemInput);
            } else {
                itemId = new Identifier("minecraft", itemInput);
            }

            Item item = Registry.ITEM.get(itemId);

            if (item == Items.AIR) {
                source.sendError(Text.literal("Invalid item ID: " + itemInput));
                return 0;
            }

            int count = WorldScanner.getCountForItem(item);

            String itemName = item.getName().getString();
            if (itemName.isEmpty()) {
                itemName = itemId.toString();
            }
            if (count > 0) {
                source.sendFeedback(Text.literal("Found " + count + " " + itemName + " in scanned area."), false);
            } else {
                source.sendFeedback(Text.literal("No " + itemName + " found in scanned area."), false);
            }

            return count;

        } catch (Exception e) {
            source.sendError(Text.literal("Invalid item ID format: " + itemInput));
            return 0;
        }
    }
}