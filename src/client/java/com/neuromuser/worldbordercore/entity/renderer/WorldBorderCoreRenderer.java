package com.neuromuser.worldbordercore.entity.renderer;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import com.neuromuser.worldbordercore.entity.model.WorldBorderCoreModel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

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


        renderRequiredItem(entity, matrices, vertexConsumers, light);
    }

    private void renderRequiredItem(WorldBorderCoreEntity entity, MatrixStack matrices,
                                    VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;


        matrices.translate(0, 2.0, 0);


        matrices.multiply(this.dispatcher.getRotation());


        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f));


        float scale = 0.5f;
        matrices.scale(scale, scale, scale);


        ItemStack requiredStack = new ItemStack(entity.getRequiredItem());


        if (!requiredStack.isEmpty()) {
            matrices.push();

            matrices.translate(0, 0.3, 0);
            matrices.scale(0.5f, 0.5f, 0.5f);

            itemRenderer.renderItem(requiredStack, ModelTransformationMode.FIXED, light,
                    net.minecraft.client.render.OverlayTexture.DEFAULT_UV,
                    matrices, vertexConsumers, entity.getWorld(), entity.getId());
            matrices.pop();


            String countText = String.valueOf(entity.getRequiredCount());
            int textWidth = textRenderer.getWidth(countText);

            matrices.push();
            matrices.translate(0, -0.5, 0.01);
            matrices.scale(0.04f, 0.04f, 0.04f);

            float x = -textWidth / 2.0f;
            float y = 0;


            textRenderer.draw(countText, x + 1, y + 1, 0x000000, false,
                    matrices.peek().getPositionMatrix(), vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, light);


            textRenderer.draw(countText, x, y, 0xFFFFFF, false,
                    matrices.peek().getPositionMatrix(), vertexConsumers,
                    TextRenderer.TextLayerType.NORMAL, 0, light);
            matrices.pop();
        }

        matrices.pop();
    }
}