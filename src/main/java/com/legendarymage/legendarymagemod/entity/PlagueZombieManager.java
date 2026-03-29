package com.legendarymage.legendarymagemod.entity;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 瘟疫僵尸管理器
 * 负责管理所有瘟疫僵尸的 tick 和清理
 * 记录瘟疫僵尸与施法者的所有权关系
 * 
 * @author Love_U
 * @version 1.0.0
 */
@EventBusSubscriber
public class PlagueZombieManager {

    /**
     * 所有活跃的瘟疫僵尸
     */
    private static final List<PlagueZombie> plagueZombies = new ArrayList<>();

    /**
     * 瘟疫僵尸 UUID 到施法者 UUID 的映射
     * 用于追踪召唤物所有权
     */
    private static final Map<UUID, UUID> plagueZombieOwners = new HashMap<>();

    /**
     * 添加瘟疫僵尸到管理器
     * 
     * @param zombie 瘟疫僵尸实例
     * @param summoner 施法者
     */
    public static synchronized void addPlagueZombie(PlagueZombie zombie, ServerPlayer summoner) {
        if (zombie != null && zombie.isAlive()) {
            plagueZombies.add(zombie);
            // 记录所有权
            if (summoner != null) {
                plagueZombieOwners.put(zombie.getSummonedZombie().getUUID(), summoner.getUUID());
            }
        }
    }

    /**
     * 获取瘟疫僵尸的所有者
     * 
     * @param zombieUUID 瘟疫僵尸 UUID
     * @return 所有者 UUID，如果没有则返回 null
     */
    public static UUID getPlagueZombieOwner(UUID zombieUUID) {
        return plagueZombieOwners.get(zombieUUID);
    }

    /**
     * 服务器 tick 事件
     * 更新所有瘟疫僵尸
     * 
     * @param event 服务器 tick 事件
     */
    @SubscribeEvent
    public static synchronized void onServerTick(ServerTickEvent.Pre event) {
        if (event.getServer() == null || event.getServer().overworld() == null) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) event.getServer().overworld();
        
        // 遍历并更新所有瘟疫僵尸
        Iterator<PlagueZombie> iterator = plagueZombies.iterator();
        while (iterator.hasNext()) {
            PlagueZombie zombie = iterator.next();
            
            // 更新僵尸
            zombie.tick();
            
            // 如果僵尸已经死亡或移除，从列表中删除并清理所有权记录
            if (!zombie.isAlive()) {
                UUID zombieUUID = zombie.getSummonedZombie().getUUID();
                plagueZombieOwners.remove(zombieUUID);
                iterator.remove();
            }
        }

        // 清理不存在的瘟疫僵尸记录（每 100tick 执行一次）
        if (event.getServer().getTickCount() % 100 == 0) {
            cleanupPlagueZombieOwners(serverLevel);
        }
    }

    /**
     * 清理不存在的瘟疫僵尸记录
     * 
     * @param level 服务器世界
     */
    private static void cleanupPlagueZombieOwners(ServerLevel level) {
        int removedCount = 0;
        Iterator<Map.Entry<UUID, UUID>> iterator = plagueZombieOwners.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, UUID> entry = iterator.next();
            var entity = level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            LegendaryMage.LOGGER.debug("[瘟疫僵尸管理器] 清理不存在的瘟疫僵尸记录：{} 条", removedCount);
        }
    }

    /**
     * 获取当前活跃的瘟疫僵尸数量（用于调试）
     * 
     * @return 瘟疫僵尸数量
     */
    public static int getActivePlagueZombieCount() {
        return plagueZombies.size();
    }
}
