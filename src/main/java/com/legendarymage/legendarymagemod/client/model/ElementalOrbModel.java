package com.legendarymage.legendarymagemod.client.model;

import com.legendarymage.legendarymagemod.LegendaryMage;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * 元素弹幕模型
 * Made with Blockbench 5.1.3
 * 包含两个骨骼：bone 和 bone2，用于创建立体旋转效果
 * 
 * @author Love_U
 * @version 1.0.6
 */
public class ElementalOrbModel<T extends Entity> extends EntityModel<T> {

    /**
     * 模型层位置
     */
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "elemental_orb"), "main");

    /**
     * 骨骼2 - 外层
     */
    private final ModelPart bone2;

    /**
     * 骨骼 - 内层
     */
    private final ModelPart bone;

    /**
     * 构造函数
     * 
     * @param root 模型根部件
     */
    public ElementalOrbModel(ModelPart root) {
        this.bone2 = root.getChild("bone2");
        this.bone = root.getChild("bone");
    }

    /**
     * 创建身体层定义
     * 
     * @return 层定义
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 外层骨骼 - 较大的立方体
        PartDefinition bone2 = partdefinition.addOrReplaceChild("bone2",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(5.0F)),
                PartPose.offset(0.0F, 19.0F, 0.0F));

        // 内层骨骼 - 较小的立方体
        PartDefinition bone = partdefinition.addOrReplaceChild("bone",
                CubeListBuilder.create()
                        .texOffs(0, 4)
                        .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(3.0F)),
                PartPose.offset(0.0F, 18.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    /**
     * 设置动画
     * 
     * @param entity           实体
     * @param limbSwing        肢体摆动
     * @param limbSwingAmount  肢体摆动幅度
     * @param ageInTicks       存在时间（tick）
     * @param netHeadYaw       头部Y轴旋转
     * @param headPitch        头部俯仰
     */
    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 添加旋转动画
        // bone2 逆时针旋转
        this.bone2.yRot = ageInTicks * 0.1f;
        this.bone2.xRot = ageInTicks * 0.05f;
        
        // bone 顺时针旋转
        this.bone.yRot = -ageInTicks * 0.15f;
        this.bone.xRot = -ageInTicks * 0.08f;
    }

    /**
     * 渲染到缓冲区
     * 
     * @param poseStack      姿态栈
     * @param vertexConsumer 顶点消费者
     * @param packedLight    光照值
     * @param packedOverlay  覆盖层
     * @param color          颜色值
     */
    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, 
                               int packedLight, int packedOverlay, int color) {
        bone2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    /**
     * 获取bone2部件
     * 
     * @return bone2部件
     */
    public ModelPart getBone2() {
        return bone2;
    }

    /**
     * 获取bone部件
     * 
     * @return bone部件
     */
    public ModelPart getBone() {
        return bone;
    }
}
