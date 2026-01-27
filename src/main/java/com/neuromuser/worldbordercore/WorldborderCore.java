package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.config.ConfigManager;
import com.neuromuser.worldbordercore.config.ConfigNetworking;
import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldborderCore implements ModInitializer {
        public static final String MOD_ID = "worldborder-core";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        private static double lastBorderSize = 0;
        private static double lastCenterX = 0;
        private static double lastCenterZ = 0;
        private static boolean needsRescan = false;
        private static int rescanDelay = 0;

    @Override
        public void onInitialize() {
                WorldScanner.initialize();

                ConfigManager.load(FabricLoader.getInstance().getConfigDir().resolve("worldborder-core.json"));

                ConfigNetworking.init();
                ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ConfigNetworking.sendToClient(handler.player));

                Registry.register(Registry.ENTITY_TYPE,
                        new Identifier(MOD_ID, "worldborder_core"),
                        ModEntities.WORLD_BORDER_CORE);

                FabricDefaultAttributeRegistry.register(ModEntities.WORLD_BORDER_CORE,
                        WorldBorderCoreEntity.createAttributes());

                WorldBorderCoreManager.initialize();
                CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> WorldBorderCoreCommand.register(dispatcher));

                ServerTickEvents.END_SERVER_TICK.register(WorldborderCore::onServerTick);

                // Restore persistent scan state if it exists
                WorldScanner.restorePersistentState();
        }

        private static void onServerTick(MinecraftServer server) {
                ServerWorld overworld = server.getWorld(World.OVERWORLD);
                if (overworld == null) return;

                WorldBorder border = overworld.getWorldBorder();
                double currentSize = border.getSize();
                double currentCenterX = border.getCenterX();
                double currentCenterZ = border.getCenterZ();

                WorldScanner.tick(overworld);

                WorldBorderCoreEntity core = WorldBorderCoreManager.getCore(overworld);

                if (core != null) {
                        boolean scanning = WorldScanner.isScanning();
                        boolean scanned = WorldScanner.isScanned();

                    boolean hasInitialScan = false;

                    if (!scanning && !scanned && !hasInitialScan) {
                                WorldborderCore.LOGGER.info("Core exists but no scan data. Starting scan...");
                                WorldScanner.startScan(overworld);
                        }
                }

                if (currentSize != lastBorderSize || currentCenterX != lastCenterX || currentCenterZ != lastCenterZ) {
                        lastBorderSize = currentSize;
                        lastCenterX = currentCenterX;
                        lastCenterZ = currentCenterZ;

                        if (WorldScanner.isScanned() && !WorldScanner.isScanning()) {
                                needsRescan = true;
                                rescanDelay = 40;
                                WorldborderCore.LOGGER.info("Border change detected. Rescan scheduled.");
                        }
                }

                if (needsRescan && rescanDelay > 0) {
                        rescanDelay--;
                        if (rescanDelay == 0) {
                                WorldScanner.startScan(overworld);
                                needsRescan = false;
                        }
                }
        }
}