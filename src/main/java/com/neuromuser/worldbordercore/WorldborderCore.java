package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.ConfigManager;
import com.neuromuser.worldbordercore.ConfigNetworking;
import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldborderCore implements ModInitializer {
        public static final String MOD_ID = "worldborder-core";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        @Override
        public void onInitialize() {
        ConfigManager.load(FabricLoader.getInstance().getConfigDir().resolve("worldborder-core.json"));
        ConfigNetworking.init();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ConfigNetworking.sendToClient(handler.player);
        });


        Registry.register(Registries.ENTITY_TYPE,
                        new Identifier(MOD_ID, "worldborder_core"),
                        ModEntities.WORLD_BORDER_CORE);

        FabricDefaultAttributeRegistry.register(ModEntities.WORLD_BORDER_CORE,
                        WorldBorderCoreEntity.createAttributes());

        WorldBorderCoreManager.initialize();

                LOGGER.info("World Border Core mod initialized successfully");
        }
}

