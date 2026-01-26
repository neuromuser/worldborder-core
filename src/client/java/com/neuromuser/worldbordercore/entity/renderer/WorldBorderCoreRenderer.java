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
            new EntityModelLayer(new Identifier("worldborder-core", "worldborder_core"), "main");

    private static final Identifier TEXTURE =
            new Identifier("worldborder-core", "textures/entity/worldborder_core.png");

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

        // Check if entity has a required item
        Item requiredItem = entity.getRequiredItem();
        boolean hasRequiredItem = requiredItem != null && requiredItem != Items.AIR;

        // Set model visibility based on whether there's a required item
        this.model.setCoreVisible(!hasRequiredItem);

        // Render the entity model (core + rings)
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        // Render the floating item if there is one
        if (hasRequiredItem) {
            renderFloatingItem(entity, requiredItem, tickDelta, matrices, vertexConsumers, light);
        }
    }

    private void renderFloatingItem(WorldBorderCoreEntity entity, Item item, float tickDelta,
                                    MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        // Position at entity center (Y=0 is center of the core)
        matrices.translate(0.0, 0.25, 0.0);

        // Calculate rotation based on age
        float age = entity.age + tickDelta;

        // Rotate the item smoothly
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(age * 3.0F));

        // Slight bobbing motion
        float bobOffset = (float) Math.sin(age * 0.1F) * 0.05F;
        matrices.translate(0.0, bobOffset, 0.0);

        // Slow pitch rotation for more dynamic movement
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees((float) Math.sin(age * 0.05F) * 10.0F));

        // Scale the item larger for better visibility
        matrices.scale(1.6F, 1.6F, 1.6F);

        // Render the item
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