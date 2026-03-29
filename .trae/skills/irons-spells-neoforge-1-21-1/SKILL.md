***

name: irons-spells-neoforge-1.21.1
description: "提供Iron's Spells 'n Spellbooks模组在NeoForge 1.21.1环境下的法术开发API参考。Invoke when developing custom spells for Iron's Spells mod on NeoForge 1.21.1."
----------------------------------------------------------------------------------------------------------------------------------------------------------

# Iron's Spells 'n Spellbooks - NeoForge 1.21.1 法术开发指南

## 环境信息

- **平台**: NeoForge 1.21.1
- **模组版本**: Iron's Spells 'n Spellbooks 3.15.3+
- **依赖库**: GeckoLib, PlayerAnimator, Curios
- **扩展API**: Aces_Spell_Utils (可选，提供额外功能)
  ## 在开发时请确定开发环境是否合适！

***

## 1. 法术基础类 (AbstractSpell)

### 核心方法

```java
// 获取法术资源位置（必须实现）
public abstract ResourceLocation getSpellResource();

// 获取默认配置（必须实现）
public abstract DefaultConfig getDefaultConfig();

// 获取施法类型（必须实现）
public abstract CastType getCastType();

// 获取法术流派
public SchoolType getSchoolType();

// 施法逻辑
public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData magicData);

// 检查施法前条件
public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magicData);
```

### 施法类型 (CastType)

```java
CastType.INSTANT      // 瞬发
CastType.LONG         // 长施法（可被打断）
CastType.CONTINUOUS   // 持续施法
```

### 法术流派 (SchoolType) - 通过 SchoolRegistry 获取

```java
SchoolRegistry.FIRE.get()        // 火焰
SchoolRegistry.ICE.get()         // 冰霜
SchoolRegistry.LIGHTNING.get()   // 闪电
SchoolRegistry.HOLY.get()        // 神圣
SchoolRegistry.ENDER.get()       // 末影
SchoolRegistry.BLOOD.get()       // 血系
SchoolRegistry.EVOCATION.get()   // 召唤
SchoolRegistry.NATURE.get()      // 自然
SchoolRegistry.ELDRITCH.get()    // 诡秘
```

***

## 2. 法术配置 (DefaultConfig)

### 配置方法

```java
new DefaultConfig()
    .setMinRarity(SpellRarity.LEGENDARY)     // 最小稀有度
    .setMaxLevel(5)                           // 最大等级
    .setCooldownSeconds(60)                   // 冷却时间（秒）
    .setAllowCrafting(true)                   // 允许合成
    .setSchoolResource(ResourceLocation)      // 流派资源位置
    .build();
```

### 稀有度等级 (SpellRarity)

```java
SpellRarity.COMMON      // 普通 (0)
SpellRarity.UNCOMMON    // 稀有 (1)
SpellRarity.RARE        // 罕见 (2)
SpellRarity.EPIC        // 史诗 (3)
SpellRarity.LEGENDARY   // 传奇 (4)
```

***

## 3. 法术注册

### 注册类示例

```java
public class ModSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(
        SpellRegistry.SPELL_REGISTRY_KEY,
        "your_modid"
    );

    public static final DeferredHolder<AbstractSpell, YourSpell> YOUR_SPELL = SPELLS.register(
        "your_spell_id",
        YourSpell::new
    );

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
```

### 在主类中注册

```java
public YourMod(IEventBus modEventBus, ModContainer modContainer) {
    ModSpells.register(modEventBus);
}
```

***

## 4. 粒子效果 API

### 简单粒子类型 (SimpleParticleType)

```java
// 血系粒子
ParticleRegistry.BLOOD_PARTICLE.get()
ParticleRegistry.BLOOD_GROUND_PARTICLE.get()

// 幽灵粒子
ParticleRegistry.WISP_PARTICLE.get()

// 其他粒子
ParticleRegistry.SNOWFLAKE_PARTICLE.get()
ParticleRegistry.ELECTRICITY_PARTICLE.get()
ParticleRegistry.FIRE_PARTICLE.get()
ParticleRegistry.EMBER_PARTICLE.get()
ParticleRegistry.SPARK_PARTICLE.get()
```

### 复杂粒子类型 (带Options)

```java
// Blastwave - 冲击波
new BlastwaveParticleOptions(Vector3f color, float scale)

// Shockwave - 环形冲击波
new ShockwaveParticleOptions(Vector3f color, float scale, boolean fullbright)

// Fog - 雾气
new FogParticleOptions(Vector3f color, float scale)

// Spark - 火花
new SparkParticleOptions(Vector3f color)

// Trace - 轨迹
new TraceParticleOptions(Vector3f color, Vector3f destination)
```

### 使用示例

```java
// 血系冲击波
level.sendParticles(
    new BlastwaveParticleOptions(new Vector3f(0.6f, 0f, 0f), 2.0f),
    pos.x, pos.y, pos.z,
    1, 0, 0, 0, 0
);

// 血雾
level.sendParticles(
    new FogParticleOptions(new Vector3f(0.6f, 0f, 0f), 1.5f),
    pos.x, pos.y, pos.z,
    5, 0.2, 0.1, 0.2, 0.02
);

// 血滴
level.sendParticles(
    ParticleRegistry.BLOOD_PARTICLE.get(),
    pos.x, pos.y, pos.z,
    10, 0.1, 0.2, 0.1, 0.05
);
```

***

## 5. 召唤亡灵 API

### SummonedZombie

```java
// 创建召唤的僵尸
SummonedZombie summonedZombie = new SummonedZombie(level, summoner, true);

// 设置召唤者
summonedZombie.setSummoner(summoner);

// 触发起身动画
summonedZombie.triggerRiseAnimation();

// 添加到世界
level.addFreshEntity(summonedZombie);
```

### 特性

- 自动识别召唤者为主人
- 不会攻击召唤者
- 会攻击召唤者的敌人
- 播放起身动画（从地下钻出）
- 自然消失时播放消失效果

***

## 6. Aces_Spell_Utils 扩展API (可选)

Aces_Spell_Utils 是一个为 Iron's Spellbooks 提供扩展功能的API插件，需要额外添加依赖。

### 6.1 自定义施法生物

#### UniqueAbstractSpellCastingMob
用于创建带有自定义模型和动画的施法生物。

```java
public class MyCustomCaster extends UniqueAbstractSpellCastingMob {
    public MyCustomCaster(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
    
    // 定义动画控制器
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(instantCastAnimationController);
        controllers.add(longCastAnimationController);
        controllers.add(contCastAnimationController);
        controllers.add(new AnimationController(this, "idle", 0, this::predicate));
    }
}
```

**支持的动画类型**:
- `instantCast` - 瞬发法术动画
- `longCast` - 长施法动画
- `continuousCast` - 持续施法动画
- `slashCast` - 斩击类法术动画
- `stompCast` - 踩踏类法术动画

#### GenericUniqueBossEntity
用于创建带有多阶段系统的Boss。

```java
public class MyBoss extends GenericUniqueBossEntity {
    public MyBoss(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.hasCustomMusic = true;
        this.changeMusicOnPhaseChange = true;
    }
    
    @Override
    public SoundEvent getBossMusic() {
        return MySounds.BOSS_MUSIC.get();
    }
    
    // 阶段转换
    public void onPhaseChange() {
        if (getHealth() < getMaxHealth() * 0.5) {
            setPhase(Phase.SecondPhase);
        }
    }
}
```

**阶段枚举**:
- `FirstPhase` 到 `TwelfthPhase` (共12个阶段)

### 6.2 自定义法术流派

```java
public class MySchoolRegistry {
    private static final DeferredRegister<SchoolType> SCHOOLS = 
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, "modid");
    
    public static final Supplier<SchoolType> CUSTOM_SCHOOL = registerSchool(
        new SchoolType(
            ResourceLocation.fromNamespaceAndPath("modid", "custom_school"),
            MyTags.CUSTOM_FOCUS,                    // 焦点标签
            Component.translatable("school.modid.custom").withStyle(Style.EMPTY.withColor(0xFF0000)),
            MyAttributeRegistry.CUSTOM_SPELL_POWER, // 法术强度属性
            MyAttributeRegistry.CUSTOM_SPELL_RESIST,// 法术抗性属性
            SoundRegistry.EVOCATION_CAST,           // 施法音效
            MyDamageTypes.CUSTOM_MAGIC              // 伤害类型
        )
    );
}
```

### 6.3 特殊物品类型

#### MagicGunItem - 魔法枪械
```java
public class MyMagicGun extends MagicGunItem {
    public MyMagicGun(Tier tier, Properties properties, SpellDataRegistryHolder[] spells) {
        super(tier, properties, spells);
    }
    
    @Override
    protected int getPassiveCooldownTicks() {
        return 100; // 被动技能冷却（tick）
    }
    
    @Override
    protected int getActiveCooldownTicks() {
        return 200; // 主动技能冷却（tick）
    }
    
    @Override
    public boolean isHeavyGun() {
        return true; // 重型枪械
    }
}
```

#### PassiveAbilitySpellbook - 被动技能法术书
```java
public class MySpellbook extends PassiveAbilitySpellbook {
    public MySpellbook() {
        super(5); // 5个法术槽位
    }
    
    @Override
    protected int getCooldownTicks() {
        return 300; // 被动技能冷却
    }
}
```

#### 链锤类武器
```java
// 可附魔链锤
public class MyMace extends ImbueableMaceItem {
    public MyMace(Tier tier, Properties properties, SpellDataRegistryHolder[] spells) {
        super(tier, properties, spells);
    }
}

// 链锤法杖
public class MyMaceStaff extends MaceStaffItem {
    public MyMaceStaff(Tier tier, Properties properties, SpellDataRegistryHolder[] spells) {
        super(tier, properties, spells);
    }
}
```

### 6.4 召唤法术基类

```java
public class MySummonSpell extends AbstractSummonSpell {
    @Override
    protected int onSummoningCast(Level level, int spellLevel, LivingEntity caster, 
                                   CastSource castSource, MagicData playerMagicData, 
                                   SummonedEntitiesCastData castData) {
        // 召唤生物
        spawnHelper(x, y, z, caster, level, summonTimer, castData, 
            () -> new MySummonEntity(level), 
            getSpellId(), spellLevel);
        
        return 600; // 召唤物持续时间（tick）
    }
}
```

### 6.5 着色器效果

```java
// 发送着色器效果包到客户端
AddShaderEffectPacket packet = new AddShaderEffectPacket(
    player.getUUID(), 
    "aces_spell_utils:grayscale", // 着色器ID
    100 // 持续时间（tick）
);
PacketDistributor.sendToPlayer(player, packet);

// 移除着色器效果
RemoveShaderEffectPacket removePacket = new RemoveShaderEffectPacket(
    player.getUUID()
);
PacketDistributor.sendToPlayer(player, removePacket);
```

**内置着色器**:
- `aces_spell_utils:grayscale` - 灰度效果
- `aces_spell_utils:grayscale_darker` - 深灰度效果

### 6.6 实用工具类

```java
// 获取标签中的法术列表
List<AbstractSpell> slashSpells = ASUtils.getSpellsFromTag(ASTags.SLASH_LIKE_SPELL);
List<AbstractSpell> stompSpells = ASUtils.getSpellsFromTag(ASTags.STOMP_LIKE_SPELL);

// 时间格式化
String time = Utils.timeFromTicks(ticks, decimalPlaces);
```

***

## 7. 语言文件

### 中文 (zh_cn.json)

```json
{
  "spell.your_modid.your_spell": "法术名称",
  "spell.your_modid.your_spell.guide": "法术描述",
  "spell.your_modid.your_spell.info1": "信息1: %d",
  "spell.your_modid.your_spell.info2": "信息2: %d"
}
```

***

## 8. 常用事件

### 生物死亡事件

```java
@SubscribeEvent
public static void onLivingDeath(LivingDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!(entity.level() instanceof ServerLevel serverLevel)) return;
    
    // 处理死亡逻辑
}
```

### 世界Tick事件

```java
@SubscribeEvent
public static void onLevelTick(LevelTickEvent.Post event) {
    if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
    
    // 每tick执行逻辑
}
```

***

## 9. 常见错误与解决方案

### 9.1 法术强度计算错误

**问题**: Iron's Spellbooks 的 `SPELL_POWER` 属性默认值为 `1.0`（表示100%），而不是 `10.0`。

**错误代码**:
```java
// 错误：假设基础值为 10.0
float spellPower = entity.getAttributeValue(AttributeRegistry.SPELL_POWER);
float multiplier = spellPower / 10.0f; // 结果为 0.1（10%），预期为 1.0（100%）
```

**正确代码**:
```java
// 正确：基础值为 1.0
float spellPower = SpellPowerHelper.getBaseSpellPowerAttribute(entity);
float multiplier = spellPower / 1.0f; // 结果为 1.0（100%）

// SpellPowerHelper 实现
public class SpellPowerHelper {
    public static final float BASE_SPELL_POWER_DEFAULT = 1.0f;
    
    public static float getBaseSpellPowerAttribute(LivingEntity entity) {
        if (entity == null) {
            return BASE_SPELL_POWER_DEFAULT;
        }
        float value = (float) entity.getAttributeValue(AttributeRegistry.SPELL_POWER);
        return Math.max(value, BASE_SPELL_POWER_DEFAULT * 0.1f);
    }
}
```

### 9.2 MobEffect 属性修饰符重复添加

**问题**: 在 `MobEffect` 的构造函数中使用 `addAttributeModifier()` 添加属性修饰符，然后在外部代码中又手动添加相同ID的修饰符，导致 `IllegalArgumentException: Modifier is already applied on this attribute!`

**错误代码**:
```java
public class MyBuffEffect extends MobEffect {
    public MyBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
        // 在构造函数中添加修饰符
        this.addAttributeModifier(
            AttributeRegistry.SPELL_POWER,
            ResourceLocation.fromNamespaceAndPath("modid", "my_buff"),
            0.1,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}

// 在外部代码中又手动添加
public void applyBuff(Player player) {
    player.addEffect(new MobEffectInstance(ModEffects.MY_BUFF.get(), 200, 0));
    
    // 错误：重复添加相同ID的修饰符
    var attr = player.getAttribute(AttributeRegistry.SPELL_POWER);
    attr.addTransientModifier(new AttributeModifier(
        ResourceLocation.fromNamespaceAndPath("modid", "my_buff"),
        0.1,
        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    ));
}
```

**解决方案**:

**方案A**: 只在构造函数中添加修饰符（推荐用于固定数值）
```java
public class MyBuffEffect extends MobEffect {
    public MyBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
        // 只在构造函数中添加修饰符
        this.addAttributeModifier(
            AttributeRegistry.SPELL_POWER,
            ResourceLocation.fromNamespaceAndPath("modid", "my_buff"),
            0.1,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}
```

**方案B**: 不在构造函数中添加，完全由外部代码动态管理（推荐用于动态数值）
```java
public class MyBuffEffect extends MobEffect {
    public MyBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFFFF);
        // 不在构造函数中添加任何修饰符
    }
}

// 在外部代码中手动管理
public void applyBuff(Player player, double spellPowerBonus) {
    player.addEffect(new MobEffectInstance(ModEffects.MY_BUFF.get(), 200, 0));
    
    // 手动添加修饰符（使用不同的ID或先移除旧的）
    var attr = player.getAttribute(AttributeRegistry.SPELL_POWER);
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("modid", "my_buff_dynamic");
    
    // 先移除旧的（如果存在）
    attr.removeModifier(id);
    
    // 添加新的
    attr.addTransientModifier(new AttributeModifier(
        id,
        spellPowerBonus,
        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    ));
}
```

### 9.3 固定值属性修饰符在Buff提示中显示问题

**问题**: 希望Buff的某个属性固定值（如-30%最大法力值），但Minecraft会自动将属性修饰符的值乘以 `(amplifier + 1)`（即Buff等级），导致5级时显示-150%。

**解决方案**:

```java
public class MagicShotgunBuffEffect extends MobEffect {
    // 实际效果值
    public static final double MAX_MANA_REDUCTION = -0.30;
    
    // 显示值（用于EMIffect等模组）
    // 计算：DISPLAY = ACTUAL / MAX_LEVEL
    public static final double MAX_MANA_REDUCTION_DISPLAY = -0.30 / 5; // -0.06
    public static final int MAX_BUFF_LEVEL = 5;
    
    public MagicShotgunBuffEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x4A0080);
        
        // 使用显示值，这样在EMIffect中会显示为-30%（5级时）
        this.addAttributeModifier(
            AttributeRegistry.MAX_MANA,
            ResourceLocation.fromNamespaceAndPath("modid", "magic_shotgun_max_mana"),
            MAX_MANA_REDUCTION_DISPLAY, // 使用-0.06而不是-0.30
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}

// 在外部代码中调整实际效果
public void applyBuff(Player player, int buffLevel) {
    player.addEffect(new MobEffectInstance(ModEffects.MAGIC_SHOTGUN_BUFF.get(), 200, buffLevel - 1));
    
    // 调整实际效果为固定-30%
    var maxManaAttr = player.getAttribute(AttributeRegistry.MAX_MANA);
    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("modid", "magic_shotgun_max_mana");
    
    // 移除Buff自动添加的修饰符
    maxManaAttr.removeModifier(id);
    
    // 计算实际值：目标值 / buffLevel
    double actualValue = MagicShotgunBuffEffect.MAX_MANA_REDUCTION / buffLevel;
    
    // 添加调整后的修饰符
    maxManaAttr.addTransientModifier(new AttributeModifier(
        id,
        actualValue,
        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
    ));
}
```

**注意**: 这种方法在5级Buff时会正确显示-30%，但在1级Buff时会显示-6%。这是Minecraft属性系统的限制。

### 9.4 死亡检查保护

**问题**: 在实体死亡时处理效果（如清除标记、触发反应）可能导致 `ConcurrentModificationException`，特别是在 `HashMap` 迭代过程中修改效果列表。

**解决方案**:
```java
public static void handleReaction(ServerLevel serverLevel, LivingEntity target, LivingEntity attacker,
                                  ElementType existingElement, ElementType newElement, int existingLevel) {
    // 检查目标是否已死亡或正在死亡
    if (!target.isAlive() || target.isDeadOrDying()) {
        return;
    }
    
    // 处理反应逻辑...
}

public static void removeElementMark(LivingEntity target, ElementType elementType) {
    // 检查目标是否已死亡或正在死亡
    if (!target.isAlive() || target.isDeadOrDying()) {
        return;
    }
    
    // 移除标记逻辑...
}
```

### 9.5 属性默认值

**重要**: Iron's Spellbooks 的所有属性默认值都是 `1.0`（表示100%），不是 `0` 或 `10.0`。

```java
// 获取属性值时，如果不存在则返回1.0
private static double getAttributeValue(LivingEntity entity, Holder<Attribute> attribute) {
    if (entity.getAttributes().hasAttribute(attribute)) {
        return entity.getAttributeValue(attribute);
    }
    return 1.0; // 铁魔法属性默认值是1.0（100%）
}

// 计算加成时，需要减去基础值1.0
double enderPower = getAttributeValue(entity, AttributeRegistry.ENDER_SPELL_POWER);
double enderBonus = enderPower - 1.0; // 获取实际加成值
double spellPowerBonus = enderBonus * 0.333; // 计算最终加成
```

***

## 10. 注意事项

1. **必须在服务器端执行**: 大多数法术逻辑应该在 `ServerLevel` 中执行
2. **检查玩家在线**: 使用 `ServerPlayer` 时注意检查玩家是否在线
3. **数据持久化**: 使用 `SavedData` 保存需要持久化的数据
4. **性能优化**: 避免在tick事件中执行耗时操作
5. **网络同步**: 需要客户端显示的效果要手动同步
6. **属性修饰符唯一性**: 确保每个属性修饰符的ID唯一，避免重复添加
7. **死亡检查**: 在处理实体效果时，始终检查 `isAlive()` 和 `isDeadOrDying()`
8. **Aces_Spell_Utils依赖**: 如果使用Aces_Spell_Utils的功能，需要将其添加到项目依赖中

***

## 参考资源

- [Iron's Spells GitHub](https://github.com/iron431/Irons-Spellbooks)
- [NeoForge Documentation](https://docs.neoforged.net/)
- [Minecraft Forge Wiki](https://mcforge.readthedocs.io/)
- [Aces_Spell_Utils GitHub](https://github.com/ACETHEELDRITCHKING/Aces_Spell_Utils) (如果可用)
