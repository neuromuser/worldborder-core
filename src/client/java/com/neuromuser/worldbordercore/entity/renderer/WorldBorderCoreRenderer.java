package com.neuromuser.worldbordercore.entity.renderer;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import com.neuromuser.worldbordercore.entity.model.WorldBorderCoreModel;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public class WorldBorderCoreRenderer extends MobEntityRenderer<WorldBorderCoreEntity, WorldBorderCoreModel> {
    public static final EntityModelLayer MODEL_LAYER =
            new EntityModelLayer(Identifier.of("worldborder-core", "worldborder_core"), "main");

    private static final Identifier TEXTURE =
            Identifier.of("worldborder-core", "textures/entity/worldborder_core.png");

    private final ItemRenderer itemRenderer;

    public WorldBorderCoreRenderer(EntityRendererFactory.Context context) {
        super(context, new WorldBorderCoreModel(context.getPart(MODEL_LAYER)), 0.5f);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public Identifier getTexture(WorldBorderCoreEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(WorldBorderCoreEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {

        Item requiredItem = entity.getRequiredItem();
        boolean hasRequiredItem = requiredItem != null && requiredItem != Items.AIR;

        this.model.setCoreVisible(!hasRequiredItem);

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        if (hasRequiredItem) {
            renderFloatingItem(entity, requiredItem, tickDelta, matrices, vertexConsumers, light);
        }
    }

    private void renderFloatingItem(WorldBorderCoreEntity entity, Item item, float tickDelta,
                                    MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        matrices.translate(0.0, 0.25, 0.0);

        float age = entity.age + tickDelta;

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 3.0F));

        float bobOffset = (float) Math.sin(age * 0.1F) * 0.05F;
        matrices.translate(0.0, bobOffset, 0.0);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Math.sin(age * 0.05F) * 10.0F));

        matrices.scale(1.6F, 1.6F, 1.6F);

        ItemStack stack = new ItemStack(item);
        this.itemRenderer.renderItem(
                stack,
                ModelTransformationMode.GROUND,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                entity.getWorld(),
                entity.getId()
        );

        matrices.pop();
    }
}