package com.legendarymage.legendarymagemod;

import org.slf4j.Logger;

import com.legendarymage.legendarymagemod.effect.ModEffects;
import com.legendarymage.legendarymagemod.entity.ModEntities;
import com.legendarymage.legendarymagemod.item.ModItems;
import com.legendarymage.legendarymagemod.school.ElementAttributeRegistry;
import com.legendarymage.legendarymagemod.school.ElementSchoolRegistry;
import com.legendarymage.legendarymagemod.sound.ModSounds;
import com.legendarymage.legendarymagemod.spell.ModSpells;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.NewRegistryEvent;

/**
 * 传奇法师模组主类
 */
@Mod(LegendaryMage.MODID)
public class LegendaryMage {
    /**
     * 模组ID
     */
    public static final String MODID = "legendarymage";

    /**
     * 日志记录器
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 模组构造函数
     *
     * @param modEventBus 模组事件总线
     * @param modContainer 模组容器
     */
    public LegendaryMage(IEventBus modEventBus, ModContainer modContainer) {
        // 注册自定义注册表（必须在其他注册之前）
        modEventBus.addListener(this::onNewRegistry);

        // 注册声音
        ModSounds.register(modEventBus);

        // 注册物品
        ModItems.register(modEventBus);

        // 注册元素流派属性
        ElementAttributeRegistry.register(modEventBus);

        // 注册元素流派
        ElementSchoolRegistry.register(modEventBus);

        // 注册法术
        ModSpells.register(modEventBus);

        // 注册效果
        ModEffects.register(modEventBus);

        // 注册实体
        ModEntities.register(modEventBus);

        // 注册模组配置
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("传奇法师模组已加载");
    }

    /**
     * 新注册表事件处理
     * 用于注册自定义注册表
     *
     * @param event 新注册表事件
     */
    private void onNewRegistry(NewRegistryEvent event) {
        ElementSchoolRegistry.registerRegistry(event);
    }
}
