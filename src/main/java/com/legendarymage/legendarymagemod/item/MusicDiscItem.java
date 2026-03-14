package com.legendarymage.legendarymagemod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 自定义音乐唱片物品类
 * 支持 Shift 键显示详细信息
 *
 * @author Love_U
 * @version 0.0.1
 */
public class MusicDiscItem extends Item {

    /**
     * 构造函数
     *
     * @param properties 物品属性
     */
    public MusicDiscItem(Properties properties) {
        super(properties);
    }

    /**
     * 添加物品提示信息
     *
     * @param stack   物品堆
     * @param context 提示上下文
     * @param tooltipComponents 提示组件列表
     * @param tooltipFlag 提示标志
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // 添加描述
        tooltipComponents.add(Component.translatable("item.legendarymage.music_disc_when_you_look_at_me.desc"));

        // 检查是否按住 Shift
        if (tooltipFlag.hasShiftDown()) {
            // 显示详细信息
            tooltipComponents.add(Component.empty());
            String[] details = Component.translatable("item.legendarymage.music_disc_when_you_look_at_me.details").getString().split("\\n");
            for (String line : details) {
                tooltipComponents.add(Component.literal(line));
            }
        } else {
            // 显示提示信息
            tooltipComponents.add(Component.empty());
            tooltipComponents.add(Component.translatable("item.legendarymage.music_disc_when_you_look_at_me.info"));
        }
    }
}
