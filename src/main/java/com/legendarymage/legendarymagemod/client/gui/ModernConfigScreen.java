package com.legendarymage.legendarymagemod.client.gui;

import com.legendarymage.legendarymagemod.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置界面
 */
public class ModernConfigScreen extends Screen {

    private static final String TRANSLATION_PREFIX = "legendarymage.modern_config";

    private final List<ConfigCategory> categories = new ArrayList<>();
    private int selectedCategory = 0;
    private final Screen parent;
    private boolean needsSave = false;

    private int panelLeft, panelTop, panelWidth, panelHeight;

    private final List<LabelInfo> labels = new ArrayList<>();
    private final List<TabButtonInfo> tabButtons = new ArrayList<>();

    private TooltipInfo activeTooltip = null;
    private float tooltipAlpha = 0.0f;

    /**
     * 配置项按钮悬停提示
     */
    private ConfigTooltipInfo activeConfigTooltip = null;
    private float configTooltipAlpha = 0.0f;

    /**
     * 配置项按钮提示列表
     */
    private final List<ConfigButtonTooltip> configButtonTooltips = new ArrayList<>();

    private int scrollOffset = 0;
    private int contentTotalHeight = 0;
    private int contentVisibleHeight = 0;

    private final int[] saveButtonArea = new int[4];
    private final int[] cancelButtonArea = new int[4];

    private int entryIndex = 0;

    /**
     * 界面打开动画进度 (0.0 - 1.0)
     */
    private float openAnimationProgress = 0.0f;

    /**
     * 内容项进入动画进度列表
     */
    private final List<Float> entryAnimationProgress = new ArrayList<>();

    /**
     * 动画开始时间
     */
    private long animationStartTime = 0;

    /**
     * 是否首次打开界面（构造函数时）
     */
    private boolean firstOpen = true;

    /**
     * 切换分类时的动画进度
     */
    private float categorySwitchProgress = 1.0f;

    /**
     * 是否正在切换分类
     */
    private boolean isSwitchingCategory = false;

    /**
     * 分类切换动画开始时间
     */
    private long categorySwitchStartTime = 0;

    public ModernConfigScreen(Screen parent) {
        super(Component.translatable(TRANSLATION_PREFIX + ".title"));
        this.parent = parent;
        initializeCategories();
        this.animationStartTime = System.currentTimeMillis();
    }

    private void initializeCategories() {
        categories.add(new ConfigCategory("spells", TRANSLATION_PREFIX + ".category.spells", TRANSLATION_PREFIX + ".category.spells.tooltip"));
        categories.add(new ConfigCategory("buffs", TRANSLATION_PREFIX + ".category.buffs", TRANSLATION_PREFIX + ".category.buffs.tooltip"));
        categories.add(new ConfigCategory("debug", TRANSLATION_PREFIX + ".category.debug", TRANSLATION_PREFIX + ".category.debug.tooltip"));
        categories.add(new ConfigCategory("element", TRANSLATION_PREFIX + ".category.element", TRANSLATION_PREFIX + ".category.element.tooltip"));
    }

    @Override
    protected void init() {
        super.init();

        this.clearWidgets();

        panelWidth = Math.min(520, this.width - 40);
        panelHeight = Math.min(380, this.height - 80);
        panelLeft = (this.width - panelWidth) / 2;
        panelTop = (this.height - panelHeight) / 2 + 10;

        labels.clear();
        tabButtons.clear();
        configButtonTooltips.clear();

        contentVisibleHeight = panelHeight - 30;

        createConfigEntries();

        saveButtonArea[0] = panelLeft + panelWidth - 160;
        saveButtonArea[1] = panelTop + panelHeight + 10;
        saveButtonArea[2] = 75;
        saveButtonArea[3] = 25;

        cancelButtonArea[0] = panelLeft + panelWidth - 80;
        cancelButtonArea[1] = panelTop + panelHeight + 10;
        cancelButtonArea[2] = 75;
        cancelButtonArea[3] = 25;
    }

    private void createConfigEntries() {
        int contentTop = panelTop + 15;
        int contentLeft = panelLeft + 15;
        int contentWidth = panelWidth - 30;
        int rowHeight = 32;

        int tabWidth = panelWidth / categories.size();
        for (int i = 0; i < categories.size(); i++) {
            ConfigCategory category = categories.get(i);
            tabButtons.add(new TabButtonInfo(
                panelLeft + i * tabWidth,
                panelTop - 28,
                tabWidth,
                28,
                category.name,
                category.description,
                i == selectedCategory
            ));
        }

        int currentY = contentTop;
        entryIndex = 0;

        switch (selectedCategory) {
            case 0 -> {
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "resurrection_rune.buff_enabled", Config.RESURRECTION_RUNE_BUFF_ENABLED);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "resurrection_rune.spell_power_multiplier", Config.RESURRECTION_RUNE_SPELL_POWER_MULTIPLIER, 0.1, 5.0);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "pyromaniac.affect_allies", Config.PYROMANIAC_AFFECT_ALLIES);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "pyromaniac.affect_summons", Config.PYROMANIAC_AFFECT_SUMMONS);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "implosion.spell_power_multiplier", Config.IMPLOSION_SPELL_POWER_MULTIPLIER, 0.1, 5.0);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "implosion.pull_strength", Config.IMPLOSION_PULL_STRENGTH, 0.1, 2.0);
                currentY = addIntRow(contentLeft, currentY, contentWidth, rowHeight, "living_ice_sculpture.max_sculptures", Config.LIVING_ICE_SCULPTURE_MAX_SCULPTURES, 1, 20);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "living_ice_sculpture.health_base", Config.LIVING_ICE_SCULPTURE_HEALTH_BASE, 10.0, 200.0);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "blizzard.base_range", Config.BLIZZARD_BASE_RANGE, 1.0, 20.0);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "blizzard.base_damage", Config.BLIZZARD_BASE_DAMAGE, 1.0, 50.0);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "element_mark_icon.enabled", Config.ELEMENT_MARK_ICON_ENABLED);
            }
            case 1 -> {
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "lightning_rod.ice_resist_reduction", Config.LIGHTNING_ROD_ICE_RESIST_REDUCTION, -0.5, 0.0);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "lightning_rod.lightning_resist_reduction", Config.LIGHTNING_ROD_LIGHTNING_RESIST_REDUCTION, -0.5, 0.0);
                currentY = addIntRow(contentLeft, currentY, contentWidth, rowHeight, "lightning_rod.duration_seconds", Config.LIGHTNING_ROD_DURATION_SECONDS, 1, 60);
                currentY = addIntRow(contentLeft, currentY, contentWidth, rowHeight, "lightning_rod.max_stacks", Config.LIGHTNING_ROD_MAX_STACKS, 1, 10);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "chaos.spell_power_bonus", Config.CHAOS_SPELL_POWER_BONUS_PER_LEVEL, 0.0, 0.5);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "plague.max_health_reduction", Config.PLAGUE_MAX_HEALTH_REDUCTION_PER_LEVEL, 0.0, 0.1);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "plague.zombie_conversion_chance", Config.PLAGUE_ZOMBIE_CONVERSION_CHANCE, 0.0, 1.0);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "electrocuted.base_damage", Config.ELECTROCUTED_BASE_DAMAGE, 1.0, 50.0);
                currentY = addIntRow(contentLeft, currentY, contentWidth, rowHeight, "electrocuted.trigger_interval", Config.ELECTROCUTED_TRIGGER_INTERVAL, 20, 200);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "ender_echo.spell_power_ratio", Config.ENDER_ECHO_SPELL_POWER_RATIO, 0.0, 1.0);
                currentY = addDoubleRow(contentLeft, currentY, contentWidth, rowHeight, "pyro_flame.damage_per_second", Config.PYRO_FLAME_DAMAGE_PER_SECOND, 0.5, 10.0);
            }
            case 2 -> {
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.global_mode", Config.GLOBAL_DEBUG_MODE);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.trail_system", Config.TRAIL_SYSTEM_DEBUG_OUTPUT);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.echo_strike", Config.ECHO_STRIKE_DEBUG_OUTPUT);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.elemental_burst", Config.ELEMENTAL_BURST_DEBUG_OUTPUT);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.element_reaction", Config.ELEMENT_REACTION_DEBUG_OUTPUT);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.elemental_barrage", Config.ELEMENTAL_BARRAGE_DEBUG_OUTPUT);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.tri_directional_arrow", Config.TRI_DIRECTIONAL_ARROW_DEBUG_OUTPUT);
                currentY = addBooleanRow(contentLeft, currentY, contentWidth, rowHeight, "debug.elemental_prism", Config.ELEMENTAL_PRISM_DEBUG_OUTPUT);
            }
            case 3 -> {
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.title");
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.description1");
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.description2");
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.elements1");
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.elements2");
                currentY += 5;
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.command");
                currentY = addInfoRow(contentLeft, currentY, TRANSLATION_PREFIX + ".element.example");
            }
        }

        contentTotalHeight = currentY - contentTop;
    }

    private int addBooleanRow(int x, int y, int width, int height, String translationKey, ModConfigSpec.BooleanValue configValue) {
        int labelWidth = width - 110;
        int drawY = y - scrollOffset;

        String label = Component.translatable(TRANSLATION_PREFIX + "." + translationKey).getString();
        String tooltip = Component.translatable(TRANSLATION_PREFIX + "." + translationKey + ".tooltip").getString();

        float entryProgress = getEntryAnimationProgress(entryIndex);
        int animOffset = (int) ((1.0f - entryProgress) * 20);
        int animAlpha = (int) (entryProgress * 255);

        if (drawY >= panelTop + 10 && drawY <= panelTop + panelHeight - 10) {
            addLabel(x, drawY + 6 - animOffset, label, animAlpha);
        }

        int widgetY = y + 2 - scrollOffset - animOffset;
        if (widgetY + 22 >= panelTop + 10 && widgetY <= panelTop + panelHeight - 10) {
            int buttonX = x + labelWidth + 10;
            int buttonY = widgetY;
            int buttonW = 100;
            int buttonH = 22;

            CycleButton<Boolean> button = CycleButton.booleanBuilder(
                    Component.translatable(TRANSLATION_PREFIX + ".value.on"),
                    Component.translatable(TRANSLATION_PREFIX + ".value.off")
                )
                .displayOnlyValue()
                .withInitialValue(configValue.get())
                .create(buttonX, buttonY, buttonW, buttonH, Component.empty(),
                    (btn, value) -> {
                        configValue.set(value);
                        needsSave = true;
                    });

            // 存储提示信息用于自定义渲染
            button.setTooltip(null);
            configButtonTooltips.add(new ConfigButtonTooltip(buttonX, buttonY, buttonW, buttonH, tooltip));

            this.addRenderableWidget(button);
        }

        entryIndex++;
        return y + height;
    }

    private int addDoubleRow(int x, int y, int width, int height, String translationKey, ModConfigSpec.DoubleValue configValue, double min, double max) {
        int labelWidth = width - 110;
        int drawY = y - scrollOffset;

        String label = Component.translatable(TRANSLATION_PREFIX + "." + translationKey).getString();

        float entryProgress = getEntryAnimationProgress(entryIndex);
        int animOffset = (int) ((1.0f - entryProgress) * 20);
        int animAlpha = (int) (entryProgress * 255);

        if (drawY >= panelTop + 10 && drawY <= panelTop + panelHeight - 10) {
            addLabel(x, drawY + 6 - animOffset, label, animAlpha);
        }

        int widgetY = y + 2 - scrollOffset - animOffset;
        if (widgetY + 22 >= panelTop + 10 && widgetY <= panelTop + panelHeight - 10) {
            EditBox editBox = new EditBox(this.font, x + labelWidth + 10, widgetY, 100, 22, Component.empty());
            editBox.setValue(String.format("%.2f", configValue.get()));
            editBox.setResponder(value -> {
                try {
                    double newValue = Double.parseDouble(value);
                    newValue = Math.max(min, Math.min(max, newValue));
                    configValue.set(newValue);
                    needsSave = true;
                } catch (NumberFormatException ignored) {
                }
            });

            this.addRenderableWidget(editBox);
        }

        entryIndex++;
        return y + height;
    }

    private int addIntRow(int x, int y, int width, int height, String translationKey, ModConfigSpec.IntValue configValue, int min, int max) {
        int labelWidth = width - 110;
        int drawY = y - scrollOffset;

        String label = Component.translatable(TRANSLATION_PREFIX + "." + translationKey).getString();

        float entryProgress = getEntryAnimationProgress(entryIndex);
        int animOffset = (int) ((1.0f - entryProgress) * 20);
        int animAlpha = (int) (entryProgress * 255);

        if (drawY >= panelTop + 10 && drawY <= panelTop + panelHeight - 10) {
            addLabel(x, drawY + 6 - animOffset, label, animAlpha);
        }

        int widgetY = y + 2 - scrollOffset - animOffset;
        if (widgetY + 22 >= panelTop + 10 && widgetY <= panelTop + panelHeight - 10) {
            EditBox editBox = new EditBox(this.font, x + labelWidth + 10, widgetY, 100, 22, Component.empty());
            editBox.setValue(String.valueOf(configValue.get()));
            editBox.setResponder(value -> {
                try {
                    int newValue = Integer.parseInt(value);
                    newValue = Math.max(min, Math.min(max, newValue));
                    configValue.set(newValue);
                    needsSave = true;
                } catch (NumberFormatException ignored) {
                }
            });

            this.addRenderableWidget(editBox);
        }

        entryIndex++;
        return y + height;
    }

    private int addLabel(int x, int y, String text) {
        labels.add(new LabelInfo(x, y, text, 255));
        return y + 20;
    }

    private void addLabel(int x, int y, String text, int alpha) {
        labels.add(new LabelInfo(x, y, text, alpha));
    }

    private int addInfoRow(int x, int y, String translationKey) {
        int drawY = y - scrollOffset;
        String text = Component.translatable(translationKey).getString();

        float entryProgress = getEntryAnimationProgress(entryIndex);
        int animOffset = (int) ((1.0f - entryProgress) * 20);
        int animAlpha = (int) (entryProgress * 255);

        if (drawY >= panelTop + 10 && drawY <= panelTop + panelHeight - 10) {
            addLabel(x, drawY - animOffset, text, animAlpha);
        }

        entryIndex++;
        return y + 25;
    }

    private float getEntryAnimationProgress(int index) {
        // 首次打开或切换分类时播放动画
        if (!firstOpen && !isSwitchingCategory) return 1.0f;

        long startTime = firstOpen ? animationStartTime : categorySwitchStartTime;
        long elapsed = System.currentTimeMillis() - startTime;
        float delay = index * 40;
        float duration = 250;
        float progress = Math.max(0.0f, Math.min(1.0f, (elapsed - delay) / duration));

        return easeOutCubic(progress);
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3);
    }

    private void selectCategory(int index) {
        if (index != selectedCategory) {
            selectedCategory = index;
            scrollOffset = 0;
            needsSave = false;
            isSwitchingCategory = true;
            categorySwitchStartTime = System.currentTimeMillis();
            categorySwitchProgress = 0.0f;
            this.init();
        }
    }

    private void saveConfig() {
        needsSave = false;

        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.sendSystemMessage(
                Component.translatable(TRANSLATION_PREFIX + ".saved"));
        }

        this.minecraft.setScreen(parent);
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // 禁用模糊背景效果
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 更新打开动画（仅首次打开）
        if (firstOpen) {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            openAnimationProgress = Math.min(1.0f, elapsed / 400.0f);
            openAnimationProgress = easeOutCubic(openAnimationProgress);

            if (openAnimationProgress >= 1.0f) {
                firstOpen = false;
            }

            // 动画期间持续重绘
            if (firstOpen) {
                this.init();
            }
        }

        // 更新分类切换动画
        if (isSwitchingCategory) {
            long elapsed = System.currentTimeMillis() - categorySwitchStartTime;
            categorySwitchProgress = Math.min(1.0f, elapsed / 300.0f);
            categorySwitchProgress = easeOutCubic(categorySwitchProgress);

            if (categorySwitchProgress >= 1.0f) {
                isSwitchingCategory = false;
            }

            // 动画期间持续重绘
            if (isSwitchingCategory) {
                this.init();
            }
        }

        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // 应用面板打开动画（仅首次）或分类切换动画
        float currentProgress = firstOpen ? openAnimationProgress : categorySwitchProgress;
        int animPanelTop = panelTop + (int) ((1.0f - currentProgress) * 20);
        int animAlpha = (int) (currentProgress * 255);

        renderPanelBackground(graphics, animPanelTop, animAlpha);

        // 标题动画（仅首次打开）
        if (firstOpen) {
            int titleY = panelTop - 50 + (int) ((1.0f - openAnimationProgress) * 20);
            int titleAlpha = (animAlpha << 24) | 0xFFFFFF;
            graphics.drawCenteredString(this.font, this.title, this.width / 2, titleY, titleAlpha);
        } else {
            graphics.drawCenteredString(this.font, this.title, this.width / 2, panelTop - 50, 0xFFFFFF);
        }

        renderTabButtons(graphics, mouseX, mouseY, animPanelTop, animAlpha);

        graphics.enableScissor(panelLeft, animPanelTop + 10, panelLeft + panelWidth, animPanelTop + panelHeight - 10);

        super.render(graphics, mouseX, mouseY, partialTick);
        renderLabels(graphics);

        graphics.disableScissor();

        renderCustomTooltip(graphics, mouseX, mouseY, partialTick);

        renderScrollbar(graphics, animPanelTop);

        renderActionButtons(graphics, mouseX, mouseY, animAlpha);

        if (needsSave) {
            graphics.drawCenteredString(this.font,
                Component.translatable(TRANSLATION_PREFIX + ".unsaved"), this.width / 2, panelTop + panelHeight + 38, 0xFFFFFF);
        }
    }

    private void renderActionButtons(GuiGraphics graphics, int mouseX, int mouseY, int alpha) {
        boolean saveHovered = mouseX >= saveButtonArea[0] && mouseX <= saveButtonArea[0] + saveButtonArea[2] &&
                             mouseY >= saveButtonArea[1] && mouseY <= saveButtonArea[1] + saveButtonArea[3];
        int saveBgColor = (alpha << 24) | (saveHovered ? 0x4CAF50 : 0x2E7D32);
        graphics.fill(saveButtonArea[0], saveButtonArea[1],
            saveButtonArea[0] + saveButtonArea[2], saveButtonArea[1] + saveButtonArea[3], saveBgColor);
        int saveTextColor = (alpha << 24) | 0xFFFFFF;
        graphics.drawCenteredString(this.font, Component.translatable(TRANSLATION_PREFIX + ".save"),
            saveButtonArea[0] + saveButtonArea[2] / 2,
            saveButtonArea[1] + (saveButtonArea[3] - 8) / 2, saveTextColor);

        boolean cancelHovered = mouseX >= cancelButtonArea[0] && mouseX <= cancelButtonArea[0] + cancelButtonArea[2] &&
                               mouseY >= cancelButtonArea[1] && mouseY <= cancelButtonArea[1] + cancelButtonArea[3];
        int cancelBgColor = (alpha << 24) | (cancelHovered ? 0xF44336 : 0xC62828);
        graphics.fill(cancelButtonArea[0], cancelButtonArea[1],
            cancelButtonArea[0] + cancelButtonArea[2], cancelButtonArea[1] + cancelButtonArea[3], cancelBgColor);
        int cancelTextColor = (alpha << 24) | 0xFFFFFF;
        graphics.drawCenteredString(this.font, Component.translatable(TRANSLATION_PREFIX + ".cancel"),
            cancelButtonArea[0] + cancelButtonArea[2] / 2,
            cancelButtonArea[1] + (cancelButtonArea[3] - 8) / 2, cancelTextColor);
    }

    private void renderPanelBackground(GuiGraphics graphics, int offsetY, int alpha) {
        int x = panelLeft;
        int y = offsetY;
        int width = panelWidth;
        int height = panelHeight;

        int shadowAlpha = (int) (alpha * 0.8);
        graphics.fill(x - 8, y - 8, x + width + 8, y + height + 8, (shadowAlpha << 24) | 0x000000);

        int borderColor = (alpha << 24) | 0x6A5ACD;
        graphics.fill(x - 8, y - 8, x + width + 8, y - 6, borderColor);
        graphics.fill(x - 8, y + height + 6, x + width + 8, y + height + 8, borderColor);
        graphics.fill(x - 8, y - 8, x - 6, y + height + 8, borderColor);
        graphics.fill(x + width + 6, y - 8, x + width + 8, y + height + 8, borderColor);

        int bgAlpha = (int) (alpha * 0.87);
        graphics.fill(x, y, x + width, y + height, (bgAlpha << 24) | 0x1a1a2e);

        graphics.fill(x, y, x + width, y + 1, borderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor);
        graphics.fill(x, y, x + 1, y + height, borderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor);

        for (int i = 0; i < 15; i++) {
            int gradientAlpha = (int) ((80 * (1.0f - i / 15.0f)) * (alpha / 255.0f));
            int color = (gradientAlpha << 24) | 0x6A5ACD;
            graphics.fill(x, y + i, x + width, y + i + 1, color);
        }
    }

    private void renderTabButtons(GuiGraphics graphics, int mouseX, int mouseY, int offsetY, int alpha) {
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButtonInfo tab = tabButtons.get(i);
            int tabY = tab.y - (panelTop - offsetY);

            if (tab.selected) {
                graphics.fill(tab.x, tabY, tab.x + tab.width, tabY + tab.height, (alpha << 24) | 0x6A5ACD);
                graphics.fill(tab.x, tabY + tab.height - 2, tab.x + tab.width, tabY + tab.height, (alpha << 24) | 0xFFFFFF);
            } else {
                boolean hovered = mouseX >= tab.x && mouseX <= tab.x + tab.width &&
                                 mouseY >= tabY && mouseY <= tabY + tab.height;
                int bgColor = (alpha << 24) | (hovered ? 0x4a4a6e : 0x2a2a4e);
                graphics.fill(tab.x, tabY, tab.x + tab.width, tabY + tab.height, bgColor);
            }

            int textColor = tab.selected ? ((alpha << 24) | 0xFFFFFF) : ((alpha << 24) | 0xAAAAAA);
            graphics.drawCenteredString(this.font, Component.translatable(tab.text),
                tab.x + tab.width / 2, tabY + (tab.height - 8) / 2, textColor);
        }
    }

    private void renderLabels(GuiGraphics graphics) {
        for (LabelInfo label : labels) {
            int color = (label.alpha << 24) | 0xFFFFFF;
            graphics.drawString(this.font, Component.translatable(label.text), label.x, label.y, color);
        }
    }

    private void renderScrollbar(GuiGraphics graphics, int offsetY) {
        if (contentTotalHeight <= contentVisibleHeight) {
            return;
        }

        int scrollbarX = panelLeft + panelWidth - 8;
        int scrollbarY = offsetY + 15;
        int scrollbarHeight = contentVisibleHeight - 10;

        graphics.fill(scrollbarX, scrollbarY, scrollbarX + 4, scrollbarY + scrollbarHeight, 0x44FFFFFF);

        float scrollRatio = (float) scrollOffset / (contentTotalHeight - contentVisibleHeight);
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * ((float) contentVisibleHeight / contentTotalHeight)));
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);

        graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, 0xCCFFFFFF);
    }

    private void renderCustomTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 渲染分类标签提示
        TooltipInfo hoveredTooltip = null;

        for (TabButtonInfo tab : tabButtons) {
            if (mouseX >= tab.x && mouseX <= tab.x + tab.width &&
                mouseY >= tab.y && mouseY <= tab.y + tab.height) {
                hoveredTooltip = new TooltipInfo(Component.translatable(tab.tooltip).getString(), mouseX, mouseY + 20);
                break;
            }
        }

        if (hoveredTooltip != null) {
            tooltipAlpha = Math.min(1.0f, tooltipAlpha + partialTick * 0.2f);
            activeTooltip = hoveredTooltip;
        } else {
            tooltipAlpha = Math.max(0.0f, tooltipAlpha - partialTick * 0.3f);
            if (tooltipAlpha <= 0.0f) {
                activeTooltip = null;
            }
        }

        if (activeTooltip != null && tooltipAlpha > 0.01f) {
            int alpha = (int) (tooltipAlpha * 255);
            int bgColor = (alpha << 24) | 0x1a1a2e;
            int borderColor = (alpha << 24) | 0x6A5ACD;

            String text = activeTooltip.text;
            int textWidth = this.font.width(text);
            int tooltipX = activeTooltip.x;
            int tooltipY = activeTooltip.y;
            int tooltipWidth = textWidth + 10;
            int tooltipHeight = 20;

            if (tooltipX + tooltipWidth > this.width) {
                tooltipX = this.width - tooltipWidth - 5;
            }
            if (tooltipY + tooltipHeight > this.height) {
                tooltipY = activeTooltip.y - 30;
            }

            graphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, bgColor);
            graphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + 1, borderColor);
            graphics.fill(tooltipX, tooltipY + tooltipHeight - 1, tooltipX + tooltipWidth, tooltipY + tooltipHeight, borderColor);
            graphics.fill(tooltipX, tooltipY, tooltipX + 1, tooltipY + tooltipHeight, borderColor);
            graphics.fill(tooltipX + tooltipWidth - 1, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, borderColor);

            int textColor = (alpha << 24) | 0xFFFFFF;
            graphics.drawString(this.font, Component.literal(text), tooltipX + 5, tooltipY + 6, textColor);
        }

        // 渲染配置项按钮提示
        ConfigTooltipInfo hoveredConfigTooltip = null;

        for (ConfigButtonTooltip btnTooltip : configButtonTooltips) {
            if (mouseX >= btnTooltip.x && mouseX <= btnTooltip.x + btnTooltip.width &&
                mouseY >= btnTooltip.y && mouseY <= btnTooltip.y + btnTooltip.height) {
                hoveredConfigTooltip = new ConfigTooltipInfo(btnTooltip.text, mouseX, mouseY + 20);
                break;
            }
        }

        if (hoveredConfigTooltip != null) {
            configTooltipAlpha = Math.min(1.0f, configTooltipAlpha + partialTick * 0.15f);
            activeConfigTooltip = hoveredConfigTooltip;
        } else {
            configTooltipAlpha = Math.max(0.0f, configTooltipAlpha - partialTick * 0.25f);
            if (configTooltipAlpha <= 0.0f) {
                activeConfigTooltip = null;
            }
        }

        if (activeConfigTooltip != null && configTooltipAlpha > 0.01f) {
            int alpha = (int) (configTooltipAlpha * 255);
            int bgColor = (alpha << 24) | 0x2a2a4e;
            int borderColor = (alpha << 24) | 0x6A5ACD;

            String text = activeConfigTooltip.text;
            int textWidth = this.font.width(text);
            int tooltipX = activeConfigTooltip.x;
            int tooltipY = activeConfigTooltip.y;
            int tooltipWidth = textWidth + 12;
            int tooltipHeight = 22;

            if (tooltipX + tooltipWidth > this.width) {
                tooltipX = this.width - tooltipWidth - 5;
            }
            if (tooltipY + tooltipHeight > this.height) {
                tooltipY = activeConfigTooltip.y - 32;
            }

            // 背景
            graphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, bgColor);
            // 边框
            graphics.fill(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + 1, borderColor);
            graphics.fill(tooltipX, tooltipY + tooltipHeight - 1, tooltipX + tooltipWidth, tooltipY + tooltipHeight, borderColor);
            graphics.fill(tooltipX, tooltipY, tooltipX + 1, tooltipY + tooltipHeight, borderColor);
            graphics.fill(tooltipX + tooltipWidth - 1, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight, borderColor);

            int textColor = (alpha << 24) | 0xFFFFFF;
            graphics.drawString(this.font, Component.literal(text), tooltipX + 6, tooltipY + 7, textColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= saveButtonArea[0] && mouseX <= saveButtonArea[0] + saveButtonArea[2] &&
            mouseY >= saveButtonArea[1] && mouseY <= saveButtonArea[1] + saveButtonArea[3]) {
            saveConfig();
            return true;
        }

        if (mouseX >= cancelButtonArea[0] && mouseX <= cancelButtonArea[0] + cancelButtonArea[2] &&
            mouseY >= cancelButtonArea[1] && mouseY <= cancelButtonArea[1] + cancelButtonArea[3]) {
            this.minecraft.setScreen(parent);
            return true;
        }

        for (int i = 0; i < tabButtons.size(); i++) {
            TabButtonInfo tab = tabButtons.get(i);
            if (mouseX >= tab.x && mouseX <= tab.x + tab.width &&
                mouseY >= tab.y && mouseY <= tab.y + tab.height) {
                selectCategory(i);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (contentTotalHeight > contentVisibleHeight) {
            int maxScroll = contentTotalHeight - contentVisibleHeight;
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - scrollY * 30));
            this.init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        if (needsSave) {
            this.minecraft.setScreen(new ConfirmSaveScreen(this, parent));
        } else {
            this.minecraft.setScreen(parent);
        }
    }

    private static class LabelInfo {
        final int x;
        final int y;
        final String text;
        final int alpha;

        LabelInfo(int x, int y, String text, int alpha) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.alpha = alpha;
        }
    }

    private static class TabButtonInfo {
        final int x;
        final int y;
        final int width;
        final int height;
        final String text;
        final String tooltip;
        final boolean selected;

        TabButtonInfo(int x, int y, int width, int height, String text, String tooltip, boolean selected) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.tooltip = tooltip;
            this.selected = selected;
        }
    }

    private static class TooltipInfo {
        final String text;
        final int x;
        final int y;

        TooltipInfo(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }

    private static class ConfigTooltipInfo {
        final String text;
        final int x;
        final int y;

        ConfigTooltipInfo(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }

    private static class ConfigButtonTooltip {
        final int x;
        final int y;
        final int width;
        final int height;
        final String text;

        ConfigButtonTooltip(int x, int y, int width, int height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }
    }

    private static class ConfigCategory {
        final String id;
        final String name;
        final String description;

        ConfigCategory(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }
    }

    private static class ConfirmSaveScreen extends Screen {
        private final Screen configScreen;
        private final Screen parent;

        protected ConfirmSaveScreen(Screen configScreen, Screen parent) {
            super(Component.translatable(TRANSLATION_PREFIX + ".confirm.title"));
            this.configScreen = configScreen;
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            this.addRenderableWidget(Button.builder(
                    Component.translatable(TRANSLATION_PREFIX + ".confirm.save"),
                    btn -> {
                        if (configScreen instanceof ModernConfigScreen modernConfig) {
                            modernConfig.saveConfig();
                        }
                    }
                )
                .pos(centerX - 110, centerY + 20)
                .size(100, 25)
                .build());

            this.addRenderableWidget(Button.builder(
                    Component.translatable(TRANSLATION_PREFIX + ".confirm.discard"),
                    btn -> this.minecraft.setScreen(parent)
                )
                .pos(centerX + 10, centerY + 20)
                .size(100, 25)
                .build());

            this.addRenderableWidget(Button.builder(
                    Component.translatable(TRANSLATION_PREFIX + ".confirm.cancel"),
                    btn -> this.minecraft.setScreen(configScreen)
                )
                .pos(centerX - 50, centerY + 55)
                .size(100, 25)
                .build());
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(graphics, mouseX, mouseY, partialTick);

            int centerX = this.width / 2;
            int centerY = this.height / 2;

            graphics.fill(centerX - 150, centerY - 60, centerX + 150, centerY + 90, 0xDD1a1a2e);
            graphics.fill(centerX - 150, centerY - 60, centerX + 150, centerY - 58, 0xFFFF6B6B);

            graphics.drawCenteredString(this.font,
                Component.translatable(TRANSLATION_PREFIX + ".confirm.title"), centerX, centerY - 40, 0xFFFFFF);
            graphics.drawCenteredString(this.font,
                Component.translatable(TRANSLATION_PREFIX + ".confirm.message1"), centerX, centerY - 15, 0xFFFFFF);
            graphics.drawCenteredString(this.font,
                Component.translatable(TRANSLATION_PREFIX + ".confirm.message2"), centerX, centerY, 0xFFFFFF);

            super.render(graphics, mouseX, mouseY, partialTick);
        }
    }
}
