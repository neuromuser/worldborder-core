package com.neuromuser.worldbordercore.entity.model;

import com.neuromuser.worldbordercore.entity.WorldBorderCoreEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

public class WorldBorderCoreModel extends EntityModel<WorldBorderCoreEntity> {
    private final ModelPart root;
    private final ModelPart core;
    private final ModelPart ring1;

    public WorldBorderCoreModel(ModelPart root) {
        this.root = root.getChild("root");
        this.core = this.root.getChild("core");
        this.ring1 = this.root.getChild("ring1");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        ModelPartData root = modelPartData.addChild("root",
                ModelPartBuilder.create(),
                ModelTransform.pivot(0.0F, 16.0F, 0.0F));

        root.addChild("core",
                ModelPartBuilder.create()
                        .uv(0, 32)
                        .cuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        root.addChild("ring1",
                ModelPartBuilder.create()
                        .uv(0, 0)
                        .cuboid(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F, new Dilation(0.0F)),
                ModelTransform.pivot(0.0F, 0.0F, 0.0F));

        return TexturedModelData.of(modelData, 64, 64);
    }

    /**
     * Control whether the core is visible.
     * When false, only the ring will be visible (for when item is displayed inside).
     */
    public void setCoreVisible(boolean visible) {
        this.core.visible = visible;
    }

    @Override
    public void setAngles(WorldBorderCoreEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.core.yaw = ageInTicks * 0.05F;
        this.core.pitch = (float) Math.sin(ageInTicks * 0.1F) * 0.1F;

        this.ring1.yaw = -ageInTicks * 0.03F;
        this.ring1.pitch = -ageInTicks * 0.03F;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay,
                       float red, float green, float blue, float alpha) {
        root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}