package com.legendarymage.legendarymagemod.client;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.legendarymage.legendarymagemod.data.SchoolElementMappingRegistry;
import com.legendarymage.legendarymagemod.element.ElementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 流派元素标记 Tooltip 处理器
 * <p>
 * 在物品的 Tooltip 中显示该流派的元素标记信息
 * </p>
 * 
 * @author Love_U
 * @version 1.0
 */
@EventBusSubscriber(modid = LegendaryMage.MODID, value = Dist.CLIENT)
public class SchoolElementMarkTooltipHandler {
    
    /**
     * 处理物品 Tooltip 事件
     * <p>
     * 当玩家查看物品提示时，如果物品与法术流派相关，添加元素标记信息
     * </p>
     * 
     * @param event 物品 Tooltip 事件
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        
        // 只在客户端处理
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        
        LocalPlayer player = minecraft.player;
        
        // 尝试从物品中获取流派信息
        List<ResourceLocation> schoolIds = extractSchoolIdsFromItem(stack);
        
        if (schoolIds.isEmpty()) {
            return;
        }
        
        // 为每个流派添加元素标记信息
        boolean hasElementMarks = false;
        List<Component> elementMarkLines = new ArrayList<>();
        
        for (ResourceLocation schoolId : schoolIds) {
            try {
                // 检查是否有自定义元素标记映射
                if (SchoolElementMappingRegistry.hasMapping(schoolId)) {
                    List<ElementType> elementMarks = SchoolElementMappingRegistry.getElementMarksForSchool(schoolId);
                    
                    if (!elementMarks.isEmpty()) {
                        hasElementMarks = true;
                        
                        // 构建元素标记文本
                        StringBuilder markText = new StringBuilder();
                        for (int i = 0; i < elementMarks.size(); i++) {
                            if (i > 0) {
                                markText.append(" §7|§r ");
                            }
                            markText.append(getElementMarkString(elementMarks.get(i)));
                        }
                        
                        // 添加 Tooltip 行
                        elementMarkLines.add(Component.literal("§7元素标记：§r " + markText));
                    }
                }
            } catch (Exception e) {
                LegendaryMage.LOGGER.error("处理流派元素标记 Tooltip 时出错：{}", e.getMessage());
            }
        }
        
        // 如果有元素标记信息，添加到 Tooltip
        if (hasElementMarks && !elementMarkLines.isEmpty()) {
            // 添加空行分隔
            tooltip.add(Component.empty());
            tooltip.addAll(elementMarkLines);
        }
    }
    
    /**
     * 从物品中提取流派 ID
     * 
     * @param stack 物品堆
     * @return 流派 ID 列表
     */
    private static List<ResourceLocation> extractSchoolIdsFromItem(ItemStack stack) {
        List<ResourceLocation> schoolIds = new ArrayList<>();
        
        // 检查是否是法术书
        if (stack.getItem() instanceof io.redspace.ironsspellbooks.item.SpellBook) {
            // 从法术书中提取流派
            if (io.redspace.ironsspellbooks.api.spells.ISpellContainer.isSpellContainer(stack)) {
                var spellContainer = io.redspace.ironsspellbooks.api.spells.ISpellContainer.get(stack);
                var activeSpells = spellContainer.getActiveSpells();
                
                for (var spellSlot : activeSpells) {
                    if (spellSlot != null && spellSlot.getSpell() != null) {
                        var schoolType = spellSlot.getSpell().getSchoolType();
                        if (schoolType != null) {
                            var schoolId = schoolType.getId();
                            if (schoolId != null && !schoolIds.contains(schoolId)) {
                                schoolIds.add(schoolId);
                            }
                        }
                    }
                }
            }
        }
        // 检查是否是卷轴
        else if (stack.getItem() instanceof io.redspace.ironsspellbooks.item.Scroll) {
            // 从卷轴中提取流派
            if (io.redspace.ironsspellbooks.api.spells.ISpellContainer.isSpellContainer(stack)) {
                var spellContainer = io.redspace.ironsspellbooks.api.spells.ISpellContainer.get(stack);
                var activeSpells = spellContainer.getActiveSpells();
                
                if (!activeSpells.isEmpty()) {
                    var firstSpell = activeSpells.get(0);
                    if (firstSpell != null && firstSpell.getSpell() != null) {
                        var schoolType = firstSpell.getSpell().getSchoolType();
                        if (schoolType != null) {
                            var schoolId = schoolType.getId();
                            if (schoolId != null) {
                                schoolIds.add(schoolId);
                            }
                        }
                    }
                }
            }
        }
        // 检查是否是法杖或其他魔法武器
        else {
            // 尝试通过类名匹配
            String itemName = stack.getItem().getClass().getName();
            if (itemName.contains("StaffItem") || itemName.contains("MagicSword")) {
                // 法杖和魔法剑通常也使用 SpellContainer
                if (io.redspace.ironsspellbooks.api.spells.ISpellContainer.isSpellContainer(stack)) {
                    var spellContainer = io.redspace.ironsspellbooks.api.spells.ISpellContainer.get(stack);
                    var activeSpells = spellContainer.getActiveSpells();
                    
                    for (var spellSlot : activeSpells) {
                        if (spellSlot != null && spellSlot.getSpell() != null) {
                            var schoolType = spellSlot.getSpell().getSchoolType();
                            if (schoolType != null) {
                                var schoolId = schoolType.getId();
                                if (schoolId != null && !schoolIds.contains(schoolId)) {
                                    schoolIds.add(schoolId);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return schoolIds;
    }
    
    /**
     * 获取元素标记的显示字符串
     * 
     * @param elementType 元素类型
     * @return 格式化的元素标记字符串
     */
    private static String getElementMarkString(ElementType elementType) {
        return switch (elementType) {
            case FIRE -> "§c火焰§r";
            case ICE -> "§b冰霜§r";
            case LIGHTNING -> "§e雷电§r";
            case POISON -> "§a毒素§r";
            case HOLY -> "§f神圣§r";
            case BLOOD -> "§4血色§r";
            case ELDRITCH -> "§5邪术§r";
            case ENDER -> "§5末影§r";
            default -> "§7" + elementType.name() + "§r";
        };
    }
}
