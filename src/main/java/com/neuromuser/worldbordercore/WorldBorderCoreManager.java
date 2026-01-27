package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Heightmap;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.border.WorldBorder;

import java.util.Objects;
import java.util.UUID;

public class WorldBorderCoreManager {

    public static void initialize() {
    }

    public static CoreState getState(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(CoreState::fromNbt, CoreState::new, "worldborder_core");
    }
    public static WorldBorderCoreEntity spawnCore(ServerWorld world) {
        WorldBorderCoreEntity existingCore = getCore(world);
        if (existingCore != null) {
            WorldborderCore.LOGGER.info("Core already exists, not spawning new one.");
            return existingCore;
        }

        clearAllCores(world);

        WorldBorder border = world.getWorldBorder();
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        int groundY = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (int)centerX, (int)centerZ);

        WorldBorderCoreEntity core = ModEntities.WORLD_BORDER_CORE.create(world);
        if (core != null) {
            core.refreshPositionAndAngles(centerX, groundY + 1.0, centerZ, 0, 0);
            world.spawnEntity(core);

            CoreState state = getState(world);
            state.setCoreUuid(core.getUuid());
            state.markDirty();

            if (!WorldScanner.isScanning() && !WorldScanner.isScanned()) {
                WorldScanner.startScan(world);
            }

        }
        return core;
    }

    public static int clearAllCores(ServerWorld world) {
        int removed = 0;

        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof WorldBorderCoreEntity core) {
                core.discard();
                removed++;
            } else if (entity instanceof ArmorStandEntity stand) {
                if (stand.hasCustomName()) {
                    String name = Objects.requireNonNull(stand.getCustomName()).getString();
                    if (name.contains("WorldBorderCoreDisplay") ||
                            name.contains("WorldBorderCoreTextDisplay") ||
                            name.contains("WorldBorderCoreItemDisplay") ||
                            (name.contains("§e") && name.contains("x §f"))) {
                        stand.discard();
                        removed++;
                    }
                }
            }
        }

        CoreState state = getState(world);
        state.clearCoreUuid();

        WorldScanner.reset();

        if (removed > 0) {
            WorldborderCore.LOGGER.info("Removed {} entities (cores + displays)", removed);
        }

        return removed;
    }

    public static WorldBorderCoreEntity getCore(ServerWorld world) {
        CoreState state = getState(world);
        UUID savedUuid = state.getCoreUuid();

        if (savedUuid != null) {
            Entity entity = world.getEntity(savedUuid);
            if (entity instanceof WorldBorderCoreEntity core && !core.isRemoved()) {
                return core;
            }
        }
        return null;
    }


}