package com.legendarymage.legendarymagemod.sound;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 声音注册类
 * 负责注册模组中的所有自定义声音
 * 
 * @author Love_U
 * @version 0.0.1
 */
public class ModSounds {

    /**
     * 声音事件注册器
     */
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            BuiltInRegistries.SOUND_EVENT.key(),
            LegendaryMage.MODID
    );

    /**
     * 暴风雪施法音效
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> BLIZZARD_CAST = SOUND_EVENTS.register(
            "blizzard_cast",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "blizzard_cast")
            )
    );

    /**
     * 暴风雪环境音效（持续播放）
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> BLIZZARD_AMBIENT = SOUND_EVENTS.register(
            "blizzard_ambient",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "blizzard_ambient")
            )
    );

    /**
     * 音乐唱片 - When You Look At Me
     * 由 Vexento 创作的音乐
     */
    public static final DeferredHolder<SoundEvent, SoundEvent> WHEN_YOU_LOOK_AT_ME = SOUND_EVENTS.register(
            "when_you_look_at_me",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "when_you_look_at_me")
            )
    );

    /**
     * 注册声音到事件总线
     * 
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
