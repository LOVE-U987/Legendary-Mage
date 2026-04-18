package com.legendarymage.legendarymagemod.item;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册类
 * 负责注册模组中的所有自定义物品
 *
 * @author Love_U
 * @version 0.0.1
 */
public class ModItems {

    /**
     * 物品注册器
     */
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            BuiltInRegistries.ITEM.key(),
            LegendaryMage.MODID
    );

    /**
     * 音乐唱片 - 会长的责任
     * 原名：When You Look At Me by Vexento
     */
    public static final DeferredHolder<Item, Item> MUSIC_DISC_WHEN_YOU_LOOK_AT_ME = ITEMS.register(
            "music_disc_when_you_look_at_me",
            () -> new MusicDiscItem(
                    new Item.Properties()
                            .stacksTo(1)
                            .rarity(Rarity.RARE)
                            .jukeboxPlayable(ResourceKey.create(
                                    Registries.JUKEBOX_SONG,
                                    ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, "when_you_look_at_me")
                            ))
            )
    );

    /**
     * 注册物品到事件总线
     *
     * @param eventBus 事件总线
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        com.legendarymage.legendarymagemod.ModLogger.spell("物品已注册");
    }
}
