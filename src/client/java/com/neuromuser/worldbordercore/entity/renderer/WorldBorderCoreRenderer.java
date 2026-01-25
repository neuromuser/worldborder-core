package com.neuromuser.worldbordercore.entity.renderer;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import com.neuromuser.worldbordercore.entity.model.WorldBorderCoreModel;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class WorldBorderCoreRenderer extends MobEntityRenderer<WorldBorderCoreEntity, WorldBorderCoreModel> {
    public static final EntityModelLayer MODEL_LAYER =
            new EntityModelLayer(new Identifier("worldborder-core", "worldborder_core"), "main");

    private static final Identifier TEXTURE =
            new Identifier("worldborder-core", "textures/entity/worldborder_core.png");

    public WorldBorderCoreRenderer(EntityRendererFactory.Context context) {
        super(context, new WorldBorderCoreModel(context.getPart(MODEL_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(WorldBorderCoreEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(WorldBorderCoreEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }
}