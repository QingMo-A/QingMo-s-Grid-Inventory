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

物品尺寸通过数据包配置，不需要修改模组。数据包内建立以下目录：

```text
你的数据包/
├─ pack.mcmeta
└─ data/
   └─ 你的命名空间/
      └─ df_grid_inventory/
         └─ item_sizes/
            └─ custom_sizes.json
```

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

规则字段：

| 字段 | 说明 |
| --- | --- |
| `type` | `item` 指定单个物品，`tag` 指定物品标签，`modid` 指定某个模组的全部物品 |
| `target` | 物品 ID、标签 ID，或模组 ID |
| `width` | 物品占用的格子宽度，必须大于 0 |
| `height` | 物品占用的格子高度，必须大于 0 |
| `rotatable` | 是否允许旋转；省略时为 `false` |

匹配优先级为：

```text
单个物品（item） > 物品标签（tag） > 整个模组（modid） > 默认尺寸
```

将数据包放入世界存档的 `datapacks` 文件夹，然后执行：

```mcfunction
/reload
```

多人游戏应将尺寸数据包安装在服务端。服务端重新加载后会把规则同步给在线玩家。

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
