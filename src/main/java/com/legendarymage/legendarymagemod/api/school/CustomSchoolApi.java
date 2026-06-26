package com.legendarymage.legendarymagemod.api.school;

import com.legendarymage.legendarymagemod.data.CustomSchoolData;
import com.legendarymage.legendarymagemod.data.CustomSchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * 自定义法术流派 API 门面
 * 向其他 addon 提供稳定的自定义流派查询能力
 * 所有方法均委托给内部 {@link CustomSchoolRegistry} 实现
 *
 * @author Love_U
 * @version 1.0.0
 */
public final class CustomSchoolApi {

    private CustomSchoolApi() {
        // 禁止实例化
    }

    /**
     * 获取已注册的自定义流派
     *
     * @param id 流派ID
     * @return SchoolType 实例，如果不存在则返回 null
     */
    public static SchoolType getSchool(ResourceLocation id) {
        return CustomSchoolRegistry.getSchool(id);
    }

    /**
     * 获取流派数据
     *
     * @param id 流派ID
     * @return 流派数据，如果不存在则返回 null
     */
    public static CustomSchoolData getSchoolData(ResourceLocation id) {
        return CustomSchoolRegistry.getSchoolData(id);
    }

    /**
     * 获取所有已注册的自定义流派
     *
     * @return 流派ID到SchoolType的映射（返回的是内部映射的副本，可安全遍历）
     */
    public static Map<ResourceLocation, SchoolType> getAllSchools() {
        return CustomSchoolRegistry.getAllSchools();
    }

    /**
     * 检查是否已注册指定ID的流派
     *
     * @param id 流派ID
     * @return 是否已注册
     */
    public static boolean isRegistered(ResourceLocation id) {
        return CustomSchoolRegistry.isRegistered(id);
    }

    /**
     * 获取流派的属性修饰符
     *
     * @param id 流派ID
     * @return 属性修饰符，如果不存在则返回 null
     */
    public static CustomSchoolData.AttributeModifiers getAttributeModifiers(ResourceLocation id) {
        return CustomSchoolRegistry.getAttributeModifiers(id);
    }

    /**
     * 获取流派的法术统计
     *
     * @param id 流派ID
     * @return 法术统计，如果不存在则返回 null
     */
    public static CustomSchoolData.SpellStats getSpellStats(ResourceLocation id) {
        return CustomSchoolRegistry.getSpellStats(id);
    }
}
