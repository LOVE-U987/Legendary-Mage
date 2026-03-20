package com.legendarymage.legendarymagemod.data;

import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 自定义法术流派数据类
 * 用于从JSON数据包加载法术流派配置
 *
 * @author Love_U
 * @version 1.1.0
 */
public record CustomSchoolData(
        String name,                    // 流派名称（用于显示）
        int color,                      // 流派颜色（十六进制）
        Optional<String> description,   // 流派描述（可选）
        List<String> compatibleElements,// 兼容的元素类型
        Optional<AttributeModifiers> attributeModifiers, // 属性修饰符（可选）
        Optional<SpellStats> spellStats, // 法术统计（可选）
        Optional<Map<String, String>> elementMarkMapping // 流派到元素标记的映射（可选）
) {

    /**
     * Codec 用于序列化和反序列化JSON
     */
    public static final Codec<CustomSchoolData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("name").forGetter(CustomSchoolData::name),
                    Codec.INT.fieldOf("color").forGetter(CustomSchoolData::color),
                    Codec.STRING.optionalFieldOf("description").forGetter(CustomSchoolData::description),
                    Codec.STRING.listOf().fieldOf("compatible_elements").forGetter(CustomSchoolData::compatibleElements),
                    AttributeModifiers.CODEC.optionalFieldOf("attribute_modifiers").forGetter(CustomSchoolData::attributeModifiers),
                    SpellStats.CODEC.optionalFieldOf("spell_stats").forGetter(CustomSchoolData::spellStats),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("element_mark_mapping").forGetter(CustomSchoolData::elementMarkMapping)
            ).apply(instance, CustomSchoolData::new)
    );

    /**
     * 属性修饰符配置
     */
    public record AttributeModifiers(
            double spellPowerBonus,      // 法术强度加成
            double magicResistBonus,     // 魔法抗性加成
            double manaCostReduction,    // 法力消耗减免
            double castTimeReduction     // 施法时间缩减
    ) {
        public static final Codec<AttributeModifiers> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.DOUBLE.optionalFieldOf("spell_power_bonus", 0.0).forGetter(AttributeModifiers::spellPowerBonus),
                        Codec.DOUBLE.optionalFieldOf("magic_resist_bonus", 0.0).forGetter(AttributeModifiers::magicResistBonus),
                        Codec.DOUBLE.optionalFieldOf("mana_cost_reduction", 0.0).forGetter(AttributeModifiers::manaCostReduction),
                        Codec.DOUBLE.optionalFieldOf("cast_time_reduction", 0.0).forGetter(AttributeModifiers::castTimeReduction)
                ).apply(instance, AttributeModifiers::new)
        );
    }

    /**
     * 法术统计配置
     */
    public record SpellStats(
            double damageMultiplier,     // 伤害倍率
            double rangeMultiplier,      // 范围倍率
            double durationMultiplier,   // 持续时间倍率
            double cooldownReduction     // 冷却缩减
    ) {
        public static final Codec<SpellStats> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.DOUBLE.optionalFieldOf("damage_multiplier", 1.0).forGetter(SpellStats::damageMultiplier),
                        Codec.DOUBLE.optionalFieldOf("range_multiplier", 1.0).forGetter(SpellStats::rangeMultiplier),
                        Codec.DOUBLE.optionalFieldOf("duration_multiplier", 1.0).forGetter(SpellStats::durationMultiplier),
                        Codec.DOUBLE.optionalFieldOf("cooldown_reduction", 0.0).forGetter(SpellStats::cooldownReduction)
                ).apply(instance, SpellStats::new)
        );
    }

    /**
     * 获取流派的资源位置
     *
     * @param id 流派ID
     * @return 资源位置
     */
    public ResourceLocation getResourceLocation(String id) {
        return ResourceLocation.fromNamespaceAndPath(LegendaryMage.MODID, id);
    }
}
