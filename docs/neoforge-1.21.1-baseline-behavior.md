# NeoForge 1.21.1 Baseline Behavior

This document is the behavior-equivalence baseline for the `targets/neoforge-1.21.1` adapter. The target adapter must preserve these semantics when mapping common behavior APIs to NeoForge 1.21.1 APIs.

## Mod Loading Flow

- Register items: all existing backpack and bag item registry names must remain unchanged.
- Register menus: the grid inventory menu registry name and factory semantics must remain unchanged.
- Register creative tabs: the mod creative tab must expose the same item set and ordering.
- Register attachment: player grid inventory attachment is registered only in the NeoForge target.
- Register data components: grid inventory, equipment storage, and backpack folded state are registered only in the NeoForge target.
- Register config: client and server config specs are registered from the NeoForge entrypoint.
- Register network: packet ids, directions, field order, and handlers are registered through the NeoForge protocol compat layer.
- Register events: manual pickup, equipment storage, reload, datapack sync, and client events are registered from the target entrypoint.
- Register reload listeners: item size rules, backpack folding rules, and equipment storage definitions are loaded through reload listeners.
- Register Curios: backpack items are registered as Curios back-slot items during common setup.

## E Key Inventory Replacement

- The vanilla inventory is replaced only outside creative mode.
- Client-side key/open-screen logic must respect game mode and server-controlled inventory state.
- The client sends semantic `open_player_grid_inventory`.
- The server checks `replaceSurvivalInventory`.
- If enabled and the player is a non-creative `ServerPlayer`, the handler calls `PlayerGridInventoryOpener.open`.

## PlayerGridInventoryOpener

- Read `GridInventoryData` from player data.
- Initialize or repair equipment storage before opening.
- Install the change listener used by menu synchronization.
- Write the updated player data back through the player-data bridge.
- Open the menu through the menu bridge with semantic `GridInventoryMenuOpenData`.

## Menu Open Protocol

`GridInventoryMenuOpenData` fields are semantic and ordered:

1. `int sourceSlot`
2. `InteractionHand hand`
3. `boolean playerInventory`
4. `GridInventoryData data`

NeoForge 1.21.1 must write/read these fields in the same order used by the original 1.21.1 branch.

## Network Packet Table

Each semantic message maps to the original id, direction, field order, and handler:

- C2S: `open_player_grid_inventory` -> `OpenPlayerGridInventoryMessage`, unit payload, handler opens player grid inventory on the server main thread.
- C2S: `move_grid_entry`, `move_equipment_storage_entry`, `insert_from_player_inventory`, `extract_to_player_inventory`, `extract_grid_entry_to_player_slot`, `insert_into_equipment_storage`, `transfer_grid_entry_into_equipment_storage`, `transfer_equipment_storage_entry_into_grid`, `manual_pickup_item`, `pickup_ground_item_into_grid`, `pickup_ground_item_into_equipment_storage`, `quick_equip_player_slot`, `quick_equip_grid_entry`, `quick_equip_equipment_storage_entry`, `toggle_grid_entry_backpack_fold`, `toggle_equipment_storage_entry_backpack_fold`.
- C2S Curios/accessory semantics: `insert_player_slot_into_curio`, `insert_grid_entry_into_curio`, `insert_equipment_storage_entry_into_curio`, `extract_curio_to_player_slot`, `extract_curio_to_grid`, `extract_curio_to_equipment_storage`.
- S2C: `sync_grid_inventory`, `sync_equipment_storage`, `sync_item_size_rules`, `sync_backpack_folding_rules`.

For every message, fields are encoded in the order defined by its common semantic message `encode/decode` pair. Client-only handlers remain target-side client registrations.

### Original 1.21.1 References

- Original registration source: `1.21.1:src/main/java/com/dreamingfish/gridinventory/common/network/ModNetworking.java`
- Original entrypoint source: `1.21.1:src/main/java/com/dreamingfish/gridinventory/DFGridInventoryMod.java`
- Original player opener source: `1.21.1:src/main/java/com/dreamingfish/gridinventory/common/inventory/PlayerGridInventoryOpener.java`
- New registration source: `targets/neoforge-1.21.1/.../NeoForge1211ProtocolCompat.java`
- New explicit codec source: `targets/neoforge-1.21.1/.../NeoForge1211MessageCodecs.java`

### Protocol Adapter Status

- `NeoForge1211MessageCodecs` now uses an explicit codec table.
- Reflection-based `encode/decode` lookup has been removed.
- Missing codecs fail fast during payload registration with the missing message id.
- Common semantic messages no longer define packet buffer `encode/decode` methods.
- Packet directions still come from `GridMessages` and match the original NeoForge registration order: sync messages are S2C, menu/actions/accessory messages are C2S.

### Explicit Codec Coverage

- C2S explicit codecs: `drop_equipment_storage_entry`, `drop_grid_entry`, `extract_curio_to_equipment_storage`, `extract_curio_to_grid`, `extract_curio_to_player_slot`, `extract_equipment_storage_entry`, `extract_grid_entry_to_player_slot`, `extract_to_player_inventory`, `insert_equipment_storage_entry_into_curio`, `insert_from_player_inventory`, `insert_grid_entry_into_curio`, `insert_into_equipment_storage`, `insert_player_slot_into_curio`, `manual_pickup_item`, `move_equipment_storage_entry`, `move_grid_entry`, `move_player_free_slot`, `open_player_grid_inventory`, `pickup_ground_item_into_equipment_storage`, `pickup_ground_item_into_grid`, `quick_equip_equipment_storage_entry`, `quick_equip_grid_entry`, `quick_equip_player_slot`, `toggle_equipment_storage_entry_backpack_fold`, `toggle_grid_entry_backpack_fold`, `transfer_equipment_storage_entry_into_grid`, `transfer_equipment_storage_entry`, `transfer_grid_entry_into_equipment_storage`.
- S2C explicit codecs: `sync_backpack_folding_rules`, `sync_equipment_storage`, `sync_grid_inventory`, `sync_item_size_rules`.
- Field order is now visible in `NeoForge1211MessageCodecs` and should be cross-checked against each original packet before marking the row fully behavior-equivalent.

## Data Synchronization

- `syncGridInventory` sends a copied `GridInventoryData` to the player and updates the open menu client state.
- `syncEquipmentStorage` sends equipment slot plus storage data and writes it back to the target item stack.
- `syncItemSizeRules` replaces client item-size rules.
- `syncBackpackFoldingRules` replaces client folding rules.
- Datapack reload sync sends rules to the changed player or all players when globally reloading.

## ItemStack Data

- Grid inventory is stored in the NeoForge `grid_inventory` data component.
- Equipment storage is stored in the NeoForge `equipment_storage` data component.
- Folded state is stored in the NeoForge `backpack_folded` data component.
- Common code accesses these through `GridInventoryItemStackDataBridge`; it must not use `DataComponentType` directly.

## Player Data

- NeoForge attachment stores the player `GridInventoryData`.
- Death clone behavior must copy or preserve data according to original 1.21.1 behavior.
- Login, respawn, and menu sync timings must match the original branch.

## Curios Behavior

- Backpacks equip as Curios `back` slot items.
- `canEquip` returns true only for the `back` identifier.
- `canEquipFromUse` unfolds the backpack before equipping.
- Quick equip behavior moves player/grid/equipment-storage entries into Curios using the same menu methods as the original branch.
- `collectSlots` returns the visible Curios slots for the screen widgets.
- NeoForge entrypoint now calls `GridInventoryServices.accessories().registerBackpackAccessories()`.
- NeoForge target bridge still registers the same seven backpack items and preserves the original `back` slot equip behavior.

## Current Verification

- `./gradlew :targets:neoforge-1.21.1:compileJava` passes.
- `./gradlew :targets:neoforge-1.21.1:runClient` was launched and reached an integrated server session. The latest log shows resource reload, Curios slots/entities loaded, item size/folding/equipment rules loaded, and player `Dev` joined the world.
- `./gradlew :targets:forge-1.20.1:compileJava` is still expected to fail until 1.21-only vanilla APIs in common data/menu/client code are target-adapted.

## Client Behavior

- Register grid inventory screen for the target menu.
- Register key mappings for inventory replacement and manual pickup.
- Preserve tooltip rendering for grid inventory and equipment storage.
- Preserve pickup HUD, item highlight rendering, nearby items panel, and item property behavior.
- Preserve folded and folded-roll item property semantics.
