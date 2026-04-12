package com.legendarymage.legendarymagemod.client.renderer;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 元素标记图标渲染器
 * 在生物头顶渲染元素标记图标
 * 
 * @author Love_U
 * @version 1.0.2
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class ElementMarkIconRenderer {

    /**
     * 图标大小
     */
    private static final float ICON_SIZE = 0.6f;

    /**
     * 图标间距
     */
    private static final float ICON_SPACING = 0.7f;

    /**
     * 元素标记图标纹理路径（使用mob_effect目录下的已有图标）
     */
    private static final ResourceLocation[] ELEMENT_MARK_ICONS = new ResourceLocation[8];

    static {
        // 初始化元素标记图标纹理（复用mob_effect目录下的图标）
        ELEMENT_MARK_ICONS[0] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/blood_mark.png");
        ELEMENT_MARK_ICONS[1] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/holy_mark.png");
        ELEMENT_MARK_ICONS[2] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/eldritch_mark.png");
        ELEMENT_MARK_ICONS[3] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/poison_mark.png");
        ELEMENT_MARK_ICONS[4] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/fire_mark.png");
        ELEMENT_MARK_ICONS[5] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/ice_mark.png");
        ELEMENT_MARK_ICONS[6] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/lightning_mark.png");
        ELEMENT_MARK_ICONS[7] = ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "textures/mob_effect/ender_mark.png");
    }

    /**
     * 渲染生物事件处理
     * 在生物渲染完成后渲染元素标记图标
     * 
     * @param event 渲染生物事件
     */
    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        // 检查是否启用元素标记图标显示
        if (!Config.ELEMENT_MARK_ICON_ENABLED.get()) {
            return;
        }

        LivingEntity entity = event.getEntity();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();

        // 获取实体身上的元素标记
        List<ElementMarkInfo> marks = getEntityElementMarks(entity);
        if (marks.isEmpty()) {
            return;
        }

        // 渲染元素标记图标
        renderElementMarkIcons(poseStack, buffer, entity, marks);
    }

    /**
     * 获取实体身上的元素标记信息
     * 
     * @param entity 实体
     * @return 元素标记信息列表
     */
    private static List<ElementMarkInfo> getEntityElementMarks(LivingEntity entity) {
        List<ElementMarkInfo> marks = new ArrayList<>();

        // 检查每种元素标记
        checkAndAddMark(entity, ElementType.BLOOD, ModEffects.BLOOD_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.HOLY, ModEffects.HOLY_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.ELDRITCH, ModEffects.ELDRITCH_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.POISON, ModEffects.POISON_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.FIRE, ModEffects.FIRE_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.ICE, ModEffects.ICE_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.LIGHTNING, ModEffects.LIGHTNING_MARK.get(), marks);
        checkAndAddMark(entity, ElementType.ENDER, ModEffects.ENDER_MARK.get(), marks);

        return marks;
    }

    /**
     * 检查并添加元素标记
     * 
     * @param entity 实体
     * @param elementType 元素类型
     * @param effect 效果
     * @param marks 标记列表
     */
    private static void checkAndAddMark(LivingEntity entity, ElementType elementType, 
                                        MobEffect effect, List<ElementMarkInfo> marks) {
        MobEffectInstance instance = entity.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
        if (instance != null) {
            int level = instance.getAmplifier() + 1; // 转换为1-3级
            marks.add(new ElementMarkInfo(elementType, level));
        }
    }

    /**
     * 渲染元素标记图标
     * 
     * @param poseStack 姿势栈
     * @param buffer 缓冲源
     * @param entity 实体
     * @param marks 元素标记列表
     */
    private static void renderElementMarkIcons(PoseStack poseStack, MultiBufferSource buffer, 
                                               LivingEntity entity, List<ElementMarkInfo> marks) {
        Minecraft minecraft = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = minecraft.getEntityRenderDispatcher();

        // 获取配置值
        double heightOffset = Config.ELEMENT_MARK_ICON_HEIGHT.get();
        double scale = Config.ELEMENT_MARK_ICON_SCALE.get();

        // 计算图标总宽度（用于居中）
        float totalWidth = (marks.size() - 1) * ICON_SPACING;
        float startX = -totalWidth / 2.0f;

        // 保存当前矩阵状态
        poseStack.pushPose();

        // 移动到实体头顶上方（增加足够的高度避免被模型遮挡）
        float entityHeight = entity.getBbHeight();
        float yOffset = entityHeight + 0.8f + (float) heightOffset;
        poseStack.translate(0.0, yOffset, 0.0);

        // 让图标面向相机
        poseStack.mulPose(dispatcher.cameraOrientation());

        // 应用缩放 - 使用负Y轴翻转，使图标正确朝向
        float iconScale = 0.03f * (float) scale;
        poseStack.scale(iconScale, -iconScale, iconScale);

        // 渲染每个图标
        for (int i = 0; i < marks.size(); i++) {
            ElementMarkInfo mark = marks.get(i);
            float x = (startX + i * ICON_SPACING) * 20; // 20是缩放后的单位转换

            // 保存当前状态
            poseStack.pushPose();
            
            // 移动到图标位置
            poseStack.translate(x, 0, 0);

            // 渲染图标
            renderIcon(poseStack, buffer, mark);

            // 恢复状态
            poseStack.popPose();
        }

        // 恢复矩阵状态
        poseStack.popPose();
    }

    /**
     * 渲染单个图标
     * 使用正确的三角形顺序渲染四边形
     * 
     * @param poseStack 姿势栈
     * @param buffer 缓冲源
     * @param mark 元素标记信息
     */
    private static void renderIcon(PoseStack poseStack, MultiBufferSource buffer, ElementMarkInfo mark) {
        ResourceLocation texture = getIconTexture(mark.elementType);
        
        // 创建渲染类型 - 使用entityCutoutNoCull确保正确渲染
        RenderType renderType = RenderType.entityCutoutNoCull(texture);
        VertexConsumer vertexConsumer = buffer.getBuffer(renderType);
        
        // 获取矩阵
        Matrix4f matrix = poseStack.last().pose();
        
        // 根据等级调整透明度（通过颜色alpha通道）
        int alpha;
        if (mark.level == 1) {
            alpha = 180; // 1级较淡
        } else if (mark.level == 2) {
            alpha = 220; // 2级中等
        } else {
            alpha = 255; // 3级最亮
        }
        
        // 渲染平面四边形 - 使用两个三角形
        // 三角形1: 左下 -> 右下 -> 左上
        // 三角形2: 右下 -> 右上 -> 左上
        float size = ICON_SIZE * 10; // 放大尺寸以适应缩放
        
        // 顶点颜色（白色+透明度）
        int color = (alpha << 24) | 0xFFFFFF;
        
        // 三角形1: 左下 -> 右下 -> 左上
        vertexConsumer.addVertex(matrix, -size, size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        vertexConsumer.addVertex(matrix, size, size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        vertexConsumer.addVertex(matrix, -size, -size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        // 三角形2: 右下 -> 右上 -> 左上
        vertexConsumer.addVertex(matrix, size, size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        vertexConsumer.addVertex(matrix, size, -size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        vertexConsumer.addVertex(matrix, -size, -size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
    }

    /**
     * 获取图标纹理
     * 
     * @param elementType 元素类型
     * @return 纹理资源位置
     */
    private static ResourceLocation getIconTexture(ElementType elementType) {
        return switch (elementType) {
            case BLOOD -> ELEMENT_MARK_ICONS[0];
            case HOLY -> ELEMENT_MARK_ICONS[1];
            case ELDRITCH -> ELEMENT_MARK_ICONS[2];
            case POISON -> ELEMENT_MARK_ICONS[3];
            case FIRE -> ELEMENT_MARK_ICONS[4];
            case ICE -> ELEMENT_MARK_ICONS[5];
            case LIGHTNING -> ELEMENT_MARK_ICONS[6];
            case ENDER -> ELEMENT_MARK_ICONS[7];
        };
    }

    /**
     * 元素标记信息类
     */
    private static class ElementMarkInfo {
        final ElementType elementType;
        final int level;

        ElementMarkInfo(ElementType elementType, int level) {
            this.elementType = elementType;
            this.level = level;
        }
    }
}
