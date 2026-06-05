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

## Client Behavior

- Register grid inventory screen for the target menu.
- Register key mappings for inventory replacement and manual pickup.
- Preserve tooltip rendering for grid inventory and equipment storage.
- Preserve pickup HUD, item highlight rendering, nearby items panel, and item property behavior.
- Preserve folded and folded-roll item property semantics.
