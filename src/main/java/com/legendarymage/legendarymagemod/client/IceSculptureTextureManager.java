package com.legendarymage.legendarymagemod.client;

import com.legendarymage.legendarymagemod.LegendaryMage;

import net.minecraft.resources.ResourceLocation;

import java.util.Random;

/**
 * 冰雕纹理管理器
 * 管理活体冰雕的纹理，支持彩蛋切换
 * 
 * @author Love_U
 * @version 0.0.2
 */
public class IceSculptureTextureManager {

    /**
     * 纹理模式枚举
     */
    public enum TextureMode {
        DEFAULT,    // 默认纹理
        EASTER_EGG  // 彩蛋纹理（随机使用 ice_sculpture1.png 或 ice_sculpture2.png）
    }

    /**
     * 默认纹理
     */
    public static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/ice_sculpture.png");

    /**
     * 彩蛋纹理1
     */
    public static final ResourceLocation EASTER_EGG_TEXTURE_1 = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/ice_sculpture1.png");

    /**
     * 彩蛋纹理2
     */
    public static final ResourceLocation EASTER_EGG_TEXTURE_2 = ResourceLocation.fromNamespaceAndPath(
            LegendaryMage.MODID, "textures/entity/ice_sculpture2.png");

    /**
     * 当前纹理模式
     */
    private static TextureMode currentMode = TextureMode.DEFAULT;

    /**
     * 随机数生成器
     */
    private static final Random random = new Random();

    /**
     * 切换纹理模式
     * 顺序: DEFAULT -> EASTER_EGG -> DEFAULT
     */
    public static void cycleTextureMode() {
        switch (currentMode) {
            case DEFAULT:
                currentMode = TextureMode.EASTER_EGG;
                break;
            case EASTER_EGG:
                currentMode = TextureMode.DEFAULT;
                break;
        }
    }

    /**
     * 获取当前纹理
     * 彩蛋模式下随机选择 ice_sculpture1.png 或 ice_sculpture2.png
     * 
     * @return 当前纹理资源位置
     */
    public static ResourceLocation getCurrentTexture() {
        return switch (currentMode) {
            case DEFAULT -> DEFAULT_TEXTURE;
            case EASTER_EGG -> random.nextBoolean() ? EASTER_EGG_TEXTURE_1 : EASTER_EGG_TEXTURE_2;
        };
    }

    /**
     * 获取当前纹理模式
     * 
     * @return 当前纹理模式
     */
    public static TextureMode getCurrentMode() {
        return currentMode;
    }

    /**
     * 设置纹理模式
     * 
     * @param mode 纹理模式
     */
    public static void setTextureMode(TextureMode mode) {
        currentMode = mode;
    }

    /**
     * 重置为默认纹理
     */
    public static void resetToDefault() {
        currentMode = TextureMode.DEFAULT;
    }
}
