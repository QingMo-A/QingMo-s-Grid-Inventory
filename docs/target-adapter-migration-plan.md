# Target Adapter 多版本迁移计划

## 1. 当前分支定位

当前分支：`BridgeDevIn1.20.1`

当前分支现状更接近：

```text
common + neoforge + fabric
```

但项目最终目标不是 Fabric 多加载器，而是：

```text
Forge / NeoForge 多版本 Target Adapter 架构
```

目标不是只适配两个固定版本，而是建立可持续扩展的目标环境架构。

```text
Target = Loader + Minecraft Version
```

示例：

```text
forge-1.20.1
forge-1.20.4
neoforge-1.21.1
neoforge-1.21.4
neoforge-1.21.6
neoforge-1.26.1
```

初始阶段只落地：

```text
targets/forge-1.20.1
targets/neoforge-1.21.1
```

---

## 2. 当前分支可取点

当前分支有一些做法值得保留：

```text
1. common 没有直接使用 NeoForge ModDev。
2. neoforge / fabric 通过 sourceSets 引入 common/src/main/java，有 shared source 思路。
3. mixin 已经从 common 迁移到了 loader 模块侧。
4. 已经有 GridInventoryServices / GridInventoryPlatform / NetworkBridge / PlayerDataBridge / RegistryBridge / ConfigBridge / MenuBridge 等基础 bridge。
5. 已经有部分 client logic / SlotPositionAccessor 的公共拆分思路。
```

这些可以作为后续 target 架构的参考。

---

## 3. 当前分支主要问题

当前分支不能直接作为最终主线，主要问题是：

```text
1. 方向是 neoforge + fabric，不是 forge + neoforge 多版本。
2. 没有 targets/forge-1.20.1。
3. 没有 targets/neoforge-1.21.1 这种 target 命名结构。
4. root build.gradle 全局强制 Java 21，不适合 Forge 1.20.1。
5. common packet 仍然 implements CustomPacketPayload，并直接依赖 StreamCodec / RegistryFriendlyByteBuf / CustomPacketPayload。
6. bridge 缺少“行为语义”，导致 NeoForge 端搭桥后的行为和原 1.21.1 分支不完全一致。
7. 当前 bridge 更像 API 转发层，而不是语义适配层。
```

---

## 4. 本轮核心目标

本轮目标：

```text
把 BridgeDevIn1.20.1 分支纠偏为 Forge / NeoForge 多版本 Target Adapter 架构。
```

本轮重点：

```text
1. 移除 Fabric 构建方向。
2. 建立 targets/forge-1.20.1 与 targets/neoforge-1.21.1。
3. 保留 shared source 思路。
4. 建立 NeoForge 1.21.1 baseline 行为文档。
5. 把 bridge 从 API 转发层升级为行为语义层。
6. 把 common packet 从 CustomPacketPayload 中解耦出来。
7. 保证 NeoForge 1.21.1 target 行为尽量等价于原 1.21.1 分支。
```

本轮不追求：

```text
1. Forge 1.20.1 完整可玩。
2. 新增更多 target。
3. 实现 Fabric。
4. 一次性完成全部网络、UI、Curios 兼容。
```

---

## 5. 目标工程结构

目标结构：

```text
root
├─ common
│  └─ src/main/java/com/dreamingfish/gridinventory
│
├─ targets
│  ├─ forge-1.20.1
│  │  └─ src/main/java/com/dreamingfish/gridinventory/target/forge1201
│  │
│  └─ neoforge-1.21.1
│     └─ src/main/java/com/dreamingfish/gridinventory/target/neoforge1211
│
├─ build.gradle
├─ settings.gradle
└─ gradle.properties
```

`settings.gradle` 改为：

```gradle
include("targets:forge-1.20.1")
include("targets:neoforge-1.21.1")
```

如果 Gradle 模块名处理短横线困难，可以使用：

```gradle
include("targets:forge_1_20_1")
include("targets:neoforge_1_21_1")
```

但目录和产物命名必须清晰体现 loader + version。

---

## 6. 停止 Fabric 方向

本轮不继续维护 Fabric。

要求：

```text
1. settings.gradle 不再 include fabric。
2. root build.gradle 不再使用 fabric-loom。
3. fabric 目录可以暂时保留在文件系统中，但不参与构建。
4. 不继续实现 Fabric、Trinkets、fabric.mod.json、fabric mixin。
```

---

## 7. Gradle / Java Toolchain 改造

### root build.gradle

要求：

```text
1. 只保留公共 group、version、repositories、插件 apply false。
2. 不要统一强制 Java 21。
3. 不要保留 fabric-loom 作为当前目标插件。
4. 保留 ForgeGradle / NeoForge ModDev 插件 apply false。
```

### targets/forge-1.20.1/build.gradle

要求：

```text
1. 使用 ForgeGradle。
2. 使用 Forge 1.20.1。
3. Java toolchain 使用 17。
4. 通过 sourceSets 引入 common/src/main/java 和 common/src/main/resources。
5. 产物名建议：df_grid_inventory-forge-1.20.1.jar。
```

### targets/neoforge-1.21.1/build.gradle

要求：

```text
1. 使用 NeoForge ModDev。
2. 使用 NeoForge 1.21.1。
3. Java toolchain 使用 21。
4. 通过 sourceSets 引入 common/src/main/java 和 common/src/main/resources。
5. 产物名建议：df_grid_inventory-neoforge-1.21.1.jar。
```

### common/build.gradle

要求：

```text
1. common 不应用 net.neoforged.moddev。
2. common 不应用 ForgeGradle。
3. common 可以是轻量 java-library，也可以不要求独立 compileJava。
4. common 的主要定位是 shared source，由每个 target 用自己的 MC/loader 依赖重新编译。
5. common 不应作为“只编译一次然后所有版本共用”的唯一公共 jar。
```

---

## 8. shared source 策略

保留当前分支已有的 shared source 思路。

目标：

```gradle
sourceSets.main.java.srcDir rootProject.file("common/src/main/java")
sourceSets.main.resources.srcDir rootProject.file("common/src/main/resources")
```

两个 target 都应该使用自己的 Minecraft / loader 依赖重新编译 common 源码。

原因：

```text
Minecraft 原版 API 在 1.20.1 和 1.21.1 之间也可能变化。
common 不能作为一个只按 1.21.1 编译的 jar 直接给 1.20.1 使用。
```

---

## 9. common 层禁止项

common 禁止 import：

```text
net.minecraftforge.*
net.neoforged.*
net.fabricmc.*
top.theillusivec4.curios.*
```

common 原则上不应该直接出现：

```text
CustomPacketPayload
StreamCodec
RegistryFriendlyByteBuf
PayloadRegistrar
SimpleChannel
NetworkEvent.Context
DataComponentType
Attachment
Capability
```

---

## 10. 建立 NeoForge 1.21.1 baseline 行为文档

新增：

```text
docs/neoforge-1.21.1-baseline-behavior.md
```

用途：

```text
作为 NeoForge 1.21.1 target 的行为等价标准。
避免搭桥后 NeoForge 端行为和原 1.21.1 分支不一致。
```

文档必须记录：

```text
1. 模组加载流程
2. 按 E 打开网格背包行为
3. PlayerGridInventoryOpener 行为
4. 菜单打开协议字段和顺序
5. 网络包表
6. 数据同步行为
7. ItemStack 数据行为
8. Player 数据行为
9. Curios 行为
10. 客户端行为
```

### 网络包表格式建议

每个 packet 记录：

```text
semantic name:
original payload class:
id:
direction:
fields:
original codec / buffer order:
handler:
threading:
client-only:
```

目标：

```text
NeoForge 1.21.1 target 改成 adapter 后，packet id、方向、编码顺序、handler 语义、同步时机必须和原 1.21.1 分支一致。
```

---

## 11. Bridge 语义升级原则

Bridge 不应该只是 API 转发层。

错误方向：

```text
sendPayload
registerPayload
openMenuWithBuffer
getAttachment
setDataComponent
```

正确方向：

```text
requestOpenPlayerGridInventory
openPlayerGridInventory
syncGridInventory
syncEquipmentStorage
syncItemSizeRules
syncBackpackFoldingRules
getGridInventoryFromItem
setGridInventoryToItem
getPlayerGridInventory
setPlayerGridInventory
registerBackpackAccessories
```

原则：

```text
common 只调用语义 API。
target 把语义 API 映射到该版本 / loader 的具体实现。
```

---

## 12. 协议层重构

当前 common packet 仍然 implements CustomPacketPayload，这是错误的。

需要新增：

```text
common/src/main/java/com/dreamingfish/gridinventory/protocol
```

包含：

```text
GridMessage
GridMessageType<T extends GridMessage>
GridMessageDirection
GridMessageHandler<T extends GridMessage>
GridMessageRegistry
GridMessageContext
```

### GridMessage 示例

```java
public interface GridMessage {
    GridMessageType<?> type();
}
```

### GridMessageDirection 示例

```java
public enum GridMessageDirection {
    CLIENT_TO_SERVER,
    SERVER_TO_CLIENT,
    BIDIRECTIONAL
}
```

### GridMessageContext 示例

```java
public interface GridMessageContext {
    Player player();

    ServerPlayer serverPlayerOrNull();

    void enqueueWork(Runnable task);

    void markHandled();
}
```

`GridMessageContext` 不允许暴露 Forge `NetworkEvent.Context` 或 NeoForge `IPayloadContext`。

---

## 13. common packet 改造要求

错误写法：

```java
public record OpenPlayerGridInventoryPacket() implements CustomPacketPayload
```

目标写法：

```java
public record OpenPlayerGridInventoryMessage() implements GridMessage {
    public static final OpenPlayerGridInventoryMessage INSTANCE = new OpenPlayerGridInventoryMessage();

    @Override
    public GridMessageType<?> type() {
        return GridMessages.OPEN_PLAYER_GRID_INVENTORY;
    }

    public static void handle(OpenPlayerGridInventoryMessage message, GridMessageContext context) {
        ...
    }
}
```

common message 禁止：

```text
implements CustomPacketPayload
CustomPacketPayload.Type
StreamCodec
RegistryFriendlyByteBuf
PayloadRegistrar
SimpleChannel
NetworkEvent.Context
```

---

## 14. GridMessages 语义表

新增：

```text
common/src/main/java/com/dreamingfish/gridinventory/protocol/GridMessages.java
```

至少包含：

### C2S

```text
open_player_grid_inventory
move_grid_entry
move_equipment_storage_entry
insert_from_player_inventory
extract_to_player_inventory
extract_grid_entry_to_player_slot
insert_into_equipment_storage
transfer_grid_entry_into_equipment_storage
transfer_equipment_storage_entry_into_grid
manual_pickup_item
pickup_ground_item_into_grid
pickup_ground_item_into_equipment_storage
quick_equip_player_slot
quick_equip_grid_entry
quick_equip_equipment_storage_entry
toggle_grid_entry_backpack_fold
toggle_equipment_storage_entry_backpack_fold
```

### S2C

```text
sync_grid_inventory
sync_equipment_storage
sync_item_size_rules
sync_backpack_folding_rules
```

Curios / accessories 相关消息也要保留语义，但 target 可以 no-op 或由 accessories bridge 处理。

每个消息必须记录：

```text
id
direction
message class
semantic handler
```

---

## 15. NeoForge 1.21.1 Protocol Compat

在 `targets/neoforge-1.21.1` 中新增：

```text
com.dreamingfish.gridinventory.target.neoforge1211.protocol.NeoForge1211ProtocolCompat
com.dreamingfish.gridinventory.target.neoforge1211.protocol.NeoForge1211PayloadAdapters
com.dreamingfish.gridinventory.target.neoforge1211.protocol.NeoForge1211MessageCodecs
com.dreamingfish.gridinventory.target.neoforge1211.protocol.NeoForge1211MessageContext
```

职责：

```text
1. 把 common GridMessage 包装成 NeoForge CustomPacketPayload。
2. 注册 PayloadRegistrar。
3. 保持原 1.21.1 packet id 不变。
4. 保持原 1.21.1 packet direction 不变。
5. 保持原 1.21.1 字段编码顺序不变。
6. 保持原 1.21.1 handler 语义不变。
7. client-only handler 仍然只在 client event 中注册。
8. PacketDistributor / ClientPacketDistributor 只出现在 NeoForge target。
```

---

## 16. Forge 1.20.1 Protocol Stub / SimpleChannel

在 `targets/forge-1.20.1` 中新增：

```text
com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201ProtocolCompat
com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201SimpleChannelBridge
com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201MessageCodecs
com.dreamingfish.gridinventory.target.forge1201.protocol.Forge1201MessageContext
```

Forge 1.20.1 使用 `SimpleChannel`。

本轮允许先 stub 大部分消息，但结构必须正确：

```text
1. SimpleChannel 只在 Forge target 中出现。
2. registerMessage 只在 Forge target 中出现。
3. NetworkEvent.Context 包装成 Forge1201MessageContext。
4. common 不直接引用 NetworkEvent.Context。
5. 先至少实现 open_player_grid_inventory 的 C2S 注册和 handler。
6. 其他消息可以抛出清晰 UnsupportedOperationException 或 TODO，但不要破坏编译。
```

---

## 17. GridNetworkBridge 语义升级

目标接口方向：

```java
public interface GridNetworkBridge {
    void registerMessages();

    void sendToServer(GridMessage message);

    void sendToPlayer(ServerPlayer player, GridMessage message);

    void sendToAllPlayers(GridMessage message);

    default void requestOpenPlayerGridInventory() {
        sendToServer(OpenPlayerGridInventoryMessage.INSTANCE);
    }

    default void syncGridInventory(ServerPlayer player, GridInventoryData data) {
        sendToPlayer(player, new SyncGridInventoryMessage(data.copy()));
    }

    default void syncEquipmentStorage(ServerPlayer player, EquipmentSlot slot, EquipmentStorageData storage) {
        sendToPlayer(player, new SyncEquipmentStorageMessage(slot, storage));
    }
}
```

目标：

```text
common 业务代码尽量调用语义方法，而不是关心底层消息形状。
```

---

## 18. MenuBridge 语义等价

common 中保留或新增：

```text
GridMenuOpenData
```

字段：

```text
int sourceSlot
InteractionHand hand
boolean playerInventory
GridInventoryData data
```

要求：

```text
NeoForge 1.21.1 target 必须按照原 1.21.1 分支的 buffer / payload 字段顺序写入。
Forge 1.20.1 target 用 Forge 1.20.1 的菜单打开方式传输同样语义字段。
```

`PlayerGridInventoryOpener.open(ServerPlayer player)` 只能做：

```text
1. 读取 playerData。
2. 初始化数据。
3. 写回 playerData。
4. GridServices.menus().openPlayerGridInventory(player, openData)。
```

common 中禁止出现：

```text
NetworkHooks
NeoForge openMenu 扩展
FriendlyByteBuf loader 写法
```

---

## 19. ItemStackDataBridge 语义等价

common 只能调用：

```text
GridServices.itemStackData().getGridInventory(stack)
GridServices.itemStackData().setGridInventory(stack, data)
GridServices.itemStackData().getEquipmentStorage(stack)
GridServices.itemStackData().setEquipmentStorage(stack, data)
GridServices.itemStackData().isBackpackFolded(stack)
GridServices.itemStackData().setBackpackFolded(stack, folded)
```

NeoForge 1.21.1：

```text
1. 使用 DataComponentType。
2. DataComponentType 注册只在 neoforge target。
3. 行为必须和原 1.21.1 分支一致。
```

Forge 1.20.1：

```text
1. 使用 ItemStack NBT。
2. key 建议：
   df_grid_inventory:grid_inventory
   df_grid_inventory:equipment_storage
   df_grid_inventory:backpack_folded
3. 使用 GridInventoryData / EquipmentStorageData 的 CODEC 或专用 NBT codec。
4. 失败时记录日志并返回 null / default，不要崩溃。
```

---

## 20. AccessoriesBridge 语义化

当前 NeoForge 入口中直接 `CuriosApi.registerCurio` 的逻辑，需要迁移到 bridge。

common 中 AccessoriesBridge 语义：

```java
public interface GridAccessoriesBridge {
    boolean isLoaded();

    void registerBackpackAccessories();

    List<CuriosSlotView> collectSlots(Player player);

    boolean quickEquip(Player player, ItemStack stack);

    boolean canQuickEquip(Player player, ItemStack stack);
}
```

NeoForge 1.21.1：

```text
使用 Curios。
```

Forge 1.20.1：

```text
可以使用 Forge Curios 或 no-op stub。
如果没有实现，返回 false / List.of()，但不能让 common 崩溃。
```

---

## 21. RegistryBridge 目标

common 的 `ModItems / ModMenus / ModCreativeTabs` 不允许暴露：

```text
DeferredRegister
DeferredHolder
DeferredItem
```

target 自己实现注册。

NeoForge 1.21.1：

```text
使用 NeoForge DeferredRegister。
```

Forge 1.20.1：

```text
使用 Forge DeferredRegister。
```

common 只保留：

```text
Supplier<Item>
Supplier<MenuType<?>>
```

这类中立类型。

---

## 22. Client / Mixin 处理

保留当前分支“mixin 不放 common”的方向。

common/client 可以包含：

```text
1. UI 逻辑
2. tooltip 逻辑
3. HUD 逻辑
4. screen 布局
5. SlotPositionAccessor 公共接口
```

targets/neoforge-1.21.1 包含：

```text
1. NeoForge 1.21.1 mixin
2. NeoForge 1.21.1 mixin json
3. NeoForge client events
```

targets/forge-1.20.1 包含：

```text
1. Forge 1.20.1 mixin
2. Forge 1.20.1 mixin json
3. Forge client events
```

禁止：

```text
1. common 直接加载 mixin。
2. Forge target 使用 NeoForge mixin。
3. NeoForge target 使用 Forge mixin。
```

---

## 23. NeoForge 1.21.1 行为回归要求

完成本轮后，必须优先保证：

```text
targets/neoforge-1.21.1 行为等价于原 1.21.1 分支。
```

验收行为：

```text
1. 模组可启动。
2. 物品注册一致。
3. 菜单注册一致。
4. 按 E 行为一致。
5. 打开网格背包行为一致。
6. 菜单初始数据一致。
7. 拖拽 / 移动 / 插入 / 提取 packet 行为一致。
8. syncGridInventory 行为一致。
9. syncEquipmentStorage 行为一致。
10. backpack folding 行为一致。
11. equipment storage 行为一致。
12. item size datapack reload 后同步一致。
13. folding rules datapack reload 后同步一致。
14. Curios 背包装备行为一致。
15. Client tooltip / HUD / highlight / nearby panel 行为尽量一致。
```

不要因为 bridge 改造让 NeoForge 端效果退化。

---

## 24. 本轮不做的事

本轮不要：

```text
1. 不要继续实现 Fabric。
2. 不要完整实现 Forge 1.20.1 所有功能。
3. 不要新增更多 target。
4. 不要引入 Architectury。
5. 不要使用运行时 if version 判断。
6. 不要改 mod_id。
7. 不要改物品注册名。
8. 不要改菜单注册名。
9. 不要改 packet id。
10. 不要改资源路径。
11. 不要删除核心玩法逻辑。
12. 不要让 common 依赖 Forge / NeoForge / Fabric / Curios。
13. 不要让 Forge target 引用 NeoForge。
14. 不要让 NeoForge target 引用 Forge。
15. 不要把 CustomPacketPayload / SimpleChannel 暴露给 common。
16. 不要把 DataComponentType / Capability / Attachment 暴露给 common。
```

---

## 25. 验收命令

运行：

```bash
./gradlew :targets:neoforge-1.21.1:compileJava
./gradlew :targets:forge-1.20.1:compileJava
```

如果模块名使用下划线，以实际模块名为准。

检查 common：

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

common 下不应该有结果。

检查 target 交叉污染：

```bash
grep -R "net.neoforged" targets/forge-1.20.1/src/main/java
grep -R "net.minecraftforge" targets/neoforge-1.21.1/src/main/java
grep -R "net.fabricmc" targets
```

除注释外不应该有结果。

---

## 26. 输出要求

完成后请输出总结：

```text
1. settings.gradle 改成了什么结构。
2. fabric 是否已经停止参与构建。
3. forge-1.20.1 target 的 Gradle / Java 版本。
4. neoforge-1.21.1 target 的 Gradle / Java 版本。
5. common 是否仍作为 shared source。
6. 新增了哪些 semantic protocol 类。
7. 哪些 common packet 已经去掉 CustomPacketPayload。
8. NeoForge 1.21.1 如何适配 CustomPacketPayload。
9. Forge 1.20.1 哪些 bridge 还是 stub。
10. docs/neoforge-1.21.1-baseline-behavior.md 是否创建。
11. NeoForge 1.21.1 和原 1.21.1 分支相比，哪些行为已确认等价。
12. 哪些 compileJava 命令通过。
13. 哪些问题仍未解决，下一步应该做什么。
```

---

## 27. 一句话原则

```text
Bridge 不是把 NeoForge API 包一层。
Bridge 要表达“网格背包行为语义”。
```

NeoForge 1.21.1 target 的任务是：

```text
严格复刻原 1.21.1 分支的行为。
```

Forge 1.20.1 target 的任务是：

```text
用 Forge 1.20.1 API 实现同一套语义。
```
