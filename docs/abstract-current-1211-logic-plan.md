# 把当前 1.21.1 分支完整逻辑抽象化的计划

## 1. 真实目标

当前真正目标不是“重新写一套 Forge / NeoForge 架构”，也不是“让 bridge 看起来更抽象”。

真实目标是：

```text
以当前 1.21.1 分支为唯一行为基准，
把它现有的完整 NeoForge 1.21.1 逻辑逐步抽象成 common 语义层，
然后让 targets/neoforge-1.21.1 只负责把这些语义映射回 NeoForge API。
```

也就是说：

```text
第一目标不是 Forge 1.20.1 可玩。
第一目标是 NeoForge 1.21.1 行为不能丢。
```

后续 Forge 1.20.1 只是实现同一套语义 API 的另一个 target。

---

## 2. 正确迁移方式

不要这样做：

```text
先设计一套理想 bridge，
再把 1.21.1 原代码硬塞进去，
最后发现 NeoForge 行为和原分支不同。
```

应该这样做：

```text
1. 从当前 1.21.1 分支读取真实代码。
2. 按功能块提取行为语义。
3. 每抽一个语义，就让 NeoForge 1.21.1 target 调回原逻辑。
4. 确认行为等价后，再抽下一个语义。
5. Forge 1.20.1 先 stub，不允许影响 NeoForge 1.21.1 行为。
```

一句话：

```text
不是重新发明逻辑，而是把现有 1.21.1 逻辑“搬到抽象层下面”。
```

---

## 3. 迁移总原则

### 3.1 当前 1.21.1 分支是事实来源

Codex 必须使用当前 `1.21.1` 分支作为 baseline。

允许使用：

```bash
git show 1.21.1:<path>
```

或：

```bash
git worktree add ../baseline-1.21.1 1.21.1
```

禁止凭猜测补逻辑。

---

### 3.2 每个抽象都必须回答三个问题

每次新增 bridge / service / message / adapter，都必须回答：

```text
1. 原 1.21.1 分支里这段逻辑在哪个类、哪个方法？
2. 抽象后 common 中表达的语义是什么？
3. targets/neoforge-1.21.1 如何调用原等价逻辑？
```

如果回答不了，就不要继续抽象。

---

### 3.3 抽象的是行为，不是 API 名字

错误抽象：

```text
sendPayload
registerPayload
openMenuWithBuffer
getAttachment
setDataComponent
```

正确抽象：

```text
requestOpenPlayerGridInventory
openPlayerGridInventory
syncGridInventory
syncEquipmentStorage
saveGridInventoryOnItem
loadGridInventoryFromPlayer
registerBackpackAccessoryBehavior
```

---

## 4. 当前应该保留的工程方向

目标工程结构：

```text
common
targets/forge-1.20.1
targets/neoforge-1.21.1
```

其中：

```text
common：放从 1.21.1 逻辑抽出来的语义、数据结构、公共算法。
targets/neoforge-1.21.1：保存 NeoForge API 映射，行为必须等价于当前 1.21.1 分支。
targets/forge-1.20.1：先实现 stub 或最小实现，不能干扰 NeoForge 等价性。
```

---

## 5. 抽象顺序

必须按顺序做，不要乱跳。

```text
第 1 轮：原 1.21.1 行为清单
第 2 轮：内容注册抽象
第 3 轮：ItemStack 数据抽象
第 4 轮：玩家数据抽象
第 5 轮：菜单打开抽象
第 6 轮：网络协议抽象
第 7 轮：装备储物与同步抽象
第 8 轮：Curios / 饰品抽象
第 9 轮：客户端事件与 mixin 抽象
第 10 轮：NeoForge 1.21.1 行为回归测试
第 11 轮：Forge 1.20.1 逐步实现同语义
```

---

## 6. 第 1 轮：原 1.21.1 行为清单

先不要改大量代码。

先从 `1.21.1` 分支抽取完整清单。

需要读取并记录：

```text
DFGridInventoryMod
DFGridInventoryCommon
ModItems
ModMenus
ModCreativeTabs
ModDataComponents
ModAttachments
ModNetworking
PlayerGridInventoryOpener
GridInventoryMenu
GridInventoryScreen
GridBackpackItem
SmallGridBagItem
EquipmentStorageManager
EquipmentStorageEvents
EquipmentStorageLoader
GridItemSizeLoader
GridItemSizeSyncManager
BackpackFoldingLoader
BackpackFoldingSyncManager
ManualPickupEvents
ClientEvents
MinecraftMixin
InventoryScreenMixin
NearbyItemsPanel
PickupPromptHud
ItemEntityHighlightRenderer
CuriosIntegration / Curios 相关逻辑
```

输出到：

```text
docs/current-1.21.1-logic-map.md
```

文档结构：

```markdown
# 当前 1.21.1 逻辑地图

## 入口注册
- 原类：...
- 原方法：...
- 做了什么：...
- 抽象目标：...
- 新位置：...

## 物品数据
...

## 玩家数据
...

## 菜单打开
...

## 网络包
...

## 客户端行为
...
```

没有这份 logic map，不允许继续大规模抽象。

---

## 7. 第 2 轮：内容注册抽象

目标：

```text
把 1.21.1 分支中已有的物品、菜单、创造栏注册逻辑抽象成 common 注册意图。
```

common 中保留：

```text
ModItems
ModMenus
ModCreativeTabs
```

但这些类不能暴露：

```text
DeferredRegister
DeferredItem
DeferredHolder
```

common 只能表达：

```text
注册名
创建工厂
语义分类
```

NeoForge target 做：

```text
把 common 的注册意图映射到 NeoForge DeferredRegister。
```

要求：

```text
1. 物品注册名必须和当前 1.21.1 分支完全一致。
2. 菜单注册名必须和当前 1.21.1 分支完全一致。
3. 创造栏物品顺序尽量和当前 1.21.1 分支一致。
4. NeoForge 端注册时机必须与原分支等价。
```

---

## 8. 第 3 轮：ItemStack 数据抽象

目标：

```text
把当前 1.21.1 分支中使用 DataComponentType 的 ItemStack 数据逻辑抽象成 common 语义。
```

common 只允许调用：

```java
GridServices.itemStackData().getGridInventory(stack)
GridServices.itemStackData().setGridInventory(stack, data)
GridServices.itemStackData().getEquipmentStorage(stack)
GridServices.itemStackData().setEquipmentStorage(stack, data)
GridServices.itemStackData().isBackpackFolded(stack)
GridServices.itemStackData().setBackpackFolded(stack, folded)
```

NeoForge 1.21.1 target：

```text
继续使用当前 1.21.1 分支原来的 DataComponentType。
```

Forge 1.20.1 target：

```text
先用 NBT 或 stub 实现，后续完善。
```

关键要求：

```text
GridBackpackItem、SmallGridBagItem、EquipmentStorageManager 等 common 逻辑不能直接调用 DataComponentType。
```

---

## 9. 第 4 轮：玩家数据抽象

目标：

```text
把当前 1.21.1 分支中的 NeoForge Attachment 玩家数据逻辑抽象成 common 语义。
```

common 只允许：

```java
GridServices.playerData().getPlayerGridInventory(player)
GridServices.playerData().setPlayerGridInventory(player, data)
GridServices.playerData().copyPlayerGridInventory(player)
```

NeoForge 1.21.1 target：

```text
继续使用原 1.21.1 分支 Attachment。
```

必须从原分支确认：

```text
1. attachment 默认值。
2. copyOnDeath 行为。
3. 登录 / 重生 / menu sync 时机。
4. 是否使用 copy。
```

Forge 1.20.1 target：

```text
先 stub 或 persistent NBT，后续可换 Capability。
```

---

## 10. 第 5 轮：菜单打开抽象

目标：

```text
把当前 1.21.1 分支中的 openMenu / buffer 写入 / GridInventoryMenu fromNetwork 完整抽象出来。
```

common 中定义：

```java
GridMenuOpenData
```

字段必须来自当前 1.21.1 分支：

```text
sourceSlot
hand
playerInventory
data
```

必须从原分支确认：

```text
1. 服务端写入顺序。
2. 客户端读取顺序。
3. GridInventoryMenu 构造参数。
4. sourceSlot 为 -1 的语义。
5. hand 的默认值。
6. playerInventory 的含义。
7. data 是否 copy。
```

common 中：

```java
PlayerGridInventoryOpener.open(player)
```

只允许：

```text
1. 读 player data。
2. 初始化/修复数据。
3. 写回 player data。
4. 调用 GridServices.menus().openPlayerGridInventory(player, openData)。
```

NeoForge 1.21.1 target：

```text
用原 1.21.1 分支等价的 openMenu 方式。
```

---

## 11. 第 6 轮：网络协议抽象

目标：

```text
把当前 1.21.1 分支的所有 CustomPacketPayload 抽成 common 语义消息。
```

注意：

```text
不是先设计新协议。
是把当前 1.21.1 分支已有协议逐包抽象。
```

流程：

```text
1. 从原 1.21.1 分支读取 ModNetworking.register。
2. 按注册顺序列出所有 packet。
3. 对每个 packet 读取 TYPE、STREAM_CODEC、字段、handler。
4. 在 common 中创建语义 message。
5. 在 NeoForge target 中创建显式 codec，字段顺序必须完全一致。
6. 移除反射式 encode/decode。
```

禁止：

```text
1. 反射查找 encode/decode。
2. 缺 decode 时自动 unit constructor。
3. 猜字段顺序。
4. 改 packet id。
5. 改 direction。
```

NeoForge target 必须有：

```text
NeoForge1211MessageCodecs
NeoForge1211ProtocolCompat
NeoForge1211MessageContext
```

其中 `NeoForge1211MessageCodecs` 必须是显式 codec 注册表。

示例：

```java
register(GridMessages.SYNC_GRID_INVENTORY, new Codec<>() {
    public void encode(SyncGridInventoryMessage message, RegistryFriendlyByteBuf buf) {
        message.data().encode(buf);
    }

    public SyncGridInventoryMessage decode(RegistryFriendlyByteBuf buf) {
        return new SyncGridInventoryMessage(GridInventoryData.decode(buf));
    }
});
```

---

## 12. 第 7 轮：装备储物与同步抽象

目标：

```text
把 EquipmentStorage 相关行为从 1.21.1 分支完整抽成语义。
```

需要读取：

```text
EquipmentStorageData
EquipmentStorageManager
EquipmentStorageEvents
EquipmentStorageLoader
SyncEquipmentStoragePacket / Message
```

必须保留：

```text
1. 胸甲 / 护腿储物初始化规则。
2. 装备变化时的数据保存与迁移。
3. syncEquipmentStorage 的触发时机。
4. 客户端接收后写回哪个对象。
5. 与菜单 UI 的关系。
```

---

## 13. 第 8 轮：Curios / 饰品抽象

目标：

```text
把当前 1.21.1 分支中 Curios 行为抽象成 AccessoriesBridge。
```

common 语义：

```java
GridServices.accessories().registerBackpackAccessories()
GridServices.accessories().canQuickEquip(player, stack)
GridServices.accessories().quickEquip(player, stack)
GridServices.accessories().collectSlots(player)
```

NeoForge 1.21.1 target：

```text
继续使用 CuriosApi。
```

必须保证：

```text
1. 注册的背包物品集合一致。
2. 只允许 back slot。
3. canEquipFromUse 时 unfold。
4. quick equip 行为和原分支一致。
```

Forge 1.20.1 target：

```text
先 no-op 或 Curios Forge stub。
```

---

## 14. 第 9 轮：客户端行为抽象

目标：

```text
把当前 1.21.1 分支的客户端行为分成 common client logic + target event/mixin adapter。
```

common/client 可保留：

```text
GridInventoryScreen
NearbyItemsPanel
PickupPromptHud
ClientPickupController
ClientItemTargeting
ItemEntityHighlightRenderer 的跨版本稳定部分
Tooltip 语义逻辑
布局计算
SlotPositionAccessor 接口
```

NeoForge target 保留：

```text
RegisterMenuScreensEvent
RegisterKeyMappingsEvent
ClientTickEvent
RenderGuiEvent
RenderLevelStageEvent
ItemTooltipEvent
FMLClientSetupEvent
MinecraftMixin
InventoryScreenMixin
SlotAccessor mixin
```

必须从原分支确认：

```text
1. E 键拦截条件。
2. manual pickup 按键行为。
3. tooltip 文本。
4. HUD 渲染时机。
5. nearby panel 点击和拖拽释放。
6. item highlight 判断。
7. item property id。
```

---

## 15. 第 10 轮：NeoForge 1.21.1 行为回归

完成抽象后，先测试 NeoForge 1.21.1，不要急着推进 Forge。

验收：

```text
1. :targets:neoforge-1.21.1:compileJava 通过。
2. :targets:neoforge-1.21.1:runClient 能启动。
3. 进世界不崩。
4. 按 E 打开行为和原分支一致。
5. 菜单打开数据一致。
6. 拖拽 / 移动 / 插入 / 提取一致。
7. 装备储物一致。
8. 附近物品栏一致。
9. 背包折叠一致。
10. Curios 行为一致。
11. datapack reload 同步一致。
```

如果 NeoForge 1.21.1 行为不等价，不要继续实现 Forge。

---

## 16. Forge 1.20.1 的位置

Forge 1.20.1 不是本阶段主角。

本阶段 Forge 1.20.1 只需要：

```text
1. compileJava 尽量通过。
2. target 结构存在。
3. bridge stub 清晰。
4. 不引用 NeoForge 类。
5. 不影响 NeoForge target。
```

后续等 NeoForge 1.21.1 行为等价后，再逐步实现 Forge。

---

## 17. 禁止行为

```text
1. 不要继续实现 Fabric。
2. 不要凭猜测重写 1.21.1 逻辑。
3. 不要先做 Forge 1.20.1 完整功能。
4. 不要用反射 codec 当最终方案。
5. 不要改 packet id。
6. 不要改 packet direction。
7. 不要改菜单注册名。
8. 不要改物品注册名。
9. 不要改 mod_id。
10. 不要删除核心逻辑来换编译通过。
11. 不要让 common import Forge / NeoForge / Fabric / Curios。
12. 不要让 common 直接依赖 CustomPacketPayload / StreamCodec / RegistryFriendlyByteBuf。
13. 不要让 Forge target 引用 NeoForge 类。
14. 不要让 NeoForge target 引用 Forge 类。
```

---

## 18. 给 Codex 的执行提示词

可以直接发送：

```text
请阅读 docs/abstract-current-1211-logic-plan.md。

当前真实目标不是重新设计一套 bridge，而是把当前 1.21.1 分支的完整逻辑逐步抽象成 common 语义层。

请严格使用 1.21.1 分支作为事实来源，通过 git show 1.21.1:<path> 或 worktree 读取原代码。

第一步先不要大改代码，请先生成 docs/current-1.21.1-logic-map.md，完整记录原 1.21.1 分支的入口注册、物品注册、数据组件、玩家 attachment、菜单打开、网络包、装备储物、Curios、客户端事件、mixin 行为。

然后按计划逐轮抽象：
1. 内容注册
2. ItemStack 数据
3. 玩家数据
4. 菜单打开
5. 网络协议
6. 装备储物与同步
7. Curios / 饰品
8. 客户端事件与 mixin

每抽一块，都必须保证 targets/neoforge-1.21.1 的行为和当前 1.21.1 分支一致。
Forge 1.20.1 暂时允许 stub，不允许为了 Forge 改坏 NeoForge。

特别禁止：
- 不要用反射 codec 当最终方案。
- 不要改 packet id / direction / 字段顺序。
- 不要凭猜测补逻辑。
- 不要继续实现 Fabric。
```

---

## 19. 一句话总结

```text
先把 1.21.1 现有逻辑完整抽象出来，
再让 NeoForge 1.21.1 target 调用这些抽象并保持行为等价，
最后才让 Forge 1.20.1 实现同一套语义。
```
