# DF Grid Inventory 通用容器伴随栏设计

> 目标：让 DF Grid Inventory 能在尽量不改变原版与第三方模组 Screen 的前提下，兼容工作台、箱子、熔炉、机器方块等几乎所有基于 `AbstractContainerScreen` 的容器界面。

---

## 1. 背景

当前 DF Grid Inventory 已经有独立的玩家网格背包界面，主界面由三栏组成：

1. 装备栏 / 装备储物栏；
2. 玩家主网格背包；
3. 附近物品栏。

这个界面适合玩家直接打开自己的背包，但并不适合直接替换所有功能方块 Screen。

例如：

- 工作台有自己的输入槽、输出槽、配方书；
- 熔炉有输入槽、燃料槽、输出槽、进度条；
- 箱子有不同大小的容器槽位；
- 第三方模组机器可能有能量槽、流体槽、过滤槽、升级槽、幽灵槽、按钮、进度条、配方区等。

如果强行替换这些 Screen，很容易破坏原版或第三方模组的交互逻辑。

因此，本设计采用 **伴随栏 Sidecar** 思路：

> 原 Screen 保持不变，只在 Screen 一侧追加一个 DF Grid Inventory 背包栏。

---

## 2. 核心目标

### 2.1 玩家体验目标

当玩家打开任意功能方块 Screen 时：

- 原 Screen 基本保持原样；
- 原 Screen 的按钮、进度条、槽位、tooltip、配方书等不被重做；
- 在 Screen 左侧或右侧显示一个 DF Grid Inventory 伴随栏；
- 玩家可以从伴随栏拖动物品到原 Screen 的 Slot；
- 玩家也可以从原 Screen 的 Slot 拖动物品回伴随栏；
- 原 Screen 的所有 Slot 对伴随栏来说都可以视为“单格自由槽目标”；
- 但实际能不能放、能不能取，仍然由原 `Slot` 和原 `AbstractContainerMenu` 的规则决定。

### 2.2 技术目标

- 不为工作台、箱子、熔炉等逐个写专门 Screen；
- 通过通用 `AbstractContainerScreen` hook / mixin 挂载伴随栏；
- 服务端负责最终物品移动校验；
- 不绕过 `Slot.mayPlace`、`Slot.mayPickup`、`Slot.getMaxStackSize` 等原规则；
- 不破坏第三方模组自定义 Slot 行为；
- 不破坏现有玩家网格背包界面；
- 不破坏自由窗口、拖拽 ghost、tooltip 层级、RarityCore、Curios 等现有功能。

---

## 3. 非目标

本阶段不做以下事情：

1. 不替换工作台 Screen；
2. 不替换箱子 Screen；
3. 不替换熔炉 Screen；
4. 不替换第三方机器 Screen；
5. 不改变原 Screen 的 `leftPos` / `topPos` / `imageWidth` / `imageHeight`；
6. 不改变原 Screen 的 Slot 坐标；
7. 不重写原 Screen 的按钮、进度条、配方书；
8. 不接管 `CreativeModeInventoryScreen`，至少第一阶段不接管；
9. 不把所有功能方块强行塞进 DF GridInventoryScreen；
10. 不让客户端直接修改真实物品数据。

---

## 4. 总体架构

建议新增一套客户端伴随栏系统：

```text
AbstractContainerScreen
├─ 原版 / 模组自己的 Screen 内容
│  ├─ 背景
│  ├─ Slot
│  ├─ 按钮
│  ├─ 进度条
│  └─ Tooltip
└─ DF Grid Inventory Sidecar Overlay
   ├─ 玩家主网格背包
   ├─ 拖拽 ghost
   ├─ 红绿放置预览
   ├─ 可选：装备储物入口
   └─ 可选：自由窗口系统
```

核心类建议：

```text
client/screen/sidecar/GridInventorySidecarOverlay.java
client/screen/sidecar/GridInventorySidecarManager.java
client/screen/sidecar/SidecarDragSource.java
client/screen/sidecar/SidecarDropTarget.java
client/screen/sidecar/SidecarLayout.java
client/screen/sidecar/SidecarScreenContext.java
```

Mixin / Hook 建议：

```text
mixin/AbstractContainerScreenMixin
mixin/AbstractContainerScreenAccessor
```

服务端同步与移动消息建议：

```text
ServerboundRequestSidecarSyncMessage
ClientboundPlayerGridSidecarSyncMessage
TransferSidecarGridEntryToMenuSlotMessage
TransferMenuSlotToSidecarGridMessage
```

后续阶段可扩展：

```text
TransferSidecarEquipmentEntryToMenuSlotMessage
TransferMenuSlotToSidecarEquipmentStorageMessage
TransferSidecarNestedEntryToMenuSlotMessage
TransferMenuSlotToSidecarNestedGridMessage
```

---

## 5. Screen 挂载规则

伴随栏默认挂载到所有满足条件的 `AbstractContainerScreen`：

- 当前 Screen 是 `AbstractContainerScreen<?>`；
- 当前玩家存在；
- 当前玩家不是旁观者；
- 当前 `menu` 存在；
- 配置启用伴随栏；
- 当前 Screen 不是 DF 自己的 `GridInventoryScreen`；
- 当前 Screen 不是原版创造模式 Screen，第一阶段建议排除；
- 当前 Screen 不在黑名单中。

建议配置：

```text
enableContainerSidecar = true
containerSidecarPreferredSide = RIGHT
containerSidecarAutoAvoidScreen = true
containerSidecarAllowOnCreativeScreen = false
containerSidecarShowEquipmentStorage = false
containerSidecarShowNestedWindows = false
containerSidecarTransfersEnabled = true
```

后续可以增加：

```text
containerSidecarScreenBlacklist = []
containerSidecarScreenWhitelist = []
containerSidecarMenuBlacklist = []
containerSidecarMenuWhitelist = []
```

---

## 6. 布局策略

伴随栏不移动原 Screen，而是尽量寻找 Screen 旁边的空位。

布局输入：

- `screen.width`；
- `screen.height`；
- `leftPos`；
- `topPos`；
- `imageWidth`；
- `imageHeight`；
- 伴随栏期望宽度；
- 伴随栏期望高度。

推荐逻辑：

```text
screenRight = leftPos + imageWidth
screenLeft = leftPos

如果右侧空间足够：
    sidecarLeft = screenRight + gap
否则如果左侧空间足够：
    sidecarLeft = screenLeft - sidecarWidth - gap
否则：
    根据 preferredSide 放在屏幕边缘，但尽量不覆盖原 Screen 主要交互区域
```

高度策略：

- 优先与原 Screen 高度对齐；
- 最小高度必须能显示若干行网格；
- 高度不足时，伴随栏内部滚动；
- 不扩大原 Screen。

---

## 7. 原 Screen Slot 语义

原 Screen 的所有 `Slot` 对伴随栏来说都是“单格目标”。

但这只是交互抽象，不代表修改原 Slot 数据结构。

例如：

| 原 Slot 类型 | 伴随栏视角 | 实际规则来源 |
|---|---|---|
| 箱子槽 | 单格自由槽 | Chest menu / Slot |
| 工作台输入槽 | 单格自由槽 | Crafting menu / Slot |
| 工作台输出槽 | 单格自由槽，但特殊 | ResultSlot / menu |
| 熔炉输入槽 | 单格自由槽 | Furnace input slot |
| 熔炉燃料槽 | 单格自由槽，但限制燃料 | Fuel slot |
| 熔炉输出槽 | 单格自由槽，但特殊 | ResultSlot / XP / recipe |
| 模组机器槽 | 单格自由槽 | 模组自己的 Slot |
| 幽灵槽 / 过滤槽 | 特殊槽 | 模组自己的 Slot |

服务端必须尊重：

```java
slot.isActive();
slot.mayPlace(stack);
slot.mayPickup(player);
slot.getMaxStackSize(stack);
```

并且不能假设所有 Slot 都是普通容器槽。

---

## 8. 数据同步

通用容器 Screen 的 `menu` 不是 `GridInventoryMenu`，因此不能直接从当前 menu 获取玩家网格背包数据。

需要新增 sidecar 同步机制。

### 8.1 客户端请求

当 `GridInventorySidecarOverlay` 创建时，客户端发送：

```text
ServerboundRequestSidecarSyncMessage
```

服务端收到后：

1. 检查玩家不是旁观者；
2. 检查网格背包功能开启；
3. 读取玩家网格背包数据；
4. 刷新 pocket shape；
5. 同步给客户端。

### 8.2 服务端同步

服务端发送：

```text
ClientboundPlayerGridSidecarSyncMessage
```

内容：

- `GridInventoryData`；
- 可选：装备储物数据；
- 可选：折叠状态；
- 可选：revision / version。

### 8.3 更新同步

当伴随栏触发物品移动成功后：

- 服务端保存玩家网格背包；
- `menu.broadcastChanges()`；
- 同步当前 sidecar 数据；
- 必要时同步装备储物数据。

客户端只显示服务端确认后的数据，不直接写真实数据。

---

## 9. 物品移动方向

第一阶段只建议实现两个方向。

### 9.1 Sidecar 网格 -> 当前 Screen Slot

消息：

```text
TransferSidecarGridEntryToMenuSlotMessage
```

字段建议：

```text
int containerId
UUID entryId
int targetSlotIndex
int amount
```

服务端流程：

1. 检查 `player.containerMenu.containerId == packet.containerId`；
2. 检查玩家不是旁观者；
3. 检查 `targetSlotIndex` 合法；
4. 获取玩家网格背包数据；
5. 根据 `entryId` 找到源物品；
6. 获取目标 `Slot`；
7. 检查 `slot.isActive()`；
8. 检查 `slot.mayPlace(movedStack)`；
9. 检查目标堆叠规则；
10. 检查最大堆叠数；
11. 从 gridData 中 extract；
12. 写入目标 slot；
13. `slot.setChanged()`；
14. `menu.broadcastChanges()`；
15. 保存玩家 gridData；
16. 同步 sidecar。

失败时不得修改任何数据。

### 9.2 当前 Screen Slot -> Sidecar 网格

消息：

```text
TransferMenuSlotToSidecarGridMessage
```

字段建议：

```text
int containerId
int sourceSlotIndex
int targetX
int targetY
boolean rotated
boolean targetFolded
int amount
```

服务端流程：

1. 检查 `containerId` 匹配当前 menu；
2. 检查玩家不是旁观者；
3. 检查 `sourceSlotIndex` 合法；
4. 获取源 `Slot`；
5. 检查 `slot.hasItem()`；
6. 检查 `slot.isActive()`；
7. 检查 `slot.mayPickup(player)`；
8. 获取待移动 stack copy；
9. 使用 `GridPlacementValidator.canPlace` 校验目标位置；
10. 从源 slot 安全取出；
11. 写入玩家 gridData；
12. `slot.setChanged()`；
13. `menu.broadcastChanges()`；
14. 保存玩家 gridData；
15. 同步 sidecar。

输出槽、配方结果槽、模组特殊槽必须谨慎处理。

如果无法安全泛化 `ResultSlot`，第一阶段可以仅支持普通可取 Slot，并在文档与完成报告中注明限制。

---

## 10. Vanilla carried stack 冲突

伴随栏拖拽不能和原版鼠标携带物品冲突。

推荐策略：

1. 如果当前 menu 已经有 carried stack，则 sidecar 暂停自定义拖拽；
2. 如果 sidecar 正在拖拽，则尽量阻止原 screen 同时开始 vanilla carried stack 拖拽；
3. 只有鼠标在 sidecar 区域内，或 sidecar 已经处于拖拽状态时，sidecar 才消费输入；
4. 鼠标不在 sidecar 区域且没有 sidecar 拖拽时，输入交给原 screen。

目标：

- 不复制物品；
- 不吞物品；
- 不让原版 carried stack 与 sidecar ghost 同时存在；
- 不破坏原 screen 自己的点击操作。

---

## 11. 客户端交互

### 11.1 从 Sidecar 拖出

流程：

1. 鼠标按下 sidecar 网格物品；
2. 记录 `SidecarDragSource.SIDECAR_GRID_ENTRY`；
3. 显示拖拽 ghost；
4. 鼠标移动到原 Screen Slot；
5. 目标 Slot 高亮；
6. 松手后发送 `TransferSidecarGridEntryToMenuSlotMessage`。

### 11.2 从原 Screen Slot 拖入 Sidecar

流程：

1. 鼠标按下原 Screen Slot；
2. 如果 sidecar 判断可以接管，则记录 `SidecarDragSource.MENU_SLOT`；
3. 显示拖拽 ghost；
4. 鼠标移动到 sidecar grid；
5. 显示红绿放置预览；
6. 松手后发送 `TransferMenuSlotToSidecarGridMessage`。

### 11.3 取消拖拽

以下行为取消 sidecar 拖拽：

- ESC；
- 右键；
- 放到空白区域；
- Screen 关闭；
- 目标无效；
- 服务端拒绝后同步刷新。

---

## 12. 第一阶段功能范围

第一阶段建议只做最小可用闭环：

1. `AbstractContainerScreen` 通用挂载；
2. sidecar 显示玩家主网格背包；
3. sidecar grid -> 当前 Screen 普通 Slot；
4. 当前 Screen 普通 Slot -> sidecar grid；
5. 红绿放置预览；
6. 拖拽 ghost；
7. 服务端同步；
8. 排除 `GridInventoryScreen`；
9. 排除 `CreativeModeInventoryScreen`。

第一阶段暂不做：

- 装备储物伴随栏；
- 自由窗口；
- nested grid；
- Curios；
- 输出槽完整泛化；
- ghost/filter slot 特判；
- 创造栏嵌入。

这样可以先验证最核心的一件事：

> 打开箱子时，旁边出现我的网格背包，可以互相拖东西。

---

## 13. 第二阶段扩展

第二阶段可以加入：

1. sidecar 装备储物区域；
2. sidecar 中打开背包自由窗口；
3. 自由窗口物品与当前 Screen Slot 互通；
4. 当前 Screen Slot 与 sidecar 装备储物互通；
5. Curios 插入与取出；
6. 更完整的 tooltip；
7. 更完整的层级管理。

---

## 14. 第三阶段兼容增强

第三阶段处理复杂 Slot：

1. 工作台输出槽；
2. 熔炉输出槽与经验；
3. 酿造台特殊槽；
4. 附魔台特殊槽；
5. 铁砧输出槽；
6. 第三方机器输出槽；
7. ghost/filter slot；
8. 模组特殊 Slot 黑名单；
9. menu / screen 黑名单和白名单；
10. 更严格的 slot 行为识别。

---

## 15. Screen 排除建议

第一阶段建议排除：

```text
GridInventoryScreen
CreativeModeInventoryScreen
```

可能需要按情况排除：

```text
InventoryScreen
RecipeBook 过度依赖布局的 Screen
纯客户端配置 Screen
特殊地图 / 书本 / 商人类 Screen
```

最终通过配置提供：

- Screen class blacklist；
- Menu type blacklist；
- 手动开关。

---

## 16. 服务端安全规则

所有物品移动必须服务端校验。

必须防止：

- 复制物品；
- 吞物品；
- 非法跨容器移动；
- 非法 slot index；
- 非法 containerId；
- 绕过 slot 限制；
- 绕过网格尺寸限制；
- 绕过套包深度限制；
- 旁观者操作；
- 客户端伪造数据。

每个 sidecar packet 必须检查：

```text
player instanceof ServerPlayer
!player.isSpectator()
player.containerMenu.containerId == packet.containerId
slot index 合法
grid entry 存在
target grid 合法
slot mayPlace / mayPickup 合法
```

失败时不修改数据。

成功后：

```text
slot.setChanged()
menu.broadcastChanges()
保存玩家 gridData
同步 sidecar
```

---

## 17. 客户端层级建议

伴随栏属于当前 Screen 的附加 UI。

层级建议：

1. 原 Screen 背景与 Slot；
2. sidecar 背景；
3. sidecar grid item；
4. sidecar placement preview；
5. sidecar drag ghost；
6. vanilla tooltip；
7. 如果启用自由窗口，自由窗口高于 sidecar；
8. 自由窗口 tooltip 高于自由窗口；
9. 拖拽 ghost 高于普通 UI，但不要使用巨大 z。

注意不要回退近期已经修复的：

- 自由窗口层级；
- 拖拽物品 ghost；
- tooltip 层级；
- 容器装备 tooltip。

---

## 18. 与现有 GridInventoryScreen 的关系

`GridInventoryScreen` 是完整玩家网格背包界面，不需要 sidecar。

Sidecar 只服务于“其他容器 Screen”。

因此 hook 中必须判断：

```text
if screen instanceof GridInventoryScreen -> skip
```

否则会出现：

- 自己界面旁边又挂一个自己；
- 拖拽状态冲突；
- tooltip 层级冲突；
- sidecar 与三栏 UI 重复。

---

## 19. 与创造模式设计的关系

创造模式玩家打开自己的 DF Grid Inventory 时，可以使用另一套设计：

> 右侧栏显示原版创造物品栏镜像页。

但玩家打开工作台、箱子、熔炉时，仍然可以显示 sidecar。

第一阶段建议排除 `CreativeModeInventoryScreen`，避免和原版创造栏冲突。

---

## 20. 测试计划

### 20.1 原版工作台

- 打开工作台；
- sidecar 显示；
- 从 sidecar 拖木板到输入槽；
- 从输入槽拖回 sidecar；
- 原配方书不被遮挡；
- 输出槽不被错误破坏。

### 20.2 原版箱子

- 打开箱子；
- sidecar 显示；
- sidecar -> 箱子成功；
- 箱子 -> sidecar 成功；
- 箱子原本 shift / 点击逻辑不被破坏。

### 20.3 原版熔炉

- sidecar -> 输入槽；
- sidecar -> 燃料槽；
- 非燃料不能放入燃料槽；
- 输入槽 -> sidecar；
- 输出槽第一阶段可谨慎限制；
- 熔炉进度条不被遮挡。

### 20.4 第三方机器

至少找一个带机器 GUI 的模组测试：

- sidecar 显示在旁边；
- 不遮挡机器按钮；
- 不遮挡能量条；
- 不遮挡流体槽；
- 普通物品槽可插入；
- 限制槽不可插入；
- 机器原功能正常。

### 20.5 回归

- DF GridInventoryScreen 不显示 sidecar；
- CreativeModeInventoryScreen 第一阶段不显示 sidecar；
- 自由窗口层级不回退；
- 拖拽 ghost 不消失；
- tooltip 层级不回退；
- RarityCore 正常；
- Curios 正常。

---

## 21. 风险点

### 21.1 输出槽风险

工作台、熔炉、铁砧等输出槽通常有特殊逻辑。

从输出槽取物品时可能需要触发：

- recipe remainder；
- experience；
- stat；
- advancement；
- input shrink；
- machine progress；
- modded onTake hook。

第一阶段不建议强行泛化所有输出槽。

### 21.2 第三方幽灵槽风险

部分机器有 ghost slot / filter slot。

这些槽可能显示物品但不是真实 inventory。

Sidecar 不应该把它们当普通真实槽处理。

### 21.3 carried stack 冲突

Vanilla Screen 已经有自己的鼠标携带物品逻辑。

Sidecar 自己也有拖拽 ghost。

两套拖拽必须避免同时存在。

### 21.4 Mixin 兼容风险

对 `AbstractContainerScreen` 做通用 mixin 影响范围很大。

需要保持注入轻量、可配置、可关闭。

---

## 22. 推荐 Codex 实施顺序

1. 新增 sidecar 文档与配置；
2. 添加 `GridInventorySidecarOverlay` 空面板；
3. 添加 `AbstractContainerScreenMixin`，只做显示测试；
4. 添加 sidecar 数据同步；
5. 显示玩家主 gridData；
6. 实现 sidecar grid 拖拽 ghost；
7. 实现 sidecar grid -> 当前 menu slot；
8. 实现当前 menu slot -> sidecar grid；
9. 增加红绿预览；
10. 增加箱子 / 工作台 / 熔炉测试；
11. 再考虑装备储物和自由窗口。

---

## 23. 一句话总结

这个系统的关键不是“把所有容器改成网格背包”，而是：

> 保留所有原版与模组 Screen，只在旁边挂一个 DF Grid Inventory 伴随栏，并通过服务端安全 packet 让原 Slot 与玩家网格背包互通。

这样才能兼顾：

- 原版兼容；
- 第三方模组兼容；
- 玩家网格背包体验；
- 未来自由窗口与装备储物扩展。
