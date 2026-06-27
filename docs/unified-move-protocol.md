# Unified Move Protocol

DF Grid Inventory historically used one packet per source/target pair. That made every new inventory surface multiply the number of packet handlers and codecs, and it made bugs easy to introduce: missed folded state, inconsistent nested paths, Optional empty-slot mistakes, and parent/child writeback ordering issues.

The unified move protocol keeps the server authoritative while reducing that path explosion.

## Server Authority

The client sends intent only. It never sends a complete edited inventory or container stack as truth.

Flow:

1. Client sends a source, target, and options.
2. Server verifies the player, menu, source, target, placement, nesting depth, Curios rules, and equipment rules.
3. Server performs the existing transaction.
4. Server syncs the final accepted state.

This avoids duplication, item loss, illegal NBT, bypassed slot restrictions, bypassed nested-backpack depth, and client/server divergence.

## MoveItemMessage

`MoveItemMessage` is the first unified client-to-server entry point:

```java
MoveItemMessage(
    GridItemSource source,
    GridItemTarget target,
    GridMoveOptions options
)
```

In phase 1 it is accepted only while the current menu is `GridInventoryMenu`.

## GridMoveOptions

```java
GridMoveOptions(
    int count,
    boolean rotated,
    boolean targetFolded
)
```

`count <= 0` is normalized by `safeCount()` to `Integer.MAX_VALUE`. In phase 1 most migrated paths still move as much as their existing server transaction permits. `rotated` and `targetFolded` are retained for forward compatibility; current placement targets still carry their own rotated/folded fields, and phase 1 treats the target fields as authoritative.

## Source Type IDs

| ID | Source |
| --- | --- |
| 0 | `GridItemSource.PlayerSlot` |
| 1 | `GridItemSource.MenuGridEntry` |
| 2 | `GridItemSource.EquipmentStorageEntry` |
| 3 | `GridItemSource.NestedGridEntry` |
| 4 | `GridItemSource.NestedEquipmentStorageEntry` |
| 5 | `GridItemSource.AccessorySlot` |

## Target Type IDs

| ID | Target |
| --- | --- |
| 0 | `GridItemTarget.PlayerSlot` |
| 1 | `GridItemTarget.MenuGridPlacement` |
| 2 | `GridItemTarget.EquipmentStoragePlacement` |
| 3 | `GridItemTarget.NestedGridPlacement` |
| 4 | `GridItemTarget.NestedEquipmentStoragePlacement` |
| 5 | `GridItemTarget.AccessorySlot` |

## NestedContainerPath Codec

`GridMoveCodecs` encodes nested paths with an explicit segment count and segment type IDs. Empty paths are legal. The maximum path depth is 16 segments, and strings are capped at 128 characters.

Segment IDs:

| ID | Segment |
| --- | --- |
| 0 | `GridEntrySegment` |
| 1 | `EquipmentEntrySegment` |
| 2 | `ContainerEntrySegment` |
| 3 | `PlayerSlotSegment` |
| 4 | `AccessorySegment` |

Invalid type IDs or excessive path depth throw `DecoderException`, rejecting the packet before it reaches move execution.

## Old Packet Adapter Strategy

Phase 1 does not remove old packets or change their packet IDs/codecs. Selected high-risk nested handlers now adapt their old fields into `GridItemSource`, `GridItemTarget`, and `GridMoveOptions`, then call `GridItemMoveService`.

Converted adapters:

- `TransferGridEntryIntoNestedGridMessage`
- `TransferNestedGridEntryIntoGridMessage`
- `TransferNestedGridEntryIntoNestedGridMessage`
- `TransferEquipmentStorageEntryIntoNestedGridMessage`
- `TransferNestedGridEntryIntoEquipmentStorageMessage`

## Migrated Client Path

The first client path migrated to `MoveItemMessage` is:

- Main grid entry -> nested/free-window grid placement

Other UI paths still send their old packets for now.

## Migration Plan

Phase 1:

- Add `MoveItemMessage`.
- Add stable source/target/options codecs.
- Add `GridItemMoveService` wrapper.
- Convert selected old handlers into adapters.
- Migrate one low-risk client path.

Phase 2:

- Add a shared server-side move-message guard.
- Move more client paths to `MoveItemMessage`.
- Remove duplicated rotated/folded semantics from options or targets once the final shape is chosen.
- Add more diagnostic failure reasons if needed.

Phase 3:

- Remove obsolete old packets after all clients use the unified protocol.
- Extend source/target types for `MenuSlot`, `GroundItem`, `CreativeItem`, and carried-stack style interactions.

## Security Principles

- Never trust client-edited inventory data.
- Never bypass `GridPlacementValidator`.
- Never bypass `NestedBackpackValidator`.
- Never bypass Curios/accessory validation.
- Never bypass vanilla slot `mayPlace`/`mayPickup` rules.
- Failed moves must not mutate source or target state.

## Phase 2 Notes

Phase 2 added `GridMoveMessageGuards` as the shared server-side guard for move-style messages. It checks that the packet is handled for a `ServerPlayer`, rejects spectators, and requires the current menu to be `GridInventoryMenu`. Rejections are logged at debug level with the message name, player name when available, and reason.

`MoveItemMessage` now uses this guard. The following legacy adapter handlers also use it:

- `TransferGridEntryIntoNestedGridMessage`
- `TransferNestedGridEntryIntoGridMessage`
- `TransferNestedGridEntryIntoNestedGridMessage`
- `TransferEquipmentStorageEntryIntoNestedGridMessage`
- `TransferNestedGridEntryIntoEquipmentStorageMessage`

The newly migrated client path is:

- Nested/free-window entry -> main grid placement

The previously migrated client path remains:

- Main grid entry -> nested/free-window grid placement

Legacy packets are still retained for compatibility, fallback, and incremental testing. Their packet ids and wire fields remain unchanged.

`GridItemMoveService` debug logging now includes the menu container id plus full source/target/options values. `GridMoveOptions.count` is still not connected to actual movement quantity control; existing transaction semantics decide the moved amount.

Recommended next phase:

- Migrate Nested -> Nested or Equipment -> Nested to `MoveItemMessage`.
- Continue keeping GroundItem, CreativeItem, and generic MenuSlot outside the core move protocol until their source/target semantics are designed separately.

## Phase 3 Notes

Phase 3 migrated the client-side Nested -> Nested path to `MoveItemMessage`. This covers dragging an entry from a free/nested window into another nested window target, including nested equipment-storage containers when a container id is present.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement

`TransferNestedGridEntryIntoNestedGridMessage` is still retained. Its handler remains an adapter through `GridMoveMessageGuards`, `GridItemSource`, `GridItemTarget`, `GridMoveOptions`, and `GridItemMoveService`. Its packet id, fields, and codecs remain unchanged.

Source dispatch for the migrated path:

- Empty source container id -> `GridItemSource.NestedGridEntry`
- Non-empty source container id -> `GridItemSource.NestedEquipmentStorageEntry`

Target dispatch for the migrated path:

- Empty target container id -> `GridItemTarget.NestedGridPlacement`
- Non-empty target container id -> `GridItemTarget.NestedEquipmentStoragePlacement`

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Nested or Nested -> Equipment to `MoveItemMessage`.

## Phase 4 Notes

Phase 4 migrated the client-side Nested -> Equipment path to `MoveItemMessage`. This covers dragging an entry from a free/nested window into the main UI equipment-storage region.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement

`TransferNestedGridEntryIntoEquipmentStorageMessage` is still retained. Its handler remains an adapter through `GridMoveMessageGuards`, `GridItemSource`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`. Its packet id, fields, and codecs remain unchanged.

Source dispatch for the migrated path:

- Empty source container id -> `GridItemSource.NestedGridEntry`
- Non-empty source container id -> `GridItemSource.NestedEquipmentStorageEntry`

The target is always `GridItemTarget.EquipmentStoragePlacement`, built from the equipment region slot, container id, `targetRegionX`, `targetRegionY`, `rotatedPreview`, and the dragged backpack folded state. `targetRegionX` and `targetRegionY` still use the existing anchor calculation.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Nested to `MoveItemMessage`.

## Phase 5 Notes

Phase 5 migrated the client-side Equipment -> Nested path to `MoveItemMessage`. This covers dragging an entry from an equipment-storage region into a free/nested window target, including nested equipment-storage containers when a target container id is present.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement

`TransferEquipmentStorageEntryIntoNestedGridMessage` is still retained. Its handler remains an adapter through `GridMoveMessageGuards`, `GridItemSource.EquipmentStorageEntry`, `GridItemTarget`, `GridMoveOptions`, and `GridItemMoveService`. Its packet id, fields, and codecs remain unchanged.

The source is always `GridItemSource.EquipmentStorageEntry`, built from the equipment region slot, source container id, and dragged entry id.

Target dispatch for the migrated path:

- Empty target container id -> `GridItemTarget.NestedGridPlacement`
- Non-empty target container id -> `GridItemTarget.NestedEquipmentStoragePlacement`

The target coordinates still use `target.cellX() - anchorCellX(draggedStack())` and `target.cellY() - anchorCellY(draggedStack())`. `rotatedPreview` and the dragged backpack folded state keep the same semantics as the old packet path.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Grid or Grid -> Equipment to `MoveItemMessage`.

## Phase 6 Notes

Phase 6 migrated the client-side Grid -> Equipment path to `MoveItemMessage`. This covers dragging an entry from the main grid into an equipment-storage region.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement

`TransferGridEntryIntoEquipmentStorageMessage` is still retained. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.MenuGridEntry`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`. Its packet id, fields, and codecs remain unchanged.

The source is always `GridItemSource.MenuGridEntry`, built from the dragged grid entry id.

The target is always `GridItemTarget.EquipmentStoragePlacement`, built from the equipment region slot, container id, `targetRegionX`, `targetRegionY`, `rotatedPreview`, and the dragged backpack folded state.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Grid or Equipment -> Equipment to `MoveItemMessage`.

## Phase 7 Notes

Phase 7 migrated the client-side Equipment -> Grid path to `MoveItemMessage`. This covers dragging an entry from an equipment-storage region back into the main grid.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement

`TransferEquipmentStorageEntryIntoGridMessage` is still retained. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.EquipmentStorageEntry`, `GridItemTarget.MenuGridPlacement`, `GridMoveOptions`, and `GridItemMoveService`. Its packet id, fields, and codecs remain unchanged.

The source is always `GridItemSource.EquipmentStorageEntry`, built from the source equipment slot, source container id, and dragged entry id.

The target is always `GridItemTarget.MenuGridPlacement`, built from `targetGridX`, `targetGridY`, `rotatedPreview`, and the dragged backpack folded state.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Equipment to `MoveItemMessage`.

## Phase 8 Notes

Phase 8 did not migrate any new client path and did not change the unified move protocol. It only extracted `GridInventoryScreen` helper methods for constructing client-side `GridItemSource`, `GridItemTarget`, `GridMoveOptions`, and `MoveItemMessage` sends.

`MoveItemMessage` coverage remains unchanged:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement

The refactored client paths now use helper methods for menu grid, nested/free-window, and equipment-storage source/target construction. Behavior should be equivalent: branch order, release flags, sound calls, drag cleanup, and early returns remain unchanged.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Equipment to `MoveItemMessage`, or start PlayerSlot -> Grid / PlayerSlot -> Nested after defining their source and target semantics.

## Phase 9 Notes

Phase 9 migrated the client-side Equipment -> Equipment path to `MoveItemMessage`. This covers both moving inside the same equipment-storage container and transferring between different equipment-storage containers.

The client no longer chooses between same-storage and cross-storage packets for this path. It now sends one intent:

- `GridItemSource.EquipmentStorageEntry`
- `GridItemTarget.EquipmentStoragePlacement`

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement

`MoveEquipmentStorageEntryMessage` and `TransferEquipmentStorageEntryMessage` are still retained. Their handlers remain adapters through `GridMoveMessageGuards`, `GridItemSource.EquipmentStorageEntry`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`. Their packet ids, fields, and codecs remain unchanged.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Recommended next phase:

- Start PlayerSlot -> Grid / PlayerSlot -> Equipment / PlayerSlot -> Nested, or first document regression tests for the unified move protocol.

## Phase 10 Notes

Phase 10 migrated the client-side PlayerSlot -> Grid path to `MoveItemMessage`. This covers dragging an item from the vanilla player inventory or hotbar into the main grid.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement

The migrated client path uses:

- `GridItemSource.PlayerSlot`
- `GridItemTarget.MenuGridPlacement`

`InsertFromPlayerInventoryMessage` is still retained. Its normal drag handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.PlayerSlot`, `GridItemTarget.MenuGridPlacement`, `GridMoveOptions`, and `GridItemMoveService`. Its quick-insert branch remains on the legacy quick path because that extra semantic is not represented by `GridMoveOptions` yet.

PlayerSlot -> Nested, PlayerSlot -> Equipment, PlayerSlot -> Curio, and PlayerSlot -> PlayerSlot still use their old packets.

The PlayerSlot source resolver now checks that the source is a free player slot and that the backing slot view allows `mayPickup`.

`GridMoveOptions.count` is still reserved for later phases and does not yet control move quantity.

Phase 10 follow-up fixed PlayerSlot -> Grid behavior parity with the legacy `InsertFromPlayerInventoryMessage` path:

- PlayerSlot -> MenuGridPlacement can merge into an existing same-item grid entry when grid item stacking is enabled.
- PlayerSlot -> MenuGridPlacement moves only one item when `GridStackMerger.itemsStackableInGrid()` is false.
- `ResolvedItemRef.removeFromRootCopy(amount)` now shrinks `entryId == null` root stacks by the requested amount instead of always clearing the whole stack.
- The client still sends `MoveItemMessage` for PlayerSlot -> Grid.
- `InsertFromPlayerInventoryMessage` remains registered, and `quick=true` still uses `quickInsertFromPlayerInventory`.

Recommended next phase:

- Migrate PlayerSlot -> Nested or PlayerSlot -> Equipment to `MoveItemMessage`.

## Phase 11 Notes

Phase 11 migrated the client-side PlayerSlot -> Nested path to `MoveItemMessage`. When a player inventory or hotbar item is released over a nested/free-window grid, the client now sends:

- `GridItemSource.PlayerSlot`
- `GridItemTarget.NestedGridPlacement` when the target container id is empty
- `GridItemTarget.NestedEquipmentStoragePlacement` when the target container id is non-empty

The existing `playerSlotSource` and `nestedPlacementTarget` helpers construct the source and target. The latter preserves the original anchor-cell offset, rotation, folded state, owner path, and equipment-storage container routing. The nested-target branch also preserves its immediate release sound, drag-state cleanup, and early return.

`InsertPlayerSlotIntoNestedGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.PlayerSlot`, the appropriate nested target type, `GridMoveOptions`, and `GridItemMoveService`.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement

PlayerSlot -> Equipment, PlayerSlot -> Curio, and PlayerSlot -> PlayerSlot still use their old packets. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate PlayerSlot -> Equipment to `MoveItemMessage`.

## Phase 12 Notes

Phase 12 migrated the client-side PlayerSlot -> Equipment path to `MoveItemMessage`. When a player inventory or hotbar item is released over an equipment-storage region, the client now sends:

- `GridItemSource.PlayerSlot`
- `GridItemTarget.EquipmentStoragePlacement`

The existing `playerSlotSource` and `equipmentPlacementTarget` helpers construct the source and target. The target helper preserves the original `targetRegionX` / `targetRegionY` anchor calculations, equipment slot, container id, rotation, and folded state. The original `released = true`, `equipped = true`, release sound, drag cleanup, and return timing remain unchanged.

`InsertIntoEquipmentStorageMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.PlayerSlot`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`.

The unified transaction preserves the legacy PlayerSlot -> Equipment behavior: when grid item stacking is disabled, only one item is moved; the path does not merge into an existing entry; and moving an equipment slot into that same equipment slot's storage is rejected. Missing equipment storage is not initialized.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement

PlayerSlot -> Curio and PlayerSlot -> PlayerSlot still use their old packets. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Phase 12 follow-up fixed the PlayerSlot -> Equipment self-storage guard. The guard no longer relies on `sameRoot`, because player-slot and equipment-slot roots use different root kinds. `GridInventoryMenu.transactionIsEquipmentPlayerSlot` now reuses the existing equipment-slot mapping to reject:

- Player slot 39 -> HEAD storage
- Player slot 38 -> CHEST storage
- Player slot 37 -> LEGS storage
- Player slot 36 -> FEET storage

The client continues to use `MoveItemMessage`, and `InsertIntoEquipmentStorageMessage` remains an adapter. Packet ids, codecs, and save formats are unchanged.

Recommended next phase:

- Start Curio -> Grid / Nested / Equipment migration, or first consolidate PlayerSlot migration regression tests.

## Phase 13 Notes

Phase 13 migrated the client-side Curio -> Grid path to `MoveItemMessage`. When an accessory item is released over the main grid, the client now sends:

- `GridItemSource.AccessorySlot`
- `GridItemTarget.MenuGridPlacement`

The new `curioDragSource` helper constructs the source from the dragged accessory view's identifier and index. The existing `menuGridPlacementTarget` helper preserves `targetGridX` / `targetGridY`, rotation, and folded state. Release, unequip sound, drag cleanup, and return timing remain unchanged.

`ExtractCurioToGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.AccessorySlot`, `GridItemTarget.MenuGridPlacement`, `GridMoveOptions`, and `GridItemMoveService`.

The transaction now preserves the legacy player-grid-only restriction specifically for AccessorySlot -> MenuGridPlacement. Accessory source snapshots and writes continue through the platform accessory bridge, so an unavailable optional integration resolves no source. The current bridge does not expose a Curios removal-permission check; this matches the legacy Curio -> Grid implementation, which also read and cleared the dynamic slot directly.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement

Curio -> Nested, Curio -> Equipment, and Curio -> PlayerSlot still use their old packets. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate Curio -> Nested or Curio -> Equipment to `MoveItemMessage`.

## Phase 14 Notes

Phase 14 migrated the client-side Curio -> Nested path to `MoveItemMessage`. When an accessory item is released over a nested/free-window grid, the client now sends:

- `GridItemSource.AccessorySlot`
- `GridItemTarget.NestedGridPlacement` when the target container id is empty
- `GridItemTarget.NestedEquipmentStoragePlacement` when the target container id is non-empty

The existing `curioDragSource` and `nestedPlacementTarget` helpers construct the source and target. They preserve the accessory identifier/index, owner path, container routing, anchor-cell offset, rotation, and folded state. The original `released` and `unequipped` semantics, window hit testing, release sound, drag cleanup, and return timing remain unchanged.

`ExtractCurioToNestedGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.AccessorySlot`, the appropriate nested target type, `GridMoveOptions`, and `GridItemMoveService`.

The AccessorySlot player-grid-only guard now covers MenuGridPlacement, NestedGridPlacement, and NestedEquipmentStoragePlacement. It does not affect accessory moves to player slots or top-level equipment storage.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement

Curio -> Equipment and Curio -> PlayerSlot still use their old packets. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate Curio -> Equipment to `MoveItemMessage`.

## Phase 15 Notes

Phase 15 migrated the client-side Curio -> Equipment path to `MoveItemMessage`. When an accessory item is released over an equipment-storage region, the client now sends:

- `GridItemSource.AccessorySlot`
- `GridItemTarget.EquipmentStoragePlacement`

The existing `curioDragSource` and `equipmentPlacementTarget` helpers construct the source and target. They preserve the accessory identifier/index, equipment slot, container id, `targetRegionX` / `targetRegionY` anchor calculations, rotation, and folded state. The original `released` and `unequipped` semantics, release sound, drag cleanup, and return timing remain unchanged.

`ExtractCurioToEquipmentStorageMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.AccessorySlot`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`.

The AccessorySlot player-grid-only guard now also covers EquipmentStoragePlacement. The transaction also preserves the legacy self-storage guard that rejects accessory `back:0` -> BACK equipment storage. Successful equipment-root commits continue through `transactionReplaceEquipmentStack`, which writes the equipped stack, marks the player inventory changed, and synchronizes equipment storage.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement
- Curio/accessory slot -> equipment-storage placement

Curio -> PlayerSlot still uses its old packet. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate Curio -> PlayerSlot, or begin the reverse Grid / Equipment / PlayerSlot -> Curio paths.

## Phase 16 Notes

Phase 16 migrated the client-side Curio -> PlayerSlot path to `MoveItemMessage`. When an accessory item is released over a vanilla player slot, the client now sends:

- `GridItemSource.AccessorySlot`
- `GridItemTarget.PlayerSlot`

The existing `curioDragSource` helper constructs the source, and the hovered slot index constructs the player-slot target. The original `released` and `unequipped` semantics, release sound, drag cleanup, and return timing remain unchanged.

`ExtractCurioToPlayerSlotMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.AccessorySlot`, `GridItemTarget.PlayerSlot`, default `GridMoveOptions`, and `GridItemMoveService`.

Because the general transaction flow resolves grid-placement targets, AccessorySlot -> PlayerSlot uses a minimal early branch in `GridItemTransferService`. That branch requires a player-grid menu and delegates to `GridInventoryMenu.extractCurioToPlayerSlot`, preserving the legacy free-slot check and platform accessory bridge behavior without extending `ResolvedGridRef`.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement
- Curio/accessory slot -> equipment-storage placement
- Curio/accessory slot -> player slot

Grid -> Curio, Equipment -> Curio, and PlayerSlot -> Curio still use their old packets. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Begin the reverse Curio insertion paths: Grid -> Curio, Equipment -> Curio, or PlayerSlot -> Curio.

## Phase 17 Notes

Phase 17 migrated the client-side Grid -> Curio path to `MoveItemMessage`. When a main-grid entry is released over an accessory slot, the client now sends:

- `GridItemSource.MenuGridEntry`
- `GridItemTarget.AccessorySlot`

The existing `gridDragSource` helper constructs the source, and the hovered accessory view supplies the identifier and index for the target. The original equip sound, drag cleanup, and immediate return timing remain unchanged.

`InsertGridEntryIntoCurioMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.MenuGridEntry`, `GridItemTarget.AccessorySlot`, default `GridMoveOptions`, and `GridItemMoveService`.

Because the general transaction flow resolves grid-placement targets, MenuGridEntry -> AccessorySlot uses a minimal early branch in `GridItemTransferService`. That branch requires a player-grid menu and delegates to `GridInventoryMenu.insertGridEntryIntoCurio`, preserving the legacy accessory bridge movement and menu-grid save behavior without extending `ResolvedGridRef`.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement
- Curio/accessory slot -> equipment-storage placement
- Curio/accessory slot -> player slot
- Main grid entry -> Curio/accessory slot

Equipment -> Curio and PlayerSlot -> Curio still use their old packets. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate Equipment -> Curio or PlayerSlot -> Curio to `MoveItemMessage`.

## Phase 18 Notes

Phase 18 migrated the client-side Equipment -> Curio path to `MoveItemMessage`. When an equipment-storage entry is released over an accessory slot, the client now sends:

- `GridItemSource.EquipmentStorageEntry`
- `GridItemTarget.AccessorySlot`

The existing `equipmentDragSource` helper constructs the source, and the hovered accessory view supplies the identifier and index for the target. The original equip sound, drag cleanup, and immediate return timing remain unchanged.

`InsertEquipmentStorageEntryIntoCurioMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.EquipmentStorageEntry`, `GridItemTarget.AccessorySlot`, default `GridMoveOptions`, and `GridItemMoveService`.

EquipmentStorageEntry -> AccessorySlot uses a minimal early branch in `GridItemTransferService`. That branch requires a player-grid menu and delegates to `GridInventoryMenu.insertEquipmentStorageEntryIntoCurio`, preserving the legacy accessory bridge movement and equipment-storage save behavior without extending `ResolvedGridRef`.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement
- Curio/accessory slot -> equipment-storage placement
- Curio/accessory slot -> player slot
- Main grid entry -> Curio/accessory slot
- Equipment-storage entry -> Curio/accessory slot

PlayerSlot -> Curio still uses its old packet. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate PlayerSlot -> Curio to `MoveItemMessage`.

## Phase 19 Notes

Phase 19 migrated the client-side PlayerSlot -> Curio path to `MoveItemMessage`. When a vanilla player-slot item is released over an accessory slot, the client now sends:

- `GridItemSource.PlayerSlot`
- `GridItemTarget.AccessorySlot`

The existing `playerSlotSource` helper constructs the source, and the hovered accessory view supplies the identifier and index for the target. The original equip sound, drag cleanup, and immediate return timing remain unchanged.

`InsertPlayerSlotIntoCurioMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.PlayerSlot`, `GridItemTarget.AccessorySlot`, default `GridMoveOptions`, and `GridItemMoveService`.

PlayerSlot -> AccessorySlot uses a minimal early branch in `GridItemTransferService`. That branch requires a player-grid menu and delegates to `GridInventoryMenu.insertPlayerSlotIntoCurio`, preserving the legacy accessory bridge behavior without adding a new free-slot restriction or extending `ResolvedGridRef`.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement
- Curio/accessory slot -> equipment-storage placement
- Curio/accessory slot -> player slot
- Main grid entry -> Curio/accessory slot
- Equipment-storage entry -> Curio/accessory slot
- Player slot -> Curio/accessory slot

`GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Begin GroundItem, CreativeItem, or MenuSlot migration, or first consolidate unified-move regression tests.

## Phase 20 Notes

Phase 20 migrated the client-side PlayerSlot -> PlayerSlot path to `MoveItemMessage`. When a vanilla player-slot item is released over another player slot, the client now sends:

- `GridItemSource.PlayerSlot`
- `GridItemTarget.PlayerSlot`

The existing `playerSlotSource` helper constructs the source, and the hovered slot index constructs the target. The original release flag, release sound, drag cleanup, and return timing remain unchanged.

`MovePlayerFreeSlotMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.PlayerSlot`, `GridItemTarget.PlayerSlot`, `GridMoveOptions`, and `GridItemMoveService`.

`GridItemMoveService` now passes its normalized `GridMoveOptions` into a new four-argument `GridItemTransferService.transfer` overload. The existing three-argument overload remains and delegates with default options.

PlayerSlot -> PlayerSlot uses a minimal early branch in `GridItemTransferService`. It requires a player-grid menu and delegates to `GridInventoryMenu.movePlayerFreeSlot`, passing `options.targetFolded()`. This preserves the legacy free-slot, slot-view, `mayPickup`, `mayPlace`, folding, stacking, and swapping behavior without extending `ResolvedGridRef`.

`MoveItemMessage` now covers these client paths:

- Main grid entry -> nested/free-window placement
- Nested/free-window entry -> main grid placement
- Nested/free-window entry -> nested/free-window placement
- Nested/free-window entry -> equipment-storage placement
- Equipment-storage entry -> nested/free-window placement
- Main grid entry -> equipment-storage placement
- Equipment-storage entry -> main grid placement
- Equipment-storage entry -> equipment-storage placement
- Player slot -> main grid placement
- Player slot -> nested/free-window placement
- Player slot -> equipment-storage placement
- Player slot -> player slot
- Curio/accessory slot -> main grid placement
- Curio/accessory slot -> nested/free-window placement
- Curio/accessory slot -> equipment-storage placement
- Curio/accessory slot -> player slot
- Main grid entry -> Curio/accessory slot
- Equipment-storage entry -> Curio/accessory slot
- Player slot -> Curio/accessory slot

`GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Begin GroundItem, CreativeItem, or MenuSlot migration, or first consolidate unified-move regression tests.

## Phase 21 Notes

Phase 21 migrated the client-side GroundItem -> Grid path to `MoveItemMessage`.

- Added `GridItemSource.GroundItem(entityId)`.
- Appended source codec type id `6`; existing source, target, and nested-path ids are unchanged.
- GroundItem uses `GridItemTarget.MenuGridPlacement`.

Both player-grid and non-player-grid main-grid release branches now send the unified message. Placement coordinates are still calculated from the nearby ground item's `view.stack()` rather than the general dragged stack. The original `inGrid` checks, menu branching, rotation state, and `rotatedPreview` reset timing remain unchanged.

`PickupGroundItemIntoGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.GroundItem`, `GridItemTarget.MenuGridPlacement`, `GridMoveOptions`, and `GridItemMoveService`.

GroundItem -> MenuGridPlacement uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.pickupGroundItemIntoGrid`. The server therefore still resolves a reachable `ItemEntity` from the entity id, uses its authoritative stack, applies the legacy stacking rule, updates or discards the entity, saves, and synchronizes the menu. The unified target's folded option is intentionally ignored for this path to preserve legacy behavior.

GroundItem -> Nested, Equipment, PlayerSlot, and Curio still use their old packets.

`MoveItemMessage` now additionally covers:

- Ground item -> main grid placement

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate GroundItem -> Nested or GroundItem -> Equipment to `MoveItemMessage`.

## Phase 22 Notes

Phase 22 migrated the client-side GroundItem -> Nested path to `MoveItemMessage`, reusing the existing `GridItemSource.GroundItem(entityId)` without changing source or target codec ids.

The client now constructs:

- `GridItemTarget.NestedGridPlacement` when the target container id is empty.
- `GridItemTarget.NestedEquipmentStoragePlacement` when the target container id is non-empty.

A stack-aware `nestedPlacementTarget` overload preserves anchor calculations based on the nearby ground item's `view.stack()`. The existing helper delegates with the normal dragged stack, so previously migrated paths remain unchanged. Nested-window hit testing, rotation state, `rotatedPreview` reset, and return timing are preserved.

`PickupGroundItemIntoNestedGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.GroundItem`, the corresponding nested target, `GridMoveOptions`, and `GridItemMoveService`.

GroundItem -> NestedGridPlacement and GroundItem -> NestedEquipmentStoragePlacement use minimal early branches in `GridItemTransferService`. Both delegate to `GridInventoryMenu.pickupGroundItemIntoNestedGrid`, preserving authoritative reachable-entity lookup, placement validation, stacking behavior, nested owner writeback, entity updates, and saving. Folded options remain intentionally unused to preserve legacy behavior.

GroundItem -> Equipment, PlayerSlot, and Curio still use their old packets. GroundItem -> Grid remains on `MoveItemMessage`.

`MoveItemMessage` now additionally covers:

- Ground item -> nested/free-window placement

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate GroundItem -> Equipment or GroundItem -> PlayerSlot to `MoveItemMessage`.

## Phase 23 Notes

Phase 23 migrated the client-side GroundItem -> Equipment path to `MoveItemMessage`, reusing the existing `GridItemSource.GroundItem(entityId)` without changing codec ids.

The client constructs `GridItemTarget.EquipmentStoragePlacement`. A stack-aware `equipmentPlacementTarget` overload preserves `targetRegionX` / `targetRegionY` calculations based on the nearby ground item's `view.stack()`. The existing helper delegates with the normal dragged stack, so previously migrated paths remain unchanged. The player-grid equipment-region restriction and `rotatedPreview` reset timing are preserved.

`PickupGroundItemIntoEquipmentStorageMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.GroundItem`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`.

GroundItem -> EquipmentStoragePlacement uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.pickupGroundItemIntoEquipmentStorage`. This preserves authoritative reachable-entity lookup, equipment-storage resolution, placement validation, stacking behavior, entity updates, storage writeback, and synchronization. Folded options remain intentionally unused to preserve legacy behavior.

GroundItem -> PlayerSlot and Curio still use their old packets. GroundItem -> Grid and Nested remain on `MoveItemMessage`.

`MoveItemMessage` now additionally covers:

- Ground item -> equipment-storage placement

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate GroundItem -> PlayerSlot or GroundItem -> Curio to `MoveItemMessage`.

## Phase 24 Notes

Phase 24 migrated the client-side GroundItem -> PlayerSlot path to `MoveItemMessage`, reusing the existing `GridItemSource.GroundItem(entityId)` without changing codec ids.

The client constructs `GridItemTarget.PlayerSlot` only when the hovered player slot exists and is empty. The existing player-grid restriction, empty-slot check, `rotatedPreview` reset, and immediate return timing remain unchanged.

`PickupGroundItemIntoPlayerSlotMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.GroundItem`, `GridItemTarget.PlayerSlot`, default `GridMoveOptions`, and `GridItemMoveService`.

GroundItem -> PlayerSlot uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.pickupGroundItemIntoPlayerSlot`. This preserves authoritative reachable-entity lookup, player-grid and free-slot checks, empty-target semantics, vanilla slot placement rules, moved-count limits, player inventory updates, and ground-entity updates. Existing-item merging and folded behavior are intentionally not added.

GroundItem -> Curio still uses its old packet. GroundItem -> Grid, Nested, Equipment, and PlayerSlot now use `MoveItemMessage`.

`MoveItemMessage` now additionally covers:

- Ground item -> player slot

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate GroundItem -> Curio or begin CreativeItem paths.

## Phase 25 Notes

Phase 25 migrated the client-side GroundItem -> Curio path to `MoveItemMessage`, reusing the existing `GridItemSource.GroundItem(entityId)` without changing codec ids.

The client constructs `GridItemTarget.AccessorySlot` from the hovered Curio view. The existing player-grid restriction, Curio hit test, `rotatedPreview` reset, and immediate return timing remain unchanged.

`PickupGroundItemIntoCurioMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.GroundItem`, `GridItemTarget.AccessorySlot`, default `GridMoveOptions`, and `GridItemMoveService`.

GroundItem -> AccessorySlot uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.pickupGroundItemIntoCurio`. This preserves authoritative reachable-entity lookup, player-grid checks, accessories bridge insertion, actual moved-count calculation, and ground-entity updates.

All GroundItem client paths now use `MoveItemMessage`:

- GroundItem -> Grid
- GroundItem -> Nested
- GroundItem -> Equipment
- GroundItem -> PlayerSlot
- GroundItem -> Curio

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Begin CreativeItem -> Grid / Nested / Equipment / PlayerSlot / Curio migration.

## Phase 26 Notes

Phase 26 migrated the client-side CreativeItem -> Grid path to `MoveItemMessage`.

- Added `GridItemSource.CreativeItem(tabIndex, itemIndex, count)`.
- Appended source codec type id `7`; existing source, target, and nested-path ids are unchanged.
- CreativeItem uses `GridItemTarget.MenuGridPlacement`.

The client calculates placement coordinates from the creative item's own stack using the stack-aware grid target helper. Rotation and folded state are preserved in both the target and `GridMoveOptions`; the existing creative-player check, grid hit test, and `rotatedPreview` reset timing remain unchanged.

`CreativeInsertIntoGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.CreativeItem`, `GridItemTarget.MenuGridPlacement`, `GridMoveOptions`, and `GridItemMoveService`.

CreativeItem -> MenuGridPlacement uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.creativeInsertIntoGrid`. The server therefore still verifies creative permissions, rejects spectators, rebuilds creative-tab contents, validates tab/item indices, reconstructs the authoritative stack with its data, applies count and folded semantics, validates placement, and saves.

CreativeItem -> Nested, Equipment, PlayerSlot, and Curio still use their old packets. All GroundItem paths remain unchanged.

`MoveItemMessage` now additionally covers:

- Creative item -> main grid placement

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate CreativeItem -> Nested or CreativeItem -> Equipment to `MoveItemMessage`.

## Phase 27 Notes

Phase 27 migrated the client-side CreativeItem -> Nested path to `MoveItemMessage`, reusing the existing `GridItemSource.CreativeItem(tabIndex, itemIndex, count)` without changing codec ids.

The client constructs:

- `GridItemTarget.NestedGridPlacement` when the target container id is empty.
- `GridItemTarget.NestedEquipmentStoragePlacement` when the target container id is non-empty.

Placement coordinates are calculated from the creative item's own stack through the existing stack-aware nested target helper. Rotation and folded state are preserved in both the target and options; creative checks, nested-window hit testing, `rotatedPreview` reset, and return timing remain unchanged.

`CreativeInsertIntoNestedGridMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.CreativeItem`, the corresponding nested target, `GridMoveOptions`, and `GridItemMoveService`.

CreativeItem -> NestedGridPlacement and CreativeItem -> NestedEquipmentStoragePlacement use minimal early branches in `GridItemTransferService`. Both delegate to `GridInventoryMenu.creativeInsertIntoNestedGrid`, preserving server-side creative stack reconstruction, path and target-grid resolution, folded semantics, placement validation, nested owner writeback, and saving.

CreativeItem -> Equipment, PlayerSlot, and Curio still use their old packets. CreativeItem -> Grid and all GroundItem paths remain unchanged.

`MoveItemMessage` now additionally covers:

- Creative item -> nested/free-window placement

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate CreativeItem -> Equipment or CreativeItem -> PlayerSlot to `MoveItemMessage`.

## Phase 28 Notes

Phase 28 migrated the client-side CreativeItem -> Equipment path to `MoveItemMessage`, reusing the existing `GridItemSource.CreativeItem(tabIndex, itemIndex, count)` without changing codec ids.

The client constructs `GridItemTarget.EquipmentStoragePlacement` after hitting an equipment storage region. Placement coordinates are still calculated from the creative item's own stack through the existing stack-aware equipment target helper. Rotation and folded state are preserved in both the target and `GridMoveOptions`; the existing creative-player check, player-grid restriction, equipment-region hit test, and `rotatedPreview` reset timing remain unchanged.

`CreativeInsertIntoEquipmentStorageMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.CreativeItem`, `GridItemTarget.EquipmentStoragePlacement`, `GridMoveOptions`, and `GridItemMoveService`.

CreativeItem -> EquipmentStoragePlacement uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.creativeInsertIntoEquipmentStorage`. This preserves authoritative creative stack reconstruction, creative and spectator checks, tab/item index and count semantics, folded state, equipment storage resolution, placement validation, writeback, saving, and synchronization.

CreativeItem -> PlayerSlot and Curio still use their old packets. CreativeItem -> Grid and Nested, all GroundItem paths, and all previously migrated PlayerSlot, Curio, Grid, and Equipment paths remain unchanged.

`MoveItemMessage` now additionally covers:

- Creative item -> equipment storage placement

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate CreativeItem -> PlayerSlot or CreativeItem -> Curio to `MoveItemMessage`.

## Phase 29 Notes

Phase 29 migrated the client-side CreativeItem -> PlayerSlot path to `MoveItemMessage`, reusing the existing `GridItemSource.CreativeItem(tabIndex, itemIndex, count)` without changing codec ids.

After hitting a vanilla player slot, the client constructs `GridItemTarget.PlayerSlot`. The existing creative-player and hovered-slot checks and `rotatedPreview` reset timing remain unchanged. This path uses `false` for folded state and does not add an empty-slot restriction, preserving the old replacement behavior when `mayInsertIntoVanillaSlot` allows the item.

`CreativeInsertIntoPlayerSlotMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.CreativeItem`, `GridItemTarget.PlayerSlot`, `GridMoveOptions.all(false, false)`, and `GridItemMoveService`.

CreativeItem -> PlayerSlot uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.creativeInsertIntoPlayerSlot`. This preserves authoritative creative stack reconstruction, creative and spectator checks, tab/item index and count semantics, fixed `folded=false`, player-slot bounds and insertion rules, replacement behavior, and inventory updates.

CreativeItem -> Curio still uses its old packet. CreativeItem -> Grid, Nested, and Equipment, all GroundItem paths, and all previously migrated PlayerSlot, Curio, Grid, and Equipment paths remain unchanged.

`MoveItemMessage` now additionally covers:

- Creative item -> player slot

All previously documented unified paths remain covered. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Migrate CreativeItem -> Curio to `MoveItemMessage`.

## Phase 30 Notes

Phase 30 migrated the client-side CreativeItem -> Curio path to `MoveItemMessage`, reusing the existing `GridItemSource.CreativeItem(tabIndex, itemIndex, count)` without changing codec ids.

After hitting a Curio slot in the player-grid menu, the client constructs `GridItemTarget.AccessorySlot`. The existing player-grid restriction, Curio hit test, creative-player check, and `rotatedPreview` reset timing remain unchanged. This path uses `false` for folded state, preserving the old behavior.

`CreativeInsertIntoCurioMessage` remains registered with unchanged fields, packet id, and codecs. Its handler is now an adapter through `GridMoveMessageGuards`, `GridItemSource.CreativeItem`, `GridItemTarget.AccessorySlot`, `GridMoveOptions.all(false, false)`, and `GridItemMoveService`.

CreativeItem -> AccessorySlot uses a minimal early branch in `GridItemTransferService` and delegates to `GridInventoryMenu.creativeInsertIntoCurio`. This preserves authoritative creative stack reconstruction, creative and spectator checks, tab/item index and count semantics, fixed `folded=false`, accessories bridge insertion, inventory updates, saving, and failure behavior.

All CreativeItem client paths now use `MoveItemMessage`:

- CreativeItem -> Grid
- CreativeItem -> Nested
- CreativeItem -> Equipment
- CreativeItem -> PlayerSlot
- CreativeItem -> Curio

All GroundItem paths and all previously migrated PlayerSlot, Curio, Grid, and Equipment paths remain unchanged. `GridMoveOptions.count` is still reserved for a later phase and does not yet control move quantity.

Recommended next phase:

- Perform a regression audit of old packet adapters, the source/target matrix, special-branch delegation, count/folded/rotated semantics, and dedicated-server client-only risks before migrating MenuSlot paths.
