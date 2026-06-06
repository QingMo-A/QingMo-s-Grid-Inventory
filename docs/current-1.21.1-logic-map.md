# Current 1.21.1 Logic Map

This document maps the real behavior of the `1.21.1` branch so the target-adapter migration can abstract existing logic into `common` without changing NeoForge behavior.

Plan source: `docs/abstract-current-1211-logic-plan.md`.

Fact source: original files read from `git show 1.21.1:<path>`. This document intentionally records original 1.21.1 behavior before continuing large code migrations.

## 1. Mod Entry And Registration

Source: `src/main/java/com/dreamingfish/gridinventory/DFGridInventoryMod.java`

`DFGridInventoryMod` is a NeoForge mod entrypoint with `MODID = "df_grid_inventory"`.

Constructor registration order:

1. `ModItems.ITEMS.register(modEventBus)`
2. `ModMenus.MENUS.register(modEventBus)`
3. `ModAttachments.ATTACHMENT_TYPES.register(modEventBus)`
4. `ModDataComponents.DATA_COMPONENTS.register(modEventBus)`
5. `ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus)`
6. `modEventBus.addListener(this::commonSetup)`
7. `modEventBus.addListener(ModNetworking::register)`
8. `modEventBus.addListener(ClientEvents::registerKeys)`
9. register client config `GridInventoryClientConfig.SPEC`
10. register server config `GridInventoryConfig.SPEC`
11. `NeoForge.EVENT_BUS.register(this)`
12. `NeoForge.EVENT_BUS.register(ManualPickupEvents.class)`
13. `NeoForge.EVENT_BUS.register(EquipmentStorageEvents.class)`

`commonSetup` enqueues Curios registration for all backpack items and logs that the mod loaded. The Curios registration is part of the 1.21.1 runtime behavior and must be preserved by the NeoForge target even after abstraction.

Reload listener event:

- `GridItemSizeLoader`
- `BackpackFoldingLoader`
- `PlayerPocketDefinitionLoader`
- `EquipmentStorageLoader`

Datapack sync event:

- If `event.getPlayer() != null`, sync item size rules and backpack folding rules to that player.
- Otherwise sync both rule sets to all players.

Client mod event subscriber:

- `RegisterMenuScreensEvent`: delegates to `ClientEvents.registerScreens`.
- `FMLClientSetupEvent`: enqueues `ClientEvents.registerItemProperties`.

Abstract target:

- Common should express registration intent and lifecycle semantics.
- `targets/neoforge-1.21.1` should keep NeoForge event bus wiring and call the common semantic services at the same points.
- `targets/forge-1.20.1` may stub or use Forge-equivalent lifecycle wiring later, but cannot change the NeoForge sequence.

## 2. Content Registration

Sources:

- `common/registry/ModItems.java`
- `common/registry/ModMenus.java`
- `common/registry/ModCreativeTabs.java`

Items are NeoForge `DeferredRegister.Items` entries. All registered items use `new Item.Properties().stacksTo(1)`.

Item ids:

- `small_grid_bag`: `SmallGridBagItem`
- `grid_backpack`: `GridBackpackItem`
- `gray_field_backpack`: `GridBackpackItem`
- `leather_backpack`: `GridBackpackItem`
- `lime_hiking_backpack`: `GridBackpackItem`
- `medium_hiking_backpack`: `GridBackpackItem`
- `military_hiking_backpack`: `GridBackpackItem`
- `tactical_backpack`: `GridBackpackItem`

Menu registration:

- Registry id: `grid_inventory`
- Type: `MenuType<GridInventoryMenu>`
- Constructor factory: NeoForge `IContainerFactory<GridInventoryMenu>` using `GridInventoryMenu::fromNetwork`
- Feature flags: `FeatureFlags.VANILLA_SET`

Creative tab:

- Registry id: `main`
- Title: `itemGroup.df_grid_inventory`
- Icon: `small_grid_bag`
- Display order: small grid bag, grid backpack, gray field backpack, leather backpack, lime hiking backpack, medium hiking backpack, military hiking backpack, tactical backpack.

## 3. Data Components

Source: `common/registry/ModDataComponents.java`

NeoForge 1.21.1 uses `DataComponentType` for ItemStack-owned mod data.

Registered data components:

| id | Java type | persistent codec | network codec |
| --- | --- | --- | --- |
| `grid_inventory` | `GridInventoryData` | `GridInventoryData.CODEC` | `GridInventoryData.STREAM_CODEC` |
| `equipment_storage` | `EquipmentStorageData` | `EquipmentStorageData.CODEC` | `EquipmentStorageData.STREAM_CODEC` |
| `backpack_folded` | `Boolean` | `Codec.BOOL` | boolean `StreamCodec` |

Migration rule: these ids, codecs, and semantics are the NeoForge 1.21.1 baseline. The common semantic layer may not expose `DataComponentType`, but the NeoForge target must keep this storage behavior.

## 4. Player Attachment

Source: `common/registry/ModAttachments.java`

Attachment registry:

- Registry key: `NeoForgeRegistries.Keys.ATTACHMENT_TYPES`
- id: `player_grid_inventory`
- Type: `AttachmentType<GridInventoryData>`
- Default factory: `PlayerPocketDefinitionManager::createInventory`
- Serialization: `GridInventoryData.CODEC`
- Death behavior: `copyOnDeath()`

This is the authoritative 1.21.1 player pocket storage. Any common abstraction must preserve default creation, serialized shape, and copy-on-death semantics.

## 4.1 Player Pocket Definition

Sources:

- `common/inventory/PlayerPocketDefinitionLoader.java`
- `common/inventory/PlayerPocketDefinitionManager.java`

Loader behavior:

- Reload path: `df_grid_inventory/player_pocket`.
- Parses entries as `EquipmentStorageContainerDefinition.CODEC`.
- Sorts resources by `ResourceLocation.toString()`.
- Uses the first valid parsed definition.
- If no valid definition exists, manager definition is set to `null`.
- Logs either default config fallback or loaded definition id.

Manager behavior:

- `createInventory()` creates a `GridInventoryData` from the current definition's resolved columns, rows, and sections.
- If no datapack definition exists, default definition uses config values `POCKET_COLUMNS` and `POCKET_ROWS`, id `pocket`, title `screen.df_grid_inventory.pocket`, and no sections.
- `refreshShape(current)` changes shape only if all existing entries still fit enabled cells in the new shape.
- If dimensions and sections already match, returns `current`.
- If any entry would no longer fit, returns `current` unchanged.

Abstract target:

- Common may keep shape refresh and definition logic.
- Target player data bridge must preserve the attachment default factory calling this manager.

## 5. ItemStack Data Behavior

Sources:

- `common/item/SmallGridBagItem.java`
- `common/item/GridBackpackItem.java`
- `common/registry/ModDataComponents.java`

Small grid bag:

- On use, `ensureData(stack)` creates `grid_inventory` component if absent.
- Server opens `GridInventoryMenu` with source slot based on hand: selected hotbar slot for main hand, `40` for offhand.
- Menu buffer order for bag open: `sourceSlot`, `InteractionHand`, `playerInventory=false`, `GridInventoryData`.
- Tooltip shows `columns x rows` if `grid_inventory` component exists.
- `getData` lazily creates default data, stores it on the stack, and returns a copy.
- `setData` stores a copy.
- Default size comes from `GridInventoryConfig.SMALL_GRID_BAG_COLUMNS` and `SMALL_GRID_BAG_ROWS`.

Backpack:

- Implements Curios `ICurioItem`.
- Can equip only into Curios slot identifier `back`.
- `canEquipFromUse` unfolds the backpack when the target slot is `back`.
- `isFolded` returns false for non-backpacks. For backpacks, missing `backpack_folded` component counts as folded.
- `canFold` returns true when missing equipment storage or all nested equipment storage containers are empty.
- `toggleFolded` unfolds if currently folded; otherwise folds only if `canFold`.
- `unfold` sets `backpack_folded=false`.
- Folded size and roll model are delegated to `BackpackFoldingManager`.

## 6. Player Grid Menu Open Protocol

Source: `common/inventory/PlayerGridInventoryOpener.java`

`PlayerGridInventoryOpener.open(ServerPlayer player)` behavior:

1. Read `player_grid_inventory` attachment.
2. Refresh shape through `PlayerPocketDefinitionManager.refreshShape`.
3. Copy data.
4. Install change listener that writes `data.copy()` back to the player attachment.
5. Initialize equipment storage for `EquipmentSlot.CHEST`.
6. Initialize equipment storage for `EquipmentSlot.LEGS`.
7. Write refreshed `data.copy()` back to the player attachment.
8. Open menu with provider.

Player grid menu buffer order:

1. `writeVarInt(-1)`
2. `writeEnum(InteractionHand.MAIN_HAND)`
3. `writeBoolean(true)`
4. `data.encode(buffer)`

Provider behavior:

- Display name: `container.df_grid_inventory.player_grid_inventory`
- Server menu: `new GridInventoryMenu(containerId, playerInventory, -1, InteractionHand.MAIN_HAND, data.copy(), true)`

This field order is a wire contract for the NeoForge 1.21.1 target.

## 7. Menu Construction And Persistence

Source: `common/menu/GridInventoryMenu.java`

Network constructor:

- `GridInventoryMenu.fromNetwork(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer)`
- Read order: `sourceSlot`, `InteractionHand`, `boolean playerGrid`, `GridInventoryData.decode(buffer)`.

Core fields:

- `Inventory playerInventory`
- `int bagSlot`
- `InteractionHand hand`
- `boolean playerGrid`
- `GridInventoryData gridData`

Persistence:

- Grid data gets a change listener bound to `save()`.
- For player grid, `save()` writes `gridData.copy()` to `ModAttachments.PLAYER_GRID_INVENTORY`.
- For item grid, `save()` writes to `SmallGridBagItem.setData(stack, gridData)`.
- `removed` calls `save()`.

Player-grid slot layout:

- Armor slots are added for inventory indices `39`, `38`, `37`, `36`.
- Offhand slot uses inventory index `40`.
- Hotbar slots use indices `0..8`.

Non-player-grid layout keeps normal inventory rows plus hotbar around the grid bag menu.

Equipment storage persistence:

- Equipment storage is read from or written to `ModDataComponents.EQUIPMENT_STORAGE`.
- Chest and legs use equipped items.
- `EquipmentSlot.BODY` maps to Curios back-slot behavior.
- Saving equipment storage marks player inventory changed and sends `SyncEquipmentStoragePacket`.

## 8. Network Registration And Packet Table

Source: `common/network/ModNetworking.java` and all `common/network/*Packet.java`

Protocol version: `event.registrar("1")`.

Server-to-client packets:

| packet | id | fields/order | handler |
| --- | --- | --- | --- |
| `SyncItemSizeRulesPacket` | `sync_item_size_rules` | `List<GridItemSizeRule> rules` | replace client item size rules |
| `SyncBackpackFoldingRulesPacket` | `sync_backpack_folding_rules` | `List<BackpackFoldingDefinition> rules` | replace client folding rules |
| `SyncGridInventoryPacket` | `sync_grid_inventory` | `GridInventoryData data` | if current menu is `GridInventoryMenu`, `menu.replaceGridData(data)` |
| `SyncEquipmentStoragePacket` | `sync_equipment_storage` | `EquipmentSlot slot`, `EquipmentStorageData storage` | update client equipment storage view |

Client-to-server packets:

| packet | id | fields/order | handler |
| --- | --- | --- | --- |
| `MoveGridEntryPacket` | `move_grid_entry` | `UUID entryId`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.moveEntry(...)` |
| `ToggleGridEntryBackpackFoldPacket` | `toggle_grid_entry_backpack_fold` | `UUID entryId`, `int targetX`, `int targetY`, `boolean rotated` | `menu.toggleGridEntryBackpackFold(...)` |
| `ToggleEquipmentStorageEntryBackpackFoldPacket` | `toggle_equipment_storage_entry_backpack_fold` | `EquipmentSlot slot`, `String containerId`, `UUID entryId`, `int targetX`, `int targetY`, `boolean rotated` | `menu.toggleEquipmentStorageEntryBackpackFold(...)` |
| `InsertFromPlayerInventoryPacket` | `insert_from_player_inventory` | `int playerSlot`, `int targetX`, `int targetY`, `boolean rotated`, `boolean quick`, `boolean targetFolded` | quick insert or `menu.insertFromPlayerInventory(...)` |
| `ExtractToPlayerInventoryPacket` | `extract_to_player_inventory` | `UUID entryId`, `int amount` | `menu.extractToPlayerInventory(...)` |
| `ExtractGridEntryToPlayerSlotPacket` | `extract_grid_entry_to_player_slot` | `UUID entryId`, `int playerSlot`, `int amount` | `menu.extractToPlayerSlot(...)` |
| `OpenPlayerGridInventoryPacket` | `open_player_grid_inventory` | no fields, unit codec | `PlayerGridInventoryOpener.open(player)` |
| `ManualPickupItemPacket` | `manual_pickup_item` | `int entityId` | `ManualPickupHandler.tryPickupToPlayerInventory(...)` |
| `PickupGroundItemIntoGridPacket` | `pickup_ground_item_into_grid` | `int entityId`, `int targetX`, `int targetY`, `boolean rotated` | `menu.pickupGroundItemIntoGrid(...)` |
| `PickupGroundItemIntoEquipmentStoragePacket` | `pickup_ground_item_into_equipment_storage` | `int entityId`, `EquipmentSlot equipmentSlot`, `String containerId`, `int targetX`, `int targetY`, `boolean rotated` | `menu.pickupGroundItemIntoEquipmentStorage(...)` |
| `InsertIntoEquipmentStoragePacket` | `insert_into_equipment_storage` | `int playerSlot`, `EquipmentSlot equipmentSlot`, `String containerId`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.insertFromPlayerIntoEquipmentStorage(...)` |
| `MoveEquipmentStorageEntryPacket` | `move_equipment_storage_entry` | `EquipmentSlot equipmentSlot`, `String containerId`, `UUID entryId`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.moveEquipmentEntry(...)` |
| `ExtractEquipmentStorageEntryPacket` | `extract_equipment_storage_entry` | `EquipmentSlot equipmentSlot`, `String containerId`, `UUID entryId`, `int playerSlot`, `int amount` | `menu.extractEquipmentEntryToPlayerSlot(...)` |
| `TransferGridEntryIntoEquipmentStoragePacket` | `transfer_grid_entry_into_equipment_storage` | `UUID entryId`, `EquipmentSlot equipmentSlot`, `String containerId`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.transferGridEntryIntoEquipmentStorage(...)` |
| `TransferEquipmentStorageEntryIntoGridPacket` | `transfer_equipment_storage_entry_into_grid` | `EquipmentSlot equipmentSlot`, `String containerId`, `UUID entryId`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.transferEquipmentEntryIntoGrid(...)` |
| `TransferEquipmentStorageEntryPacket` | `transfer_equipment_storage_entry` | `EquipmentSlot sourceSlot`, `String sourceContainerId`, `UUID entryId`, `EquipmentSlot targetSlot`, `String targetContainerId`, `int targetX`, `int targetY`, `boolean rotated` | `menu.transferEquipmentEntryBetweenStorages(...)` |
| `MovePlayerFreeSlotPacket` | `move_player_free_slot` | `int sourcePlayerSlot`, `int targetPlayerSlot` | `menu.movePlayerFreeSlot(...)` |
| `QuickEquipGridEntryPacket` | `quick_equip_grid_entry` | `UUID entryId` | `menu.quickEquipGridEntry(...)` |
| `QuickEquipEquipmentStorageEntryPacket` | `quick_equip_equipment_storage_entry` | `EquipmentSlot sourceSlot`, `String containerId`, `UUID entryId` | `menu.quickEquipEquipmentStorageEntry(...)` |
| `QuickEquipPlayerSlotPacket` | `quick_equip_player_slot` | `int playerSlot` | `menu.quickEquipPlayerSlot(...)` |
| `DropGridEntryPacket` | `drop_grid_entry` | `UUID entryId` | `menu.dropGridEntry(...)` |
| `DropEquipmentStorageEntryPacket` | `drop_equipment_storage_entry` | `EquipmentSlot equipmentSlot`, `String containerId`, `UUID entryId` | `menu.dropEquipmentStorageEntry(...)` |
| `InsertPlayerSlotIntoCurioPacket` | `insert_player_slot_into_curio` | `int sourcePlayerSlot`, `String identifier`, `int index` | `menu.insertPlayerSlotIntoCurio(...)` |
| `InsertGridEntryIntoCurioPacket` | `insert_grid_entry_into_curio` | `UUID entryId`, `String identifier`, `int index` | `menu.insertGridEntryIntoCurio(...)` |
| `InsertEquipmentStorageEntryIntoCurioPacket` | `insert_equipment_storage_entry_into_curio` | `EquipmentSlot sourceSlot`, `String containerId`, `UUID entryId`, `String identifier`, `int index` | `menu.insertEquipmentStorageEntryIntoCurio(...)` |
| `ExtractCurioToPlayerSlotPacket` | `extract_curio_to_player_slot` | `String identifier`, `int index`, `int targetPlayerSlot` | `menu.extractCurioToPlayerSlot(...)` |
| `ExtractCurioToGridPacket` | `extract_curio_to_grid` | `String identifier`, `int index`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.extractCurioToGrid(...)` |
| `ExtractCurioToEquipmentStoragePacket` | `extract_curio_to_equipment_storage` | `String identifier`, `int index`, `EquipmentSlot equipmentSlot`, `String containerId`, `int targetX`, `int targetY`, `boolean rotated`, `boolean targetFolded` | `menu.insertCurioIntoEquipmentStorage(...)` |

Registration directions and packet ids are fixed compatibility contracts. Future abstraction must keep the ids, directions, and field order unchanged in `targets/neoforge-1.21.1`.

## 9. Network Sync Helpers

Source: `common/network/ModNetworking.java`

`syncMenu(Player, GridInventoryMenu)`:

- Only sends when player is a `ServerPlayer`.
- Sends `SyncGridInventoryPacket(menu.getGridData().copy())` to that player.

`syncEquipmentStorage(Player, EquipmentSlot, EquipmentStorageData)`:

- Only sends when player is a `ServerPlayer`.
- Sends `SyncEquipmentStoragePacket(slot, storage)` to that player.

Rule sync is triggered by datapack sync event through:

- `GridItemSizeSyncManager.syncTo` / `syncToAll`
- `BackpackFoldingSyncManager.syncTo` / `syncToAll`

## 9.1 Datapack Rule Loaders

Sources:

- `common/size/GridItemSizeLoader.java`
- `common/size/GridItemSizeSyncManager.java`
- `common/folding/BackpackFoldingLoader.java`
- `common/folding/BackpackFoldingSyncManager.java`
- `common/equipment/EquipmentStorageLoader.java`

Item size loader:

- Reload path: `df_grid_inventory/item_sizes`.
- Supports either a root object with `rules` array or a single rule object.
- Parses each rule with `GridItemSizeRule.CODEC`.
- Invalid rules are logged and skipped.
- On completion, calls `GridItemSizeManager.replaceRules(loaded)` and logs count.
- Sync sends `SyncItemSizeRulesPacket(GridItemSizeManager.getRules())` to one player or all players.

Backpack folding loader:

- Reload path: `df_grid_inventory/backpack_folding`.
- Supports either a root object with `rules` array or a single definition object.
- Parses with `BackpackFoldingDefinition.CODEC`.
- Invalid definitions are logged and skipped.
- On completion, calls `BackpackFoldingManager.replaceRules(loaded)` and logs count.
- Sync sends `SyncBackpackFoldingRulesPacket(BackpackFoldingManager.getRules())` to one player or all players.

Equipment storage loader:

- Reload path: `df_grid_inventory/equipment_storage`.
- Supports either a root object with `rules` array or a single definition object.
- Parses with `EquipmentStorageDefinition.CODEC`.
- Invalid definitions are logged and skipped.
- On completion, calls `EquipmentStorageManager.replaceRules(loaded)` and logs count.

Abstract target:

- Common should own parsing and manager replacement semantics where APIs are stable.
- Target network bridge should own actual NeoForge `PacketDistributor` usage while keeping packet ids and data order.

## 10. Equipment Storage

Sources:

- `common/equipment/EquipmentStorageManager.java`
- `common/equipment/EquipmentStorageEvents.java`
- `common/equipment/EquipmentStorageLoader.java`
- `common/menu/GridInventoryMenu.java`

Rules:

- Runtime rules are replaced with `EquipmentStorageManager.replaceRules`.
- Storage is enabled only if `GridInventoryConfig.EQUIPMENT_STORAGE_ENABLED` is true.
- Allowed slots: chest when chest storage config is true, legs when legs storage config is true, and `EquipmentSlot.BODY`.
- Matching priority is item, then tag, then mod id.
- `initializeStorage` reads `equipment_storage` data component.
- Existing non-empty storage is refreshed to the current rule shape if a definition exists.
- Missing storage is created from the matching definition.
- Empty initialized storage is not written.

Player tick behavior:

- Server-side `PlayerTickEvent.Post` initializes equipped storage for chest and legs.
- If initialization creates non-empty storage, player inventory is marked changed.

Menu behavior:

- Equipment storage entry operations save the relevant equipment stack component.
- `EquipmentSlot.BODY` is the Curios-backed back-slot path.
- Saves call equipment sync so the client menu view remains current.

## 11. Manual Pickup

Sources:

- `common/pickup/ManualPickupEvents.java`
- `common/pickup/ManualPickupHandler.java`

Event behavior:

- `ItemEntityPickupEvent.Pre` is cancelled with `TriState.FALSE` when `DISABLE_VANILLA_AUTO_PICKUP` is true and the picker is a server player.

Manual pickup behavior:

- Requires `MANUAL_PICKUP_ENABLED`.
- Entity must be a live item entity in same level.
- Distance must be within `PICKUP_RANGE`.
- If wall pickup is disabled, line of sight must pass.
- First inserts into hotbar `0..8`.
- If survival inventory replacement is enabled, remainder goes into the player pocket grid.
- Otherwise remainder goes into player slots `9..35`.
- If player currently has a player grid menu open, insertion uses that menu data, saves, and syncs menu.
- Otherwise insertion reads attachment, refreshes shape, inserts, and writes attachment if changed.
- Successful pickup discards or shrinks the item entity, broadcasts inventory menu changes, and plays pickup sound.

## 12. Curios Behavior

Source: `common/compat/curios/CuriosIntegration.java`

Curios availability is checked with `ModList.get().isLoaded("curios")`.

Slot collection:

- Reads Curios inventory.
- Keeps visible handlers only.
- Sorts handlers by identifier.
- Keeps active slots only.
- Slot icon is from `CuriosApi.getSlot(identifier, level)` or fallback `curios:slot/empty_curio_slot`.
- Returns `CuriosSlotView(identifier, index, stack.copy(), true, icon)`.

Movement behavior:

- Player slot to Curio requires source slot in player inventory, non-empty source, valid target, empty target.
- Grid entry to Curio validates target and extracts exactly one item from the grid.
- Equipment entry to Curio mirrors grid-entry behavior using the equipment container grid.
- Every item moved into Curios is unfolded through `GridBackpackItem.unfold`.
- Successful moves mark player inventory changed.

Quick equip:

- Requires Curios loaded and non-empty source.
- Iterates visible Curios handlers sorted by identifier, then active empty slots.
- First valid target receives `source.copyWithCount(1)`, unfolded.
- Source shrinks by one.

Curio extraction:

- Curio to player slot requires empty target player slot.
- Curio to grid optionally applies target folded state for backpacks and validates placement.
- Curio to equipment storage follows the menu equipment-storage operation path.

Backpack item Curios behavior:

- Backpack can equip only into `back`.
- Equip-from-use into `back` unfolds first.

## 13. Client Events

Sources:

- `client/ClientEvents.java`
- `client/key/ModKeyMappings.java`

Menu screen:

- `ModMenus.GRID_INVENTORY` uses `GridInventoryScreen`.

Key mappings:

- Category: `key.categories.df_grid_inventory`
- Pickup item: `R`
- Rotate grid item: `V`
- Drop hovered grid item: `X`
- Toggle backpack fold: `B`

Item model properties:

- `df_grid_inventory:folded` is registered for gray field, leather, lime hiking, medium hiking, military hiking, and tactical backpacks.
- `df_grid_inventory:folded_roll` is registered for leather, lime hiking, and medium hiking backpacks.
- `folded` is `1.0` when `GridBackpackItem.isFolded(stack)`.
- `folded_roll` is `1.0` when folded and `GridBackpackItem.usesRollFoldedModel(stack)`.

Runtime client events:

- Client tick: `ClientItemTargeting.tick`, `PickupPromptHud.tick`, `ClientPickupController.tick`.
- GUI post render: `PickupPromptHud.render`.
- Level render stage: `ItemEntityHighlightRenderer.render`.
- Tooltip: if a stack can quick-equip into empty armor slot or Curios slot, add `tooltip.df_grid_inventory.right_click_equip`.

## 13.1 Grid Inventory Screen

Source: `client/screen/GridInventoryScreen.java`

Screen registration uses `GridInventoryScreen::new`.

Construction:

- Extends `AbstractContainerScreen<GridInventoryMenu>`.
- Cell size is `27`.
- Player-grid image width starts at `390`, item-grid image width is `176`.
- Image height starts at `222`.
- Inventory label y is `126`.

Player-grid layout:

- Uses workspace margin from viewport size, bounded by `6..16`.
- Column gap comes from `GridInventoryClientConfig.COLUMN_GAP`.
- Screen width and height expand to available viewport minus margins.
- Creates three columns: equipment column, grid column, nearby-items column.
- Nearby panel columns are constrained by `NEARBY_PANEL_COLUMNS` and available width.
- Vanilla slots are hidden by moving their slot positions far offscreen through `SlotAccessor`.

Item-grid layout:

- Grid is at `leftPos + 16`, `topPos + 18`.
- Nearby panel is placed to the right of the menu.

Rendering:

- Player-grid render draws custom equipment column, grid column, hover, placement previews, equipment previews, and dragged stack ghost.
- Item-grid render draws transparent background, the grid, entries, origin shadow, hover, preview, and dragged stack ghost.
- Player-grid hides vanilla labels and vanilla slot rendering.
- Render method overlays dim background, then renders tooltips, nearby panel, hovered grid entry tooltip, and quick-equip hints.

Network interactions:

- The screen sends the original packet classes directly through `PacketDistributor.sendToServer`.
- Packet sends cover grid moves, player-slot insert/extract, equipment storage moves, transfers, quick equip, drop, folding, ground item pickup, and Curios insert/extract.

Abstract target:

- Common client can keep layout, hit testing, rendering, and semantic screen actions.
- Target client adapter must own loader-specific packet send calls.

## 13.2 Nearby Items Panel

Source: `client/screen/widget/NearbyItemsPanel.java`

Behavior:

- Cell size is `18`.
- Bounds derive from config visible rows, available height, and configured/max columns.
- Rendering is skipped if `SHOW_NEARBY_ITEMS_PANEL` is false.
- Refresh scans item entities near the client player in an inflated bounding box.
- Nearby range uses `NEARBY_ITEMS_RANGE`, clamped to `PICKUP_RANGE` when `SERVER_VALIDATE_NEARBY_RANGE` is true.
- Items are filtered for live non-empty item entities.
- Each view stores entity id, stack copy, distance, grid width, grid height, rotatable flag, and position.
- Views are sorted by distance and packed into the panel grid.
- Hover tooltip shows item name, count, distance, size, and drag hint.
- Left click starts dragging a hovered ground item view.
- Mouse scroll changes row offset.
- `pickupHovered` requests pickup through `ClientPickupController.requestPickup(entityId)`.

Abstract target:

- Common can keep panel packing/rendering semantics.
- Target/client protocol should provide the actual pickup request send path.

## 13.3 Pickup HUD And Highlight

Sources:

- `client/render/PickupPromptHud.java`
- `client/render/ItemEntityHighlightRenderer.java`

Pickup HUD:

- Visible only when no screen is open and `ClientItemTargeting.currentTargetItemEntityId() != -1`.
- Alpha lerps toward visible at speed `0.18` and hidden at speed `0.12`.
- Hidden if `minecraft.options.hideGui`.
- Text key: `hud.df_grid_inventory.pickup_prompt`, with pickup key display as argument.
- Renders centered slightly below crosshair.

Item highlight:

- `ItemEntityHighlightRenderer.render` intentionally does nothing directly.
- Highlighting is routed through `MinecraftMixin.shouldEntityAppearGlowing`, allowing vanilla entity outline shader use.

Abstract target:

- Common can keep HUD alpha/text and target-highlight decision semantics.
- NeoForge target keeps event hook types and mixin injection points.

## 14. Mixin Behavior

Sources:

- `mixin/client/MinecraftMixin.java`
- `mixin/client/InventoryScreenMixin.java`
- `mixin/client/SlotAccessor.java`
- `src/main/resources/df_grid_inventory.mixins.json`

Mixin config:

- Required.
- Java compatibility: `JAVA_21`.
- Client mixins: `client.MinecraftMixin`, `client.InventoryScreenMixin`, `client.SlotAccessor`.

`MinecraftMixin`:

- Injects into `Minecraft.handleKeybinds` at the inventory key check.
- If player is null, game mode is null, player is creative, or `gameMode.isServerControlledInventory()` is true, vanilla behavior remains.
- Otherwise consumes all inventory key clicks.
- For each consumed click, sends `OpenPlayerGridInventoryPacket.INSTANCE` to server.
- Cancels vanilla handling when at least one click was consumed.
- Injects into `shouldEntityAppearGlowing`; if highlight config is enabled and the entity is the currently targeted item entity, returns glowing.

`InventoryScreenMixin`:

- Adds a `NearbyItemsPanel`.
- Renders the panel to the right of the vanilla inventory screen for non-creative players.
- Handles click and release for nearby item dragging.

`SlotAccessor`:

- Mutable accessor for `Slot.x`.
- Mutable accessor for `Slot.y`.

## 15. Abstraction Rounds

The migration should proceed in these rounds, using this map as the behavior contract:

1. Content registration: abstract item/menu/tab registration shape while preserving ids and NeoForge registry behavior.
2. ItemStack data: hide `DataComponentType` behind semantic get/set while preserving component ids and copy behavior.
3. Player data: hide attachment behind semantic player-data bridge while preserving factory, codec, and copy-on-death behavior.
4. Menu opening: abstract `PlayerGridInventoryOpener` and small-bag open data while preserving buffer field order.
5. Network protocol: convert common packet logic to semantic messages while preserving NeoForge ids, directions, field order, and handlers.
6. Equipment storage and sync: abstract storage access and sync semantics without changing chest, legs, and Curios BODY behavior.
7. Curios / accessories: move Curios API use into NeoForge target while preserving slot collection, quick equip, and unfold semantics.
8. Client events and mixins: target-own loader event and mixin registration, common-own reusable UI/client logic.

Forge 1.20.1 may remain stub during these rounds. The NeoForge 1.21.1 target must remain behavior-equivalent to the original `1.21.1` branch after each round.
