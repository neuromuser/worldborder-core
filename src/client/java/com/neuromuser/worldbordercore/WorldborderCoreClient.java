package com.neuromuser.worldbordercore;

import com.neuromuser.worldbordercore.entity.model.WorldBorderCoreModel;
import com.neuromuser.worldbordercore.entity.renderer.WorldBorderCoreRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class WorldborderCoreClient implements ClientModInitializer {
        @Override
        public void onInitializeClient() {
        ConfigNetworkingClient.init();
                EntityModelLayerRegistry.registerModelLayer(
                        WorldBorderCoreRenderer.MODEL_LAYER,
                        WorldBorderCoreModel::getTexturedModelData
                );
                EntityRendererRegistry.register(ModEntities.WORLD_BORDER_CORE, WorldBorderCoreRenderer::new);
        }
}
