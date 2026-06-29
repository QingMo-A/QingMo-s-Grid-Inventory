# DF Grid Inventory

DF Grid Inventory 是一个数据驱动的俄罗斯方块式背包模组。不同物品可以占用不同数量的格子，并可在主背包、装备栏、饰品栏、附近物品以及可自由移动的容器窗口之间转移。

## 支持版本

| Minecraft | 加载器 |
| --- | --- |
| 1.20.1 | Forge 47+ |
| 1.21.1 | NeoForge 21+ |

以下模组为可选兼容：

- **Curios**：安装后，背包界面会显示并支持相应饰品槽位。
- **RarityCore**：安装后，网格物品、自由格子和饰品格会显示 RarityCore 提供的稀有度颜色背景及物品装饰。

不安装这些可选模组不会影响 DF Grid Inventory 的基本功能。

## 安装

1. 安装对应 Minecraft 版本的 Forge 或 NeoForge。
2. 将对应版本的模组文件放入游戏实例的 `mods` 文件夹。
3. 如需饰品栏支持，同时安装对应版本的 Curios。
4. 如需物品稀有度背景，同时安装对应版本的 RarityCore。
5. 启动游戏并进入世界。按原版背包键即可打开网格背包界面。

客户端与服务端游玩时应安装相同版本的模组。物品尺寸规则由服务端加载并同步给客户端。

## 背包界面

背包界面包含以下区域：

- **装备栏**：快捷栏、主手、副手、护甲栏以及已安装的饰品栏。
- **格子**：主要网格储物区域。物品会按照自身宽度和高度占用格子。
- **附近物品**：显示玩家附近可拾取的掉落物，可直接拾取或拖入可用区域。
- **创造物品**：仅创造模式玩家可见，可搜索并浏览原版及模组物品。

拖动物品时，绿色预览表示当前位置可以放置，红色预览表示无法放置。支持旋转的非正方形物品可以在拖动期间旋转。

## 容器与自由窗口

带有存储空间的装备可以作为容器使用：

1. 在背包界面单击容器物品，打开对应的自由窗口。
2. 拖动窗口标题栏可以自由移动窗口。
3. 单击窗口可将其置于其他自由窗口上方。
4. 物品可以在主网格、玩家槽位、饰品栏和不同层级的容器窗口之间拖动。
5. 容器内还可以放置其他容器，并继续打开其子容器窗口。

同一个容器实例只会打开一个自由窗口。

## 默认按键

所有按键都可以在 Minecraft 的“控制”设置中修改。

| 按键 | 功能 |
| --- | --- |
| `R` | 拾取面前或附近选中的物品 |
| `V` | 旋转正在拖动的网格物品 |
| `X` | 快速丢弃鼠标悬停的物品 |
| `B` | 折叠或展开正在拖动的背包 |

鼠标右键也可以旋转正在拖动且允许旋转的物品。背包类物品放在要求保持展开的饰品槽位时不会折叠。

## 自定义物品尺寸

物品尺寸通过数据包配置，不需要修改模组，也不需要制作资源包。

### 下载示例包

- [下载 Minecraft 1.20.1 示例数据包](examples/df-grid-item-sizes-example-1.20.1.zip)
- [下载 Minecraft 1.21.1 示例数据包](examples/df-grid-item-sizes-example-1.21.1.zip)

下载对应版本后：

1. 不要解压 ZIP。
2. 将 ZIP 放入 `.minecraft/saves/你的世界/datapacks/`。
3. 进入世界并执行 `/reload`。
4. 使用 `/datapack list enabled` 确认示例包已启用。

示例包会将钻石剑设为 `1×3`、所有船设为 `3×2`，并将 `df_grid_inventory` 的其余物品设为 `2×2`。单个物品规则优先，因此本模组自身已有明确尺寸的物品不会被最后一条通用示例规则覆盖。

### 自己创建数据包

数据包目录结构如下：

```text
你的数据包/
├─ pack.mcmeta
└─ data/
   └─ 你的命名空间/
      └─ df_grid_inventory/
         └─ item_sizes/
            └─ custom_sizes.json
```

`pack.mcmeta` 的 `pack_format` 取决于 Minecraft 版本。建议直接下载上面的对应版本示例包并修改。示例包的可编辑源码也保存在 [`examples/datapacks`](examples/datapacks)。

`custom_sizes.json` 示例：

```json
{
  "rules": [
    {
      "type": "item",
      "target": "minecraft:diamond_sword",
      "width": 1,
      "height": 3,
      "rotatable": true
    },
    {
      "type": "tag",
      "target": "minecraft:boats",
      "width": 3,
      "height": 2,
      "rotatable": true
    },
    {
      "type": "modid",
      "target": "examplemod",
      "width": 2,
      "height": 2,
      "rotatable": false
    }
  ]
}
```

一个 JSON 文件可以包含任意数量的规则，`item_sizes` 目录中也可以放置多个 JSON 文件。文件名可以自定义，但必须使用小写英文字母、数字、下划线或短横线。

### 规则字段

| 字段 | 说明 |
| --- | --- |
| `type` | `item` 指定单个物品，`tag` 指定物品标签，`modid` 指定某个模组的全部物品 |
| `target` | 物品 ID、标签 ID，或模组 ID |
| `width` | 物品占用的格子宽度，必须大于 0 |
| `height` | 物品占用的格子高度，必须大于 0 |
| `rotatable` | 是否允许旋转；省略时为 `false` |

`width` 与 `height` 表示未旋转状态下占用的格子数。例如 `width: 2`、`height: 3` 表示 `2×3`；允许旋转后可以变为 `3×2`。

### 三种匹配方式

指定单个物品：

```json
{
  "type": "item",
  "target": "minecraft:diamond_sword",
  "width": 1,
  "height": 3,
  "rotatable": true
}
```

指定物品标签，标签内的全部物品都会使用该尺寸：

```json
{
  "type": "tag",
  "target": "minecraft:boats",
  "width": 3,
  "height": 2,
  "rotatable": true
}
```

指定某个模组的全部物品。`target` 此时只填写模组 ID，不带冒号：

```json
{
  "type": "modid",
  "target": "examplemod",
  "width": 2,
  "height": 2,
  "rotatable": false
}
```

可以在游戏中打开高级提示框（`F3 + H`），然后将鼠标放在物品上查看物品 ID。ID 通常形如 `minecraft:diamond_sword` 或 `模组ID:物品ID`。

### 优先级与冲突

匹配优先级为：

```text
单个物品（item） > 物品标签（tag） > 整个模组（modid） > 默认尺寸
```

因此可以先用 `modid` 给整个模组设置通用尺寸，再用 `item` 为少数物品单独覆盖。不要为同一个物品编写多条同类型规则；同类型重复规则的结果可能受到资源加载顺序影响。

背包的折叠状态拥有自己的动态尺寸，不受普通物品尺寸规则覆盖。

### 安装与重新加载

单人世界的数据包位置：

```text
.minecraft/saves/世界名称/datapacks/
```

专用服务器的数据包位置：

```text
服务器目录/world/datapacks/
```

安装或修改数据包后执行：

```mcfunction
/reload
```

多人游戏只需由服务端安装尺寸数据包。服务端重新加载后会把规则同步给在线玩家，客户端不应使用另一套尺寸规则。

如果规则没有生效：

1. 使用 `/datapack list enabled` 检查数据包是否启用。
2. 确认 ZIP 打开后最外层直接包含 `pack.mcmeta` 和 `data`，而不是额外套了一层文件夹。
3. 确认路径是 `data/<命名空间>/df_grid_inventory/item_sizes/*.json`。
4. 确认 `target` 使用正确的物品、标签或模组 ID。
5. 查看日志中是否出现 `Failed to load grid item size rules` 或 `Invalid grid item size rule`。

## 新增特殊物品

模组目前包含以下网格物品：

| 物品 | 尺寸 |
| --- | --- |
| 机密文件 | 2×1 |
| 目标定位模块 | 2×2 |
| 盘蛇金像 | 2×2 |
| 花瓶 | 2×2 |
| 盛宴雕塑 | 2×3 |
| 高能燃料 | 2×2 |
| 茶壶 | 2×2 |
| 阵列镜片 | 1×2 |

这些物品默认不可堆叠，并支持旋转。目前可以从创造物品栏的 DF Grid Inventory 分类中获取。

## 开发与构建

在仓库根目录执行：

```powershell
.\gradlew.bat buildAllTargets
```

分别启动开发客户端：

```powershell
.\gradlew.bat runForge1201Client
.\gradlew.bat runNeoForge1211Client
```

开发环境默认不加载 RarityCore。临时启用 RarityCore 并启动客户端：

```powershell
.\gradlew.bat runForge1201Client -Praritycore_runtime=true
.\gradlew.bat runNeoForge1211Client -Praritycore_runtime=true
```

也可以在 `gradle.properties` 中将以下配置改为 `true`，使后续开发运行默认加载 RarityCore：

```properties
raritycore_runtime=true
```

恢复为不加载：

```properties
raritycore_runtime=false
```

启用 RarityCore 后，Gradle 会根据目标版本下载对应文件。该依赖只加入开发运行环境，不会被打包进 DF Grid Inventory 的成品 JAR。

共享源码位于 `common`，版本相关代码位于 `targets/forge-1.20.1` 和 `targets/neoforge-1.21.1`。
