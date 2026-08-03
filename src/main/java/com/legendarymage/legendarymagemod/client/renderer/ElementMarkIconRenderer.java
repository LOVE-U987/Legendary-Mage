package com.legendarymage.legendarymagemod.client.renderer;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import com.legendarymage.legendarymagemod.Config;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.element.ElementType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

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

    /**
     * 元素标记图标渲染类型（透视 + 自发光）
     * 每个图标纹理对应一个渲染类型
     */
    private static final RenderType[] ELEMENT_MARK_RENDER_TYPES = new RenderType[8];

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

        // 为每个图标创建"透视 + 自发光"渲染类型
        // 透视（NO_DEPTH_TEST）：图标无视深度测试，不会被实体模型/方块遮挡，始终完整显示
        // 自发光（entityTranslucentEmissive shader）：不受光照影响，清晰明亮
        for (int i = 0; i < ELEMENT_MARK_ICONS.length; i++) {
            ResourceLocation texture = ELEMENT_MARK_ICONS[i];
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new RenderType.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderType.NO_CULL)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .setOverlayState(RenderType.OVERLAY)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST)
                    .createCompositeState(false);
            ELEMENT_MARK_RENDER_TYPES[i] = RenderType.create(
                    "legendarymage_mark_icon_" + i,
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state
            );
        }
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
        checkAndAddMark(entity, ElementType.BLOOD, marks);
        checkAndAddMark(entity, ElementType.HOLY, marks);
        checkAndAddMark(entity, ElementType.ELDRITCH, marks);
        checkAndAddMark(entity, ElementType.POISON, marks);
        checkAndAddMark(entity, ElementType.FIRE, marks);
        checkAndAddMark(entity, ElementType.ICE, marks);
        checkAndAddMark(entity, ElementType.LIGHTNING, marks);
        checkAndAddMark(entity, ElementType.ENDER, marks);

        return marks;
    }

    /**
     * 检查并添加元素标记
     * 
     * @param entity 实体
     * @param elementType 元素类型
     * @param marks 标记列表
     */
    private static void checkAndAddMark(LivingEntity entity, ElementType elementType, List<ElementMarkInfo> marks) {
        MobEffect effect = elementType.getMarkEffect();
        if (effect == null) {
            return;
        }
        // 注意：不能直接使用 DeferredHolder 查询！
        // 客户端从服务器同步的效果，其 key 是注册表缓存的 Holder.Reference 实例；
        // 而 DeferredHolder 的 hashCode 基于 ResourceKey 内容，与 Reference（Object 默认 identity hash）不同，
        // 直接查询会导致 HashMap miss，图标无法渲染。必须通过 wrapAsHolder 获取注册表持有的同一实例。
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

        // 移动到实体头顶上方
        // 图标中心位于碰撞箱顶部上方 heightOffset 处（默认 0.5 块），
        // 由于图标半高约 0.18 块（0.36 * 0.5），图标底部恰好悬在碰撞箱上方约 5 像素（5/16 块）处。
        float entityHeight = entity.getBbHeight();
        float yOffset = entityHeight + (float) heightOffset;
        poseStack.translate(0.0, yOffset, 0.0);

        // 让图标面向相机
        poseStack.mulPose(dispatcher.cameraOrientation());

        // 应用缩放（正 Y：相机朝向坐标系中 +Y 为视觉上方，UV 按左上->右下映射即可正立）
        float iconScale = 0.03f * (float) scale;
        poseStack.scale(iconScale, iconScale, iconScale);

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
     * 使用 QUADS 模式渲染四边形（4 个顶点，顺序：左上 -> 右上 -> 右下 -> 左下）
     * 
     * @param poseStack 姿势栈
     * @param buffer 缓冲源
     * @param mark 元素标记信息
     */
    private static void renderIcon(PoseStack poseStack, MultiBufferSource buffer, ElementMarkInfo mark) {
        // 使用"透视 + 自发光"渲染类型（索引与 ElementType 枚举顺序一致）
        RenderType renderType = ELEMENT_MARK_RENDER_TYPES[mark.elementType.ordinal()];
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
        
        // 四边形（QUADS 模式，必须恰好 4 个顶点，顺序：左上 -> 右上 -> 右下 -> 左下）
        // 注意：不能用 6 顶点（两个三角形）拼接，QUADS 模式每 4 个顶点才构成一个四边形，
        // 6 顶点会被解释成 1.5 个 quad，导致图标对折/斜切！
        float size = ICON_SIZE * 10; // 放大尺寸以适应缩放
        
        // 顶点1: 左上 (-size, +size) UV(0,0)
        vertexConsumer.addVertex(matrix, -size, size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        // 顶点2: 右上 (+size, +size) UV(1,0)
        vertexConsumer.addVertex(matrix, size, size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0f, 0.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        // 顶点3: 右下 (+size, -size) UV(1,1)
        vertexConsumer.addVertex(matrix, size, -size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
        
        // 顶点4: 左下 (-size, -size) UV(0,1)
        vertexConsumer.addVertex(matrix, -size, -size, 0.0f)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0f, 1.0f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(0.0f, 0.0f, 1.0f);
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
