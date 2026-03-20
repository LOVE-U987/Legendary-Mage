package com.legendarymage.legendarymagemod.data;

import com.google.gson.JsonElement;
import com.legendarymage.legendarymagemod.LegendaryMage;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义法术流派数据包加载器
 * 从数据包中加载自定义法术流派配置
 *
 * @author Love_U
 * @version 1.0.0
 */
public class CustomSchoolDataLoader extends SimpleJsonResourceReloadListener {

    /**
     * 数据包路径
     */
    private static final String PATH = "custom_schools";

    /**
     * 加载的自定义流派数据映射
     * Key: 流派ID, Value: 流派数据
     */
    private static final Map<ResourceLocation, CustomSchoolData> LOADED_SCHOOLS = new HashMap<>();

    /**
     * 构造函数
     */
    public CustomSchoolDataLoader() {
        super(LegendaryMage.GSON, PATH);
    }

    /**
     * 应用加载的数据
     *
     * @param data      加载的JSON数据
     * @param resourceManager 资源管理器
     * @param profiler  性能分析器
     */
    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        LegendaryMage.LOGGER.info("正在加载自定义法术流派数据包...");
        LOADED_SCHOOLS.clear();

        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : data.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonElement json = entry.getValue();

            try {
                // 使用Codec解析JSON
                DataResult<CustomSchoolData> result = CustomSchoolData.CODEC.parse(JsonOps.INSTANCE, json);

                if (result.result().isPresent()) {
                    CustomSchoolData schoolData = result.result().get();
                    LOADED_SCHOOLS.put(id, schoolData);
                    successCount++;
                    LegendaryMage.LOGGER.info("成功加载自定义法术流派: {} - {}", id, schoolData.name());
                } else if (result.error().isPresent()) {
                    failCount++;
                    LegendaryMage.LOGGER.error("解析自定义法术流派失败: {} - {}", id, result.error().get().message());
                }
            } catch (Exception e) {
                failCount++;
                LegendaryMage.LOGGER.error("加载自定义法术流派时发生错误: {}", id, e);
            }
        }

        LegendaryMage.LOGGER.info("自定义法术流派加载完成: 成功 {} 个, 失败 {} 个", successCount, failCount);

        // 注册加载的自定义流派
        CustomSchoolRegistry.registerLoadedSchools();
    }

    /**
     * 获取加载的自定义流派数据
     *
     * @return 流派数据映射
     */
    public static Map<ResourceLocation, CustomSchoolData> getLoadedSchools() {
        return new HashMap<>(LOADED_SCHOOLS);
    }

    /**
     * 获取指定ID的自定义流派数据
     *
     * @param id 流派ID
     * @return 流派数据，如果不存在则返回null
     */
    public static CustomSchoolData getSchoolData(ResourceLocation id) {
        return LOADED_SCHOOLS.get(id);
    }

    /**
     * 检查是否存在指定ID的自定义流派
     *
     * @param id 流派ID
     * @return 是否存在
     */
    public static boolean hasSchool(ResourceLocation id) {
        return LOADED_SCHOOLS.containsKey(id);
    }

    /**
     * 清除所有加载的流派数据
     * 主要用于调试和重新加载
     */
    public static void clear() {
        LOADED_SCHOOLS.clear();
    }
}
