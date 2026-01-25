package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.List;
import java.util.UUID;

public class WorldBorderCoreManager {

    private static UUID coreEntityUuid = null;
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 100;

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(WorldBorderCoreManager::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;

        if (tickCounter >= CHECK_INTERVAL) {
            tickCounter = 0;
            ensureCoreExists(server);
        }
    }

    private static void ensureCoreExists(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) return;

        WorldBorder border = overworld.getWorldBorder();

        if (border.getSize() > 59999984) {
            hideCore(overworld);
            return;
        }

        WorldBorderCoreEntity existingCore = findExistingCore(overworld);

        if (existingCore == null) {
            spawnCore(overworld);
        } else {
            coreEntityUuid = existingCore.getUuid();
        }
    }

    private static WorldBorderCoreEntity findExistingCore(ServerWorld world) {
        WorldBorder border = world.getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();

        // Search in a large area
        net.minecraft.util.math.Box searchBox = new net.minecraft.util.math.Box(
                centerX - 100, 0, centerZ - 100,
                centerX + 100, world.getHeight(), centerZ + 100
        );

        List<WorldBorderCoreEntity> cores = world.getEntitiesByClass(
                WorldBorderCoreEntity.class,
                searchBox,
                entity -> true
        );

        if (!cores.isEmpty()) {
            if (cores.size() > 1) {
                for (int i = 1; i < cores.size(); i++) {
                    cores.get(i).discard();
                }
            }
            return cores.get(0);
        }

        if (coreEntityUuid != null) {
            net.minecraft.entity.Entity entity = world.getEntity(coreEntityUuid);
            if (entity instanceof WorldBorderCoreEntity core) {
                return core;
            }
        }

        return null;
    }

    private static void spawnCore(ServerWorld world) {
        WorldBorder border = world.getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();

        // Use MOTION_BLOCKING_NO_LEAVES
        double y = world.getTopY(
                net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
                (int)centerX,
                (int)centerZ
        ) + 1.0;

        WorldBorderCoreEntity core = ModEntities.WORLD_BORDER_CORE.create(world);
        if (core != null) {
            core.refreshPositionAndAngles(centerX, y, centerZ, 0, 0);
            core.setInvulnerable(true);

            // Initialize with random requirement
            if (world.isClient) {
                // On client, just use default - will be synced
                core.getDataTracker().set(WorldBorderCoreEntity.REQUIRED_ITEM_ID, "minecraft:diamond");
                core.getDataTracker().set(WorldBorderCoreEntity.REQUIRED_COUNT, 20);
            } else {
                // On server, set random requirement
                core.getDataTracker().set(WorldBorderCoreEntity.REQUIRED_ITEM_ID, "minecraft:diamond");
                core.getDataTracker().set(WorldBorderCoreEntity.REQUIRED_COUNT, 20);
                // The entity will roll new requirement in its initDataTracker
            }

            world.spawnEntity(core);
            coreEntityUuid = core.getUuid();

            WorldborderCore.LOGGER.info("World Border Core spawned at ({}, {}, {})", centerX, y, centerZ);
        }
    }

    private static void hideCore(ServerWorld world) {
        WorldBorderCoreEntity core = findExistingCore(world);
        if (core != null) {
            core.setInvisible(true);
            core.setInvulnerable(true);
        }
    }

    public static void reset() {
        coreEntityUuid = null;
    }
}