package com.legendarymage.legendarymagemod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模组统一日志工具类
 * 
 * 【设计目标】
 * - 统一日志格式和前缀
 * - 支持分类日志（法术、实体、拖尾、元素反应等）
 * - 提供便捷的日志级别控制
 * - 支持调试模式开关
 * 
 * 【日志分类】
 * - SPELL: 法术相关日志
 * - ENTITY: 实体相关日志
 * - TRAIL: 拖尾特效日志
 * - ELEMENT: 元素反应日志
 * - SYSTEM: 系统/通用日志
 * 
 * 【使用示例】
 * ModLogger.info(ModLogger.Category.SPELL, "法术施放成功");
 * ModLogger.debug(ModLogger.Category.TRAIL, "创建拖尾: {}", trailId);
 * ModLogger.warn(ModLogger.Category.ELEMENT, "元素反应失败");
 * ModLogger.error(ModLogger.Category.ENTITY, "实体创建异常", exception);
 * 
 * @author Love_U
 * @version 1.0.7
 */
public class ModLogger {
    
    /**
     * 日志分类枚举
     */
    public enum Category {
        /** 法术相关 */
        SPELL("法术"),
        /** 实体相关 */
        ENTITY("实体"),
        /** 拖尾特效 */
        TRAIL("拖尾"),
        /** 元素反应 */
        ELEMENT("元素"),
        /** 系统/通用 */
        SYSTEM("系统"),
        /** 配置 */
        CONFIG("配置"),
        /** 客户端 */
        CLIENT("客户端"),
        /** 数据 */
        DATA("数据");
        
        private final String displayName;
        
        Category(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * SLF4J Logger 实例
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LegendaryMage.MODID);
    
    /**
     * 检查是否处于调试模式
     * 从配置文件读取全局调试模式设置
     * 
     * @return 是否启用调试模式
     */
    public static boolean isDebugMode() {
        return Config.GLOBAL_DEBUG_MODE.get();
    }
    
    /**
     * 格式化日志消息
     * 
     * @param category 日志分类
     * @param message 原始消息
     * @return 格式化后的消息
     */
    private static String formatMessage(Category category, String message) {
        return String.format("[%s] %s", category.getDisplayName(), message);
    }
    
    // ==================== INFO 级别 ====================
    
    /**
     * 输出 INFO 级别日志
     * 
     * @param category 日志分类
     * @param message 日志消息
     */
    public static void info(Category category, String message) {
        LOGGER.info(formatMessage(category, message));
    }
    
    /**
     * 输出 INFO 级别日志（带参数）
     * 
     * @param category 日志分类
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void info(Category category, String message, Object... args) {
        LOGGER.info(formatMessage(category, message), args);
    }
    
    // ==================== DEBUG 级别 ====================
    
    /**
     * 输出 DEBUG 级别日志
     * 仅在调试模式下输出
     * 
     * @param category 日志分类
     * @param message 日志消息
     */
    public static void debug(Category category, String message) {
        if (isDebugMode()) {
            LOGGER.debug(formatMessage(category, message));
        }
    }
    
    /**
     * 输出 DEBUG 级别日志（带参数）
     * 仅在调试模式下输出
     * 
     * @param category 日志分类
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void debug(Category category, String message, Object... args) {
        if (isDebugMode()) {
            LOGGER.debug(formatMessage(category, message), args);
        }
    }
    
    /**
     * 强制输出 DEBUG 级别日志（无视调试模式）
     * 用于关键调试信息
     * 
     * @param category 日志分类
     * @param message 日志消息
     */
    public static void debugForce(Category category, String message) {
        LOGGER.debug(formatMessage(category, message));
    }
    
    /**
     * 强制输出 DEBUG 级别日志（带参数，无视调试模式）
     * 
     * @param category 日志分类
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void debugForce(Category category, String message, Object... args) {
        LOGGER.debug(formatMessage(category, message), args);
    }
    
    // ==================== WARN 级别 ====================
    
    /**
     * 输出 WARN 级别日志
     * 
     * @param category 日志分类
     * @param message 日志消息
     */
    public static void warn(Category category, String message) {
        LOGGER.warn(formatMessage(category, message));
    }
    
    /**
     * 输出 WARN 级别日志（带参数）
     * 
     * @param category 日志分类
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void warn(Category category, String message, Object... args) {
        LOGGER.warn(formatMessage(category, message), args);
    }
    
    /**
     * 输出 WARN 级别日志（带异常）
     * 
     * @param category 日志分类
     * @param message 日志消息
     * @param throwable 异常
     */
    public static void warn(Category category, String message, Throwable throwable) {
        LOGGER.warn(formatMessage(category, message), throwable);
    }
    
    // ==================== ERROR 级别 ====================
    
    /**
     * 输出 ERROR 级别日志
     * 
     * @param category 日志分类
     * @param message 日志消息
     */
    public static void error(Category category, String message) {
        LOGGER.error(formatMessage(category, message));
    }
    
    /**
     * 输出 ERROR 级别日志（带参数）
     * 
     * @param category 日志分类
     * @param message 日志消息模板
     * @param args 参数
     */
    public static void error(Category category, String message, Object... args) {
        LOGGER.error(formatMessage(category, message), args);
    }
    
    /**
     * 输出 ERROR 级别日志（带异常）
     * 
     * @param category 日志分类
     * @param message 日志消息
     * @param throwable 异常
     */
    public static void error(Category category, String message, Throwable throwable) {
        LOGGER.error(formatMessage(category, message), throwable);
    }
    
    // ==================== 便捷方法 ====================
    
    /**
     * 法术相关 INFO 日志
     */
    public static void spell(String message, Object... args) {
        info(Category.SPELL, message, args);
    }
    
    /**
     * 法术相关 DEBUG 日志
     */
    public static void spellDebug(String message, Object... args) {
        debug(Category.SPELL, message, args);
    }
    
    /**
     * 实体相关 INFO 日志
     */
    public static void entity(String message, Object... args) {
        info(Category.ENTITY, message, args);
    }
    
    /**
     * 实体相关 DEBUG 日志
     */
    public static void entityDebug(String message, Object... args) {
        debug(Category.ENTITY, message, args);
    }
    
    /**
     * 拖尾相关 INFO 日志
     */
    public static void trail(String message, Object... args) {
        info(Category.TRAIL, message, args);
    }
    
    /**
     * 拖尾相关 DEBUG 日志
     */
    public static void trailDebug(String message, Object... args) {
        debug(Category.TRAIL, message, args);
    }
    
    /**
     * 元素反应相关 INFO 日志
     */
    public static void element(String message, Object... args) {
        info(Category.ELEMENT, message, args);
    }
    
    /**
     * 元素反应相关 DEBUG 日志
     */
    public static void elementDebug(String message, Object... args) {
        debug(Category.ELEMENT, message, args);
    }
    
    /**
     * 系统相关 INFO 日志
     */
    public static void system(String message, Object... args) {
        info(Category.SYSTEM, message, args);
    }
    
    /**
     * 系统相关 DEBUG 日志
     */
    public static void systemDebug(String message, Object... args) {
        debug(Category.SYSTEM, message, args);
    }

    /**
     * 通用 WARN 日志
     */
    public static void warn(String message, Object... args) {
        warn(Category.SYSTEM, message, args);
    }

    /**
     * 通用 ERROR 日志
     */
    public static void error(String message, Object... args) {
        error(Category.SYSTEM, message, args);
    }

    /**
     * 通用 ERROR 日志（带异常）
     */
    public static void error(String message, Throwable throwable) {
        error(Category.SYSTEM, message, throwable);
    }
}
