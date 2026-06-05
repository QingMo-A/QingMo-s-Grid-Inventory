# NeoForge 1.21.1 逻辑回归与语义桥优化计划

## 1. 背景与当前问题

当前分支：`BridgeDevIn1.20.1`

当前分支已经从早期的 `common + neoforge + fabric` 方向，初步改成了：

```text
common
targets/forge-1.20.1
targets/neoforge-1.21.1
```

这是正确方向。

但是目前仍然存在一个核心问题：

```text
Bridge 层没有正确从原 1.21.1 分支读取并复刻 NeoForge 1.21.1 的真实逻辑。
```

也就是说，现在的代码更像是：

```text
把旧代码迁移到 bridge 结构里，并让它尽量编译。
```

而不是：

```text
先抽取原 1.21.1 分支的行为规格，再让 targets/neoforge-1.21.1 精确实现这些行为。
```

因此出现了：

```text
搭桥之后，NeoForge 端的实际效果和原 1.21.1 分支不一致。
```

本计划专门解决这个问题。

---

## 2. 本轮目标

本轮目标不是继续扩展 Forge 1.20.1，也不是继续新增 target。

本轮目标是：

```text
让 targets/neoforge-1.21.1 成为原 1.21.1 分支行为的等价实现。
```

具体目标：

```text
1. 从原 1.21.1 分支抽取真实逻辑，不再凭猜测补 bridge。
2. 建立完整的 NeoForge 1.21.1 行为基线。
3. 建立完整的 packet 协议字段表。
4. 去掉 NeoForge 协议适配中的反射式 encode/decode。
5. 为每个消息建立显式 codec。
6. 修复 common 中仍然残留的 1.21.1 网络类污染。
7. 让 NeoForge target 的注册顺序、数据同步、菜单打开、Curios、客户端行为尽量恢复到原 1.21.1 分支。
8. Forge 1.20.1 继续允许 stub，不在本轮追求完整可玩。
```

---

## 3. 当前代码中已经发现的问题

### 3.1 Baseline 文档过粗

当前已有：

```text
docs/neoforge-1.21.1-baseline-behavior.md
```

但它现在只是概述级文档。

问题：

```text
1. 没有逐个 packet 写字段顺序。
2. 没有记录原 1.21.1 分支的真实 codec。
3. 没有记录原 handler 的主线程处理方式。
4. 没有记录原 syncMenu / syncEquipmentStorage 的触发位置。
5. 没有记录原 PlayerGridInventoryOpener 的完整流程。
6. 没有记录原 GridInventoryMenu 构造器和 fromNetwork 读取顺序。
7. 没有把“原分支具体类/方法”作为参考来源。
```

因此它不足以指导 NeoForge target 行为回归。

---

### 3.2 NeoForge 协议适配使用反射，不可靠

当前 `NeoForge1211MessageCodecs` 使用反射查找：

```text
encode(FriendlyByteBuf)
decode(FriendlyByteBuf)
encode(RegistryFriendlyByteBuf)
decode(RegistryFriendlyByteBuf)
```

这会带来问题：

```text
1. 字段顺序没有被协议表强制约束。
2. 某个 message 缺 decode 时会走 unit constructor，可能悄悄吞掉字段。
3. FriendlyByteBuf / RegistryFriendlyByteBuf 差异被反射掩盖。
4. 编译期无法发现某个 packet codec 写错。
5. 行为是否等价于原 1.21.1 分支无法保证。
```

本轮必须移除这种反射式 codec。

---

### 3.3 common 仍有 1.21.1 网络污染

当前一些 common message 中仍然出现：

```text
RegistryFriendlyByteBuf
FriendlyByteBuf
```

例如某些 sync message 仍然自己写：

```java
encode(RegistryFriendlyByteBuf buf)
decode(RegistryFriendlyByteBuf buf)
```

这说明 common 还没有完全变成语义层。

正确方向：

```text
common message 只描述字段和 handler。
编码 / 解码由 target 的 protocol codec 层完成。
```

如果短期还需要公共 codec，也应该定义中立的 `GridMessageCodec<T>`，不要直接把 `RegistryFriendlyByteBuf` 放进每个 message。

---

### 3.4 NeoForge target 入口仍有原始 Curios 直连

当前 NeoForge 入口中仍然存在直接：

```java
CuriosApi.registerCurio(...)
```

这说明 accessories 行为还没有完全语义化。

这不一定立刻导致功能错误，但会让 Forge / NeoForge 多版本 target 架构变得不干净。

目标：

```text
NeoForge entrypoint 调用 GridInventoryServices.accessories().registerBackpackAccessories()
NeoForge target 内部用 Curios 实现。
Forge target 内部可以用 Curios Forge 或 no-op。
```

---

### 3.5 NeoForge target 类仍未完全 target 化

当前 `targets/neoforge-1.21.1` 下仍有不少 package 使用：

```text
com.dreamingfish.gridinventory
com.dreamingfish.gridinventory.platform.neoforge
```

而不是统一的：

```text
com.dreamingfish.gridinventory.target.neoforge1211
```

这不是本轮最高优先级，但会造成维护混乱。

本轮优先保证行为等价，package 清理可以作为后续任务。

---

## 4. 本轮总原则

### 4.1 原 1.21.1 分支是事实来源

不要靠猜测修 NeoForge 逻辑。

必须从原 `1.21.1` 分支读取实际代码。

建议使用：

```bash
git show 1.21.1:src/main/java/com/dreamingfish/gridinventory/common/network/ModNetworking.java

git show 1.21.1:src/main/java/com/dreamingfish/gridinventory/common/network/OpenPlayerGridInventoryPacket.java

git show 1.21.1:src/main/java/com/dreamingfish/gridinventory/common/inventory/PlayerGridInventoryOpener.java

git show 1.21.1:src/main/java/com/dreamingfish/gridinventory/common/menu/GridInventoryMenu.java

git show 1.21.1:src/main/java/com/dreamingfish/gridinventory/DFGridInventoryMod.java
```

或者创建只读 worktree：

```bash
git worktree add ../baseline-1.21.1 1.21.1
```

然后从 `../baseline-1.21.1` 抽取真实逻辑。

禁止凭记忆或猜测补字段顺序。

---

### 4.2 先恢复 NeoForge 1.21.1，再完善 Forge 1.20.1

本轮优先级：

```text
1. targets/neoforge-1.21.1 行为等价。
2. common 语义层干净。
3. targets/forge-1.20.1 保持可编译 stub。
```

不要反过来为了 Forge 1.20.1 stub，破坏 NeoForge 1.21.1 原行为。

---

### 4.3 Bridge 必须表达行为语义

错误方向：

```text
Bridge = 把 NeoForge API 包一层
```

正确方向：

```text
Bridge = 把“网格背包行为”映射到对应 target API
```

例如：

```text
open_player_grid_inventory 不是一个 CustomPacketPayload。
它是“客户端请求服务端打开玩家网格背包”的语义。
```

NeoForge 1.21.1 可以把它包装成 CustomPacketPayload。
Forge 1.20.1 可以把它注册成 SimpleChannel message。
common 不应该关心这些细节。

---

## 5. 第一步：重建完整 Baseline 行为文档

更新文件：

```text
docs/neoforge-1.21.1-baseline-behavior.md
```

必须从原 `1.21.1` 分支逐项读取并补全。

### 5.1 入口注册流程

记录原分支：

```text
DFGridInventoryMod 构造函数中：
1. 注册哪些 DeferredRegister。
2. 注册哪些 data component。
3. 注册哪些 attachment。
4. 注册哪些 menu / item / creative tab。
5. 注册哪些网络事件。
6. 注册哪些 config。
7. 注册哪些 NeoForge.EVENT_BUS 事件。
8. commonSetup 中做了什么。
9. Curios 注册了哪些物品。
```

要求写明：

```text
原始类：
原始方法：
新 target 对应类：
是否已经等价：yes/no
```

---

### 5.2 网络包协议表

必须为每个 packet 建立表格。

格式：

```markdown
### open_player_grid_inventory

- 原始类：OpenPlayerGridInventoryPacket
- 新语义类：OpenPlayerGridInventoryMessage
- id：df_grid_inventory:open_player_grid_inventory
- direction：C2S
- 原始 codec：StreamCodec.unit(INSTANCE)
- 字段顺序：无字段
- handler：OpenPlayerGridInventoryPacket.handle
- 主线程：通过 NeoForge payload handler / context 执行，需确认是否 enqueue
- 新 NeoForge adapter：xxx
- 是否行为等价：待确认
```

对于有字段的 packet，必须写：

```text
字段 1：类型、写入方法、读取方法
字段 2：类型、写入方法、读取方法
字段 3：类型、写入方法、读取方法
```

例如：

```text
sourceSlot -> writeVarInt / readVarInt
hand -> writeEnum / readEnum
playerInventory -> writeBoolean / readBoolean
GridInventoryData -> GridInventoryData.encode / GridInventoryData.decode
```

必须覆盖当前 `GridMessages` 中注册的所有消息。

---

### 5.3 菜单打开协议

从原 `1.21.1` 分支读取：

```text
PlayerGridInventoryOpener
GridInventoryMenu
ModMenus
菜单 fromNetwork / 构造器
```

明确记录：

```text
1. 服务端 openMenu 写入字段顺序。
2. 客户端 GridInventoryMenu 读取字段顺序。
3. sourceSlot 的语义。
4. hand 的语义。
5. playerInventory 的语义。
6. GridInventoryData 是否 copy。
7. 打开前是否写回 playerData。
8. change listener 如何设置。
```

---

### 5.4 数据同步协议

记录：

```text
syncGridInventory 原始触发位置
syncEquipmentStorage 原始触发位置
syncItemSizeRules 原始触发位置
syncBackpackFoldingRules 原始触发位置
```

必须说明：

```text
1. 是发送给单个 player 还是 all players。
2. 是否发送 copy。
3. 客户端接收后修改哪个 manager / menu。
4. 是否要求菜单正在打开。
```

---

### 5.5 客户端行为

从原 `1.21.1` 分支读取：

```text
ClientEvents
MinecraftMixin
InventoryScreenMixin
SlotAccessor
NearbyItemsPanel
PickupPromptHud
ItemEntityHighlightRenderer
```

记录：

```text
1. key mapping 注册。
2. E 键拦截条件。
3. 手动拾取按键行为。
4. HUD 渲染时机。
5. tooltip 行为。
6. item property 注册。
7. nearby panel 的渲染和点击行为。
8. item highlight 判断。
```

---

## 6. 第二步：去掉 NeoForge 协议反射 codec

当前文件：

```text
targets/neoforge-1.21.1/src/main/java/.../NeoForge1211MessageCodecs.java
```

当前问题：

```text
使用反射查找 encode/decode。
```

必须改成显式 codec 注册表。

新增或重构：

```text
NeoForge1211MessageCodecs
```

目标结构：

```java
public final class NeoForge1211MessageCodecs {
    private static final Map<GridMessageType<?>, NeoForge1211MessageCodec<?>> CODECS = new LinkedHashMap<>();

    public static void registerAll() {
        register(GridMessages.OPEN_PLAYER_GRID_INVENTORY, unit(OpenPlayerGridInventoryMessage.INSTANCE));
        register(GridMessages.SYNC_GRID_INVENTORY, new NeoForge1211MessageCodec<SyncGridInventoryMessage>() {
            @Override
            public void encode(SyncGridInventoryMessage message, RegistryFriendlyByteBuf buf) {
                message.data().encode(buf);
            }

            @Override
            public SyncGridInventoryMessage decode(RegistryFriendlyByteBuf buf) {
                return new SyncGridInventoryMessage(GridInventoryData.decode(buf));
            }
        });
        ...
    }
}
```

每个 message 必须显式注册 codec。

禁止：

```text
反射调用 encode/decode
反射构造 unit message
用 NoSuchMethodException 判断是否 unit payload
```

原因：

```text
协议必须可审计、可对照 baseline、字段顺序可见。
```

---

## 7. 第三步：common message 只保留语义字段

common message 中不要继续保留：

```text
encode(FriendlyByteBuf)
decode(FriendlyByteBuf)
encode(RegistryFriendlyByteBuf)
decode(RegistryFriendlyByteBuf)
```

除非临时过渡，但本轮目标是迁移到 target codec。

例如：

### 当前不理想

```java
public record SyncGridInventoryMessage(GridInventoryData data) implements GridMessage {
    public void encode(RegistryFriendlyByteBuf buf) {
        data.encode(buf);
    }

    public static SyncGridInventoryMessage decode(RegistryFriendlyByteBuf buf) {
        return new SyncGridInventoryMessage(GridInventoryData.decode(buf));
    }
}
```

### 目标

```java
public record SyncGridInventoryMessage(GridInventoryData data) implements GridMessage {
    public static void handle(SyncGridInventoryMessage message, GridMessageContext context) {
        ...
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_GRID_INVENTORY;
    }
}
```

编码 / 解码移动到：

```text
NeoForge1211MessageCodecs
Forge1201MessageCodecs
```

---

## 8. 第四步：NeoForge adapter 必须逐包对照 baseline

当前 `NeoForge1211ProtocolCompat` 遍历 `GridMessages.REGISTRY.messages()` 自动注册。

自动注册可以保留，但必须满足：

```text
1. 每个 messageType 必须有显式 NeoForge codec。
2. 缺 codec 时直接启动失败，并指出 message id。
3. 不允许缺 codec 后自动 unit constructor。
4. 注册方向必须来自 GridMessages，并和 baseline 一致。
5. handler 必须通过 GridMessageType.handle 调用。
```

建议：

```java
if (!NeoForge1211MessageCodecs.hasCodec(messageType)) {
    throw new IllegalStateException("Missing NeoForge 1.21.1 codec for " + messageType.id());
}
```

这样不会再出现“协议字段漏了但编译还过”的情况。

---

## 9. 第五步：恢复 NeoForge 原始业务入口

当前 NeoForge entrypoint 仍然有一些直接逻辑和部分桥接逻辑混杂。

本轮要求：

```text
1. 先以原 1.21.1 分支为基准恢复行为。
2. 再逐步把直接逻辑移入 bridge。
```

不要为了“看起来更抽象”而破坏行为。

### 9.1 Curios 注册

当前直接：

```java
CuriosApi.registerCurio(...)
```

优化目标：

```java
GridInventoryServices.accessories().registerBackpackAccessories();
```

但必须保证行为等价：

```text
1. 注册的背包物品集合一致。
2. canEquip 只允许 back identifier。
3. canEquipFromUse 时 unfold。
```

如果无法一次迁移，先保留直接逻辑，并在 baseline 文档中标记未迁移。

行为优先于抽象洁癖。

---

### 9.2 DataComponents bootstrap

当前出现：

```java
ModDataComponents.bootstrap();
```

必须确认这是否等价于原 1.21.1 分支的 data component 注册流程。

如果原分支是：

```text
ModDataComponents.DATA_COMPONENTS.register(modEventBus)
```

那么新 target 也必须保持同等注册时机和方式。

不要用空 bootstrap 替代真实注册。

---

### 9.3 Event 注册顺序

必须对照原分支确认：

```text
ManualPickupEvents
EquipmentStorageEvents
reload listeners
datapack sync
ClientEvents
```

是否仍然注册。

若迁移后包名变化，行为也必须保持。

---

## 10. 第六步：PlayerGridInventoryOpener 行为回归

从原 `1.21.1` 分支读取 `PlayerGridInventoryOpener`。

对照当前 common 版本，确认：

```text
1. 读取 playerData 的方式是否等价。
2. 是否 copy。
3. 是否设置 change listener。
4. change listener 中是否触发 syncMenu。
5. 打开菜单前是否 setPlayerGridInventory。
6. EquipmentStorage 初始化是否仍然执行。
7. 打开菜单数据是否和原分支一致。
```

如果当前 common 版本缺少任何一步，必须补回。

---

## 11. 第七步：GridInventoryMenu 行为回归

从原 `1.21.1` 分支读取：

```text
GridInventoryMenu
ModMenus
```

对照当前版本确认：

```text
1. menu constructor 参数是否一致。
2. sourceSlot 语义是否一致。
3. hand 语义是否一致。
4. playerInventory boolean 是否一致。
5. data 是否 copy。
6. slots 构造顺序是否一致。
7. container changed / broadcast changes 行为是否一致。
8. stillValid 是否一致。
9. quickMoveStack 是否一致。
```

菜单行为是 UI 不一致的高风险区域，必须优先检查。

---

## 12. 第八步：ItemStackDataBridge 行为回归

NeoForge 1.21.1 target 应该使用 DataComponentType，并保持原分支行为。

检查：

```text
1. grid_inventory component 是否真实注册。
2. equipment_storage component 是否真实注册。
3. backpack_folded component 是否真实注册。
4. GridBackpackItem / SmallGridBagItem 是否都通过 bridge 读取。
5. 是否出现 old direct stack.set(component) 和 new bridge 混用。
6. 默认值和缺失数据行为是否一致。
```

Forge 1.20.1 可以继续 NBT stub 或最小实现，但不要影响 NeoForge。

---

## 13. 第九步：客户端行为回归

对照原 1.21.1 分支检查：

```text
ClientEvents
MinecraftMixin
InventoryScreenMixin
SlotAccessor
GridInventoryScreen
NearbyItemsPanel
ClientPickupController
ClientItemTargeting
PickupPromptHud
ItemEntityHighlightRenderer
```

重点确认：

```text
1. registerScreens 是否注册同一个 menu / screen。
2. registerKeys 是否和原来一致。
3. clientSetup item properties 是否一致。
4. E 键拦截条件是否一致。
5. Packet 发送是否变成 requestOpenPlayerGridInventory，且行为一致。
6. nearby panel 位置、点击、拖拽释放是否一致。
7. highlightTargetItem config 是否仍然生效。
8. HUD 渲染是否仍然触发。
```

如果客户端行为和原分支不同，必须优先恢复 NeoForge target。

---

## 14. 第十步：Forge 1.20.1 保持可编译 stub

本轮 Forge 1.20.1 不要求完整功能。

要求：

```text
1. Forge target 不引用 NeoForge 类。
2. Forge target 使用 Java 17。
3. Forge target protocol 有 SimpleChannel 结构。
4. 至少 open_player_grid_inventory 可以注册或清晰 stub。
5. 其他未实现功能抛 UnsupportedOperationException，信息包含 message id 或 bridge 名称。
6. 不要为了 Forge stub 修改 NeoForge baseline 行为。
```

---

## 15. 验收检查

### 15.1 编译

运行：

```bash
./gradlew :targets:neoforge-1.21.1:compileJava
./gradlew :targets:forge-1.20.1:compileJava
```

如果模块名使用下划线，以实际模块名为准。

---

### 15.2 common 污染检查

```bash
grep -R "net.neoforged" common/src/main/java
grep -R "net.minecraftforge" common/src/main/java
grep -R "net.fabricmc" common/src/main/java
grep -R "top.theillusivec4.curios" common/src/main/java
grep -R "CustomPacketPayload" common/src/main/java
grep -R "StreamCodec" common/src/main/java
grep -R "RegistryFriendlyByteBuf" common/src/main/java
grep -R "DataComponentType" common/src/main/java
grep -R "SimpleChannel" common/src/main/java
```

目标：无结果。

如果短期仍有 `FriendlyByteBuf`，必须解释为什么保留，以及下一轮如何移除。

---

### 15.3 NeoForge 行为等价检查

至少人工或日志确认：

```text
1. NeoForge target runClient 能启动。
2. 进世界不崩。
3. 按 E 后服务端收到 open_player_grid_inventory。
4. PlayerGridInventoryOpener 被调用。
5. menu open data 字段顺序和 baseline 一致。
6. GridInventoryMenu 客户端构造成功。
7. sync_grid_inventory 能更新打开菜单。
8. sync_equipment_storage 能更新装备储物。
9. datapack reload 后规则同步。
10. Curios 背包仍能装备到 back slot。
```

---

## 16. 禁止行为

本轮禁止：

```text
1. 不要继续实现 Fabric。
2. 不要新增更多 target。
3. 不要把反射 codec 当最终方案。
4. 不要通过 unit constructor 自动吞掉 message 字段。
5. 不要为了编译删除 packet。
6. 不要改 packet id。
7. 不要改菜单注册名。
8. 不要改物品注册名。
9. 不要改 mod_id。
10. 不要改资源路径。
11. 不要让 common 引用 Forge / NeoForge / Fabric / Curios。
12. 不要让 Forge target 引用 NeoForge。
13. 不要让 NeoForge target 引用 Forge。
14. 不要为了 Forge 1.20.1 破坏 NeoForge 1.21.1 baseline。
```

---

## 17. 给 Codex 的执行提示词

可以直接把下面这段发给 Codex：

```text
请阅读 docs/neoforge-1211-logic-restoration-plan.md，并严格按计划执行本轮优化。

当前问题不是 bridge 数量不够，而是 NeoForge 1.21.1 target 没有正确从原 1.21.1 分支读取并复刻真实逻辑。

本轮优先任务：
1. 使用 git show 1.21.1:<path> 或 worktree 读取原 1.21.1 分支真实代码。
2. 补全 docs/neoforge-1.21.1-baseline-behavior.md，使其包含逐 packet 字段顺序、handler、同步时机、菜单打开协议、Curios、client behavior。
3. 移除 NeoForge1211MessageCodecs 中的反射式 encode/decode。
4. 为每个 GridMessage 显式建立 NeoForge 1.21.1 codec，字段顺序必须来自原 1.21.1 分支。
5. common message 不再直接 encode/decode RegistryFriendlyByteBuf / FriendlyByteBuf。
6. NeoForge 1.21.1 target 的协议适配必须保持原 packet id、direction、字段顺序、handler 语义不变。
7. 优先恢复 targets/neoforge-1.21.1 行为等价；Forge 1.20.1 继续允许 stub。

禁止：
- 不要继续实现 Fabric。
- 不要新增更多 target。
- 不要用反射 codec 当最终方案。
- 不要改 packet id / menu id / item id / mod id。
- 不要为了编译删功能。
- 不要让 Forge 1.20.1 的 stub 破坏 NeoForge 1.21.1 原行为。

验收：
./gradlew :targets:neoforge-1.21.1:compileJava
./gradlew :targets:forge-1.20.1:compileJava

并输出：
1. 补全了哪些 baseline 条目。
2. 哪些 message 已改成显式 codec。
3. 哪些 common message 去掉了 buffer encode/decode。
4. NeoForge target 哪些行为已和原 1.21.1 分支对齐。
5. 哪些仍是 TODO。
```

---

## 18. 一句话总结

本轮不是继续“抽象化”。

本轮是：

```text
以原 1.21.1 分支为事实来源，恢复 NeoForge target 的行为等价。
```

只有 NeoForge 1.21.1 target 先和原分支行为一致，Forge 1.20.1 target 才能基于同一套语义正确实现。
