package com.legendarymage.legendarymagemod.element;

import com.legendarymage.legendarymagemod.LegendaryMage;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 元素标记效果同步处理器
 * <p>
 * 【问题背景】
 * 原版 Minecraft 只会把“效果状态”同步给效果持有者本人（ServerPlayer.onEffectAdded）
 * 以及骑乘者（sendEffectToPassengers）。普通生物（怪物）身上的效果<b>不会</b>
 * 同步给观察者客户端，导致客户端实体上的 activeEffects 始终为空，
 * 头顶元素标记图标渲染器无法检测到怪物身上的标记。
 * <p>
 * 【解决方案】
 * 在服务器端监听元素标记效果的添加/移除事件，手动向同维度所有玩家广播
 * {@link ClientboundUpdateMobEffectPacket} / {@link ClientboundRemoveMobEffectPacket}，
 * 使客户端实体也能获得元素标记效果数据，从而正常渲染头顶图标。
 *
 * @author Love_U
 * @version 1.0.0
 */
@EventBusSubscriber(modid = LegendaryMage.MODID)
public class ElementMarkSyncHandler {

    /**
     * 判断效果是否为元素标记效果
     *
     * @param instance 效果实例
     * @return 是否为元素标记
     */
    private static boolean isElementMark(MobEffectInstance instance) {
        if (instance == null) {
            return false;
        }
        MobEffect effect = instance.getEffect().value();
        for (ElementType elementType : ElementType.values()) {
            if (elementType.getMarkEffect() == effect) {
                return true;
            }
        }
        return false;
    }

    /**
     * 效果添加/升级事件
     * 服务器端向所有玩家广播效果更新包
     *
     * @param event 效果添加事件
     */
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        // 只在服务器端处理（客户端 forceAddEffect 不会触发 Added 事件，此判断为兜底）
        if (entity.level().isClientSide) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (!isElementMark(instance)) {
            return;
        }

        // 向同维度所有玩家广播效果更新包
        if (entity.level() instanceof ServerLevel serverLevel) {
            ClientboundUpdateMobEffectPacket packet = new ClientboundUpdateMobEffectPacket(entity.getId(), instance, false);
            for (ServerPlayer player : serverLevel.players()) {
                if (player.level() == serverLevel) {
                    player.connection.send(packet);
                }
            }
        }
    }

    /**
     * 效果移除事件（主动移除：命令清除、元素反应消耗、牛奶等）
     * 服务器端向所有玩家广播效果移除包
     *
     * @param event 效果移除事件
     */
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        // 只在服务器端处理
        if (entity.level().isClientSide) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (!isElementMark(instance)) {
            return;
        }

        // 向同维度所有玩家广播效果移除包
        if (entity.level() instanceof ServerLevel serverLevel) {
            ClientboundRemoveMobEffectPacket packet = new ClientboundRemoveMobEffectPacket(entity.getId(), instance.getEffect());
            broadcastRemove(serverLevel, packet);
        }
    }

    /**
     * 效果到期事件（服务器端效果自然到期）
     * 服务器端向所有玩家广播效果移除包
     *
     * @param event 效果到期事件
     */
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        // Expired 事件仅在服务器端触发，无需判断 isClientSide
        MobEffectInstance instance = event.getEffectInstance();
        if (!isElementMark(instance)) {
            return;
        }

        if (entity.level() instanceof ServerLevel serverLevel) {
            ClientboundRemoveMobEffectPacket packet = new ClientboundRemoveMobEffectPacket(entity.getId(), instance.getEffect());
            broadcastRemove(serverLevel, packet);
        }
    }

    /**
     * 向同维度所有玩家广播效果移除包
     *
     * @param serverLevel 服务器世界
     * @param packet      移除包
     */
    private static void broadcastRemove(ServerLevel serverLevel, ClientboundRemoveMobEffectPacket packet) {
        for (ServerPlayer player : serverLevel.players()) {
            if (player.level() == serverLevel) {
                player.connection.send(packet);
            }
        }
    }
}
