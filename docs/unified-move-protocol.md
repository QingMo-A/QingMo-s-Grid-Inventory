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

## Phase 31 Notes

Phase 31 is a regression audit and protocol-matrix freeze. It adds no migration path and makes no code change. The audit found no issue that requires a fix.

### Codec Matrix

Source ids remain:

| Id | Source |
|---:|---|
| 0 | `PlayerSlot` |
| 1 | `MenuGridEntry` |
| 2 | `EquipmentStorageEntry` |
| 3 | `NestedGridEntry` |
| 4 | `NestedEquipmentStorageEntry` |
| 5 | `AccessorySlot` |
| 6 | `GroundItem` |
| 7 | `CreativeItem` |

Target ids remain:

| Id | Target |
|---:|---|
| 0 | `PlayerSlot` |
| 1 | `MenuGridPlacement` |
| 2 | `EquipmentStoragePlacement` |
| 3 | `NestedGridPlacement` |
| 4 | `NestedEquipmentStoragePlacement` |
| 5 | `AccessorySlot` |

Nested path segment ids and their read/write order remain unchanged.

### MoveItemMessage Coverage

| Source | Covered targets |
|---|---|
| Grid | Nested, Equipment, Curio |
| Nested | Grid, Nested, Equipment |
| Equipment | Grid, Nested, Equipment, Curio |
| PlayerSlot | Grid, Nested, Equipment, PlayerSlot, Curio |
| Curio | Grid, Nested, Equipment, PlayerSlot |
| GroundItem | Grid, Nested, Equipment, PlayerSlot, Curio |
| CreativeItem | Grid, Nested, Equipment, PlayerSlot, Curio |

All five GroundItem paths and all five CreativeItem paths are migrated. MenuSlot and Container Sidecar paths remain outside this matrix.

### Old Packet Adapters

The audit confirmed adapters for all migrated GroundItem and CreativeItem packets:

- `PickupGroundItemIntoGridMessage`, `PickupGroundItemIntoNestedGridMessage`, `PickupGroundItemIntoEquipmentStorageMessage`, `PickupGroundItemIntoPlayerSlotMessage`, and `PickupGroundItemIntoCurioMessage`.
- `CreativeInsertIntoGridMessage`, `CreativeInsertIntoNestedGridMessage`, `CreativeInsertIntoEquipmentStorageMessage`, `CreativeInsertIntoPlayerSlotMessage`, and `CreativeInsertIntoCurioMessage`.

The previously migrated PlayerSlot, Curio, Grid, Equipment, and Nested movement packets were also checked. Each migrated adapter uses `GridMoveMessageGuards.withGridMenu`, constructs the appropriate source, target, and options, calls `GridItemMoveService.move`, and on success calls both `menu.broadcastChanges()` and `ModNetworking.syncMenu(player, menu)`. Old packet registrations, ids, fields, and codecs remain intact.

### Special Branch Delegation

The pre-transaction branches in `GridItemTransferService` remain limited to old menu-method delegation:

| Source | Target | Delegated menu method |
|---|---|---|
| GroundItem | Grid | `pickupGroundItemIntoGrid` |
| GroundItem | Nested / nested equipment | `pickupGroundItemIntoNestedGrid` |
| GroundItem | Equipment | `pickupGroundItemIntoEquipmentStorage` |
| GroundItem | PlayerSlot | `pickupGroundItemIntoPlayerSlot` |
| GroundItem | Curio | `pickupGroundItemIntoCurio` |
| CreativeItem | Grid | `creativeInsertIntoGrid` |
| CreativeItem | Nested / nested equipment | `creativeInsertIntoNestedGrid` |
| CreativeItem | Equipment | `creativeInsertIntoEquipmentStorage` |
| CreativeItem | PlayerSlot | `creativeInsertIntoPlayerSlot` |
| CreativeItem | Curio | `creativeInsertIntoCurio` |
| PlayerSlot | PlayerSlot | `movePlayerFreeSlot` |

Every branch logs through `debug`, returns the delegated result, and appears before transaction construction. None duplicates entity, creative-tab, placement, nested-writeback, inventory-write, save, or accessory-write logic.

### Option Semantics

- `GridMoveOptions.count` remains reserved and does not yet control transfer quantity.
- GroundItem quantity remains controlled by the delegated menu methods. Grid, Nested, and Equipment branches do not apply folded state; PlayerSlot and Curio use `false`.
- CreativeItem carries count in `GridItemSource.CreativeItem`. Grid, Nested, and Equipment preserve folded state; PlayerSlot and Curio use `folded=false`.
- Grid, Nested, and Equipment placements use `target.rotated()`. PlayerSlot and AccessorySlot do not use rotation.
- PlayerSlot -> PlayerSlot passes target folded state to `movePlayerFreeSlot`.

The common-side transfer, codec, message, guard, source, target, and menu classes contain no imports from client, GUI, screen, `net.minecraft.client`, or Blaze3D packages. No dedicated-server client-only loading risk was found in the audited unified protocol path.

Recommended next phase:

- Do not begin a broad MenuSlot migration until MenuSlot and Container Sidecar source/target representations, any required new codec ids, and old-semantics adapter behavior are designed explicitly.

## Phase 32 Notes

Phase 32 is a design audit only. It migrates no packet and changes only this document. `GridItemSource`, `GridItemTarget`, `GridMoveCodecs`, `GridMoveOptions`, `MoveItemMessage`, transfer logic, menu logic, and old handlers remain unchanged.

### Remaining Packet Classification

| Packet or family | Fields / behavior | Current state | Future direction |
|---|---|---|---|
| `ExtractToPlayerInventoryMessage` | `entryId`, `amount`; grid entry to automatic player-inventory merge/add | Direct menu call; no equivalent target | Keep dedicated until auto-placement and count semantics are designed |
| `ExtractGridEntryToPlayerSlotMessage` | `entryId`, `playerSlot`, `amount` | Direct menu call | Migrate after count semantics, using existing source/target plus a temporary delegating branch |
| `ExtractNestedGridEntryToPlayerSlotMessage` | owner path, container id, entry id, player slot, amount | Direct menu call with nested writeback | Migrate after count semantics and explicit empty/non-empty container-id mapping |
| `ExtractEquipmentStorageEntryMessage` | equipment slot, container id, entry id, player slot, amount | Direct menu call | Same amount-path family; migrate only after count semantics |
| `InsertFromPlayerInventoryMessage` | player slot, coordinates, rotated, quick, folded | `quick=false` is an adapter; `quick=true` directly invokes automatic insertion | Keep quick mode dedicated for now |
| `MoveGridEntryMessage` | entry id, coordinates, rotated, folded | Direct same-grid reposition | Can use existing `MenuGridEntry -> MenuGridPlacement` after same-root semantics are verified |
| Grid / Equipment / Nested transfer packets | source identity, explicit placement, rotated, folded | Already adapters to `GridItemMoveService` | Retain as compatibility adapters |
| `QuickEquip*Message` | source identity; server chooses equipment target | Direct quick action | Keep dedicated or later use a separate quick-action protocol |
| `Drop*Message` | source identity; creates a world drop | Direct action | Keep dedicated; drop is not a placement target |
| `Toggle*BackpackFoldMessage` | source identity plus placement state | Direct state transition | Keep dedicated; folding is not movement |
| `ManualPickupItemMessage` | world pickup action | Dedicated action | Keep dedicated; it is not an explicit source-to-target placement |
| Open and sync messages | menu opening or state synchronization | Dedicated protocol | Outside movement protocol |

The already migrated Curio extraction/insertion, equipment/grid/nested transfer, PlayerSlot transfer, GroundItem, and CreativeItem packets are compatibility adapters and require no further migration.

### Player Inventory Auto-Placement

`ExtractToPlayerInventoryMessage` cannot be represented faithfully as `PlayerSlot`: it relies on player-inventory merge/add rules, has no explicit destination slot, and carries `amount`.

Two designs were considered:

- Add `GridItemTarget.PlayerInventoryAutoPlacement`, with quantity supplied by `GridMoveOptions.count`.
- Keep the dedicated packet and its `extractToPlayerInventory` menu method.

Recommendation: keep the dedicated packet through the count work. If automatic destinations later become a shared protocol concept, add an explicit auto-placement target rather than overloading `PlayerSlot`. The target should not contain amount; quantity belongs in options. This would require a new target codec id and must therefore be a deliberate compatibility change.

### Explicit PlayerSlot Amount Paths

`ExtractGridEntryToPlayerSlotMessage` already maps structurally to:

`MenuGridEntry(entryId) -> PlayerSlot(playerSlot)`

No new source or target is needed. Migration should first define `GridMoveOptions.count`, including positive-value validation, maximum clamping, whole-stack/default behavior, partial extraction, target merge capacity, and rollback. The first migration should use a narrow pre-transaction branch delegating to `extractToPlayerSlot` so old partial-stack behavior is preserved before any generic transaction implementation is attempted.

`ExtractNestedGridEntryToPlayerSlotMessage` maps to:

- Empty `sourceContainerId`: `NestedGridEntry(ownerPath, entryId) -> PlayerSlot`.
- Non-empty `sourceContainerId`: `NestedEquipmentStorageEntry(ownerPath, containerId, entryId) -> PlayerSlot`.

It needs no new source or target, but should follow the main-grid amount path. A narrow branch should initially delegate to `extractNestedGridEntryToPlayerSlot`, preserving owner resolution, partial extraction, nested writeback, and save semantics. The container-id split must happen in both the client path and old-packet adapter.

`ExtractEquipmentStorageEntryMessage` similarly maps from the existing `EquipmentStorageEntry` to `PlayerSlot` and should be handled in the same count-enabled family.

Count on these explicit PlayerSlot paths must not silently change the existing whole-item semantics of Grid, Nested, or Equipment moves to Curio or grid placements.

### Quick Insert and Same-Grid Reposition

For `InsertFromPlayerInventoryMessage`, `quick=false` is already an adapter for `PlayerSlot -> MenuGridPlacement`. `quick=true` asks the server to find the best available grid location and can insert according to grid merge rules. It should remain a dedicated packet for now.

Possible future targets include `MenuGridAutoPlacement` or a broader quick-action protocol. A vague `QuickInsertTarget` should be avoided because it hides destination ownership and policy. Quick behavior should not share ordinary drag-placement semantics until automatic target selection has a stable contract.

`MoveGridEntryMessage` is same-root `MenuGridEntry -> MenuGridPlacement` repositioning. Existing source and target types can express it, including rotated and folded state. Before migration, verify that the generic transaction handles identical roots without duplicate extraction/writeback and preserves `moveEntry` validation and rollback. Toggle-fold remains a separate action and must not be folded into repositioning. The safest first implementation is an adapter plus a narrow delegation branch to `moveEntry`.

### Quick Equip, Drop, and Fold

- Quick equip performs server-side target selection across armor/accessory rules. It is an action, not an explicit placement. Keep `QuickEquip*Message`, or later design a separate `QuickMoveMessage`.
- Drop turns a source into a world entity and has no inventory placement target. Keep `Drop*Message`.
- Toggle fold changes item state and may validate a resulting footprint, but it is not movement. Keep `Toggle*BackpackFoldMessage`.

These actions should not be forced into `MoveItemMessage`.

### Container Sidecar and MenuSlot

The repository contains `docs/container-sidecar-design.md`, which proposes sidecar-to-menu-slot and menu-slot-to-sidecar packets and client-side abstractions. No stable runtime Sidecar source/target classes or corresponding transfer packets were found in `common`.

A future MenuSlot identity must not reuse `PlayerSlot`. `PlayerSlot` is a player inventory index; a menu slot is a container-menu index whose ownership, backing container, permissions, and `mayPlace`/`mayPickup` rules must be revalidated by the server against the active menu. Sidecar support may therefore need new `MenuSlot` source and target types and new codec ids, but those additions are not recommended until runtime ownership and synchronization are implemented. No client object or `ItemStack` should be serialized as slot identity.

### Protocol Decisions

- Add no source type now. Existing types cover all currently implemented amount paths.
- Add no target type now. An auto-placement target is justified only if automatic inventory insertion becomes part of the unified protocol.
- Enable `GridMoveOptions.count` before migrating amount packets. Define a clear sentinel/default, reject non-positive requests, clamp to authoritative source and destination capacity, and make partial extraction transactional.
- Do not conflate PlayerSlot, MenuSlot, automatic placement, and quick actions.
- Old packet ids, codecs, and handlers must remain as compatibility adapters when each path is eventually migrated.

### Recommended Sequence

Phase 33:

- Only design and enable server-side `GridMoveOptions.count` semantics, or migrate one minimal amount path as the proof.
- Do not also migrate auto-placement, quick actions, or drop.

Phase 34:

- Migrate GridEntry -> PlayerSlot amount paths after count semantics are explicit.

Phase 35:

- Migrate NestedGridEntry -> PlayerSlot amount paths after the `sourceContainerId` mapping is explicit.

Phase 36:

- Design `PlayerInventoryAutoPlacement`, or formally retain `ExtractToPlayerInventoryMessage`.

Phase 37:

- Decide whether quick insert belongs in a separate `QuickMoveMessage` or remains a dedicated packet.

## Phase 33 Notes

Phase 33 migrated `ExtractGridEntryToPlayerSlotMessage`, establishing the first minimal amount proof path in the unified movement protocol.

The client now sends `MoveItemMessage` for GridEntry -> PlayerSlot using:

- Source: `GridItemSource.MenuGridEntry(entryId)`.
- Target: `GridItemTarget.PlayerSlot(playerSlot)`.
- Options: `new GridMoveOptions(amount, false, false)`.

The requested amount is the client-observed entry stack count at release time and is carried in `GridMoveOptions.count`; rotation and target folded state are both false. The server still resolves the authoritative entry by id and treats count only as a requested upper bound.

`ExtractGridEntryToPlayerSlotMessage` remains registered with unchanged fields, packet id, and codec. Its handler is now an adapter through `GridMoveMessageGuards`, the same source and target types, count-preserving options, and `GridItemMoveService`. Successful legacy requests broadcast and synchronize the menu.

`GridItemTransferService` now has a pre-transaction `MenuGridEntry -> PlayerSlot` branch. It normalizes count through `safeOptions.safeCount()` and delegates to `GridInventoryMenu.extractToPlayerSlot`, preserving entry and slot validation, vanilla-slot restrictions, empty-slot insertion, compatible-stack merging, capacity clamping, partial extraction, inventory updates, and saving. Generic transaction count handling remains unchanged.

No source, target, or codec type was added or modified. NestedGridEntry -> PlayerSlot, EquipmentStorageEntry -> PlayerSlot, player-inventory auto-placement, quick actions, drop, fold toggles, MenuSlot, and Container Sidecar remain on their previous paths.

`GridMoveOptions.count` is currently consumed only by:

- `MenuGridEntry -> PlayerSlot` amount transfer.

It still does not control ordinary Grid, Nested, or Equipment placements, GroundItem, CreativeItem, quick actions, auto-placement, or drop.

Recommended next phase:

- Migrate either EquipmentStorageEntry -> PlayerSlot or NestedGridEntry -> PlayerSlot as a separate amount path. Prefer the simpler EquipmentStorageEntry path and do not migrate both in one phase.

## Phase 34 Notes

Phase 34 migrated `ExtractEquipmentStorageEntryMessage`, establishing the second minimal amount proof path in the unified movement protocol.

The client now sends `MoveItemMessage` for EquipmentStorageEntry -> PlayerSlot using:

- Source: `GridItemSource.EquipmentStorageEntry(equipmentSlot, containerId, entryId)`.
- Target: `GridItemTarget.PlayerSlot(playerSlot)`.
- Options: `new GridMoveOptions(amount, false, false)`.

The requested amount is carried in `GridMoveOptions.count`; rotation and target folded state are both false. The server resolves the authoritative equipment storage and entry and treats count only as a requested upper bound.

`ExtractEquipmentStorageEntryMessage` remains registered with unchanged fields, packet id, and codec. Its handler is now an adapter through `GridMoveMessageGuards`, the same source and target types, count-preserving options, and `GridItemMoveService`. Successful legacy requests broadcast and synchronize the menu.

`GridItemTransferService` now has a pre-transaction `EquipmentStorageEntry -> PlayerSlot` branch. It normalizes count through `safeOptions.safeCount()` and delegates to `GridInventoryMenu.extractEquipmentEntryToPlayerSlot`, preserving equipment storage resolution, entry and slot validation, vanilla-slot restrictions, empty-slot insertion, compatible-stack merging, capacity clamping, partial extraction, equipment storage saving, and player inventory updates. Generic transaction count handling remains unchanged.

No source, target, codec, or `GridMoveOptions` definition was modified. NestedGridEntry -> PlayerSlot, player-inventory auto-placement, quick actions, drop, fold toggles, MenuSlot, and Container Sidecar remain on their previous paths.

`GridMoveOptions.count` is currently consumed by:

- `MenuGridEntry -> PlayerSlot` amount transfer.
- `EquipmentStorageEntry -> PlayerSlot` amount transfer.

It still does not control ordinary Grid, Nested, or Equipment placements, GroundItem, CreativeItem, quick actions, auto-placement, or drop.

Recommended next phase:

- Migrate NestedGridEntry -> PlayerSlot as a separate amount path, explicitly mapping empty and non-empty `sourceContainerId` values to the corresponding nested source type.

## Phase 35 Notes

Phase 35 migrated `ExtractNestedGridEntryToPlayerSlotMessage`, establishing the third minimal amount proof-path family.

The client now sends `MoveItemMessage` for nested entry -> PlayerSlot and reuses `nestedDragSource()`:

- Empty `sourceContainerId`: `GridItemSource.NestedGridEntry(ownerPath, entryId)`.
- Non-empty `sourceContainerId`: `GridItemSource.NestedEquipmentStorageEntry(ownerPath, containerId, entryId)`.
- Target: `GridItemTarget.PlayerSlot(playerSlot)`.
- Options: `new GridMoveOptions(amount, false, false)`.

The requested amount is carried in `GridMoveOptions.count`; rotation and target folded state are both false. The server resolves the authoritative owner, nested grid, and entry and treats count only as a requested upper bound.

`ExtractNestedGridEntryToPlayerSlotMessage` remains registered with unchanged fields, packet id, and codec. Its handler is now an adapter through `GridMoveMessageGuards`, maps the container id to the corresponding nested source, constructs `PlayerSlot` and count-preserving options, and calls `GridItemMoveService`. Successful legacy requests broadcast and synchronize the menu.

`GridItemTransferService` now has two pre-transaction delegating branches:

- `NestedGridEntry -> PlayerSlot`, passing an empty container id.
- `NestedEquipmentStorageEntry -> PlayerSlot`, passing the source container id.

Both normalize count through `safeOptions.safeCount()` and delegate to `GridInventoryMenu.extractNestedGridEntryToPlayerSlot`, preserving owner and nested-grid resolution, entry and slot validation, vanilla-slot restrictions, merging and capacity clamping, partial extraction, nested writeback, player inventory updates, and saving. Generic transaction count handling remains unchanged.

No source, target, codec, or `GridMoveOptions` definition was modified. Player-inventory auto-placement, quick actions, drop, fold toggles, MenuSlot, and Container Sidecar remain on their previous paths.

`GridMoveOptions.count` is currently consumed by:

- `MenuGridEntry -> PlayerSlot`.
- `EquipmentStorageEntry -> PlayerSlot`.
- `NestedGridEntry -> PlayerSlot`.
- `NestedEquipmentStorageEntry -> PlayerSlot`.

It still does not control ordinary Grid, Nested, or Equipment placements, GroundItem, CreativeItem, quick actions, auto-placement, or drop.

Recommended next phase:

- Perform an amount-path regression audit. Do not immediately migrate `ExtractToPlayerInventoryMessage` until automatic placement has a stable target design, and keep quick, drop, and fold actions dedicated.

## Phase 36 Notes

Phase 36 is an amount-path regression audit. It migrates no new path and changes only this document. The audit found no issue that requires a code fix.

### Count-Enabled Matrix

`GridMoveOptions.count` is consumed only by these explicit PlayerSlot amount paths:

| Source | Target | Delegated menu method |
|---|---|---|
| `MenuGridEntry` | `PlayerSlot` | `extractToPlayerSlot` |
| `EquipmentStorageEntry` | `PlayerSlot` | `extractEquipmentEntryToPlayerSlot` |
| `NestedGridEntry` | `PlayerSlot` | `extractNestedGridEntryToPlayerSlot` with an empty container id |
| `NestedEquipmentStorageEntry` | `PlayerSlot` | `extractNestedGridEntryToPlayerSlot` with the source container id |

All four paths use a server-authoritative entry identity and an explicit player slot. Count is only a requested upper bound. `safeOptions.safeCount()` maps non-positive values to `Integer.MAX_VALUE`, and the delegated menu method limits the move by the authoritative source count, destination capacity, and slot rules.

### Client Path Audit

The three client release paths all send `MoveItemMessage` with `new GridMoveOptions(entryCount, false, false)`:

- Main grid entries use `MenuGridEntry -> PlayerSlot`.
- Equipment storage entries use `EquipmentStorageEntry -> PlayerSlot`; existing `released` and `unequipped` timing is preserved.
- Nested entries reuse `nestedDragSource()`, which maps empty and non-empty container ids to `NestedGridEntry` and `NestedEquipmentStorageEntry`.

All retain the `hovered != null` check without adding an empty-slot restriction. The client no longer sends the three old amount packets on these drag paths.

`ExtractToPlayerInventoryMessage`, `InsertFromPlayerInventoryMessage` quick mode, `MoveGridEntryMessage`, QuickEquip, Drop, and ToggleFold remain on their dedicated paths.

### Compatibility Adapter Audit

`ExtractGridEntryToPlayerSlotMessage`, `ExtractEquipmentStorageEntryMessage`, and `ExtractNestedGridEntryToPlayerSlotMessage` remain registered with unchanged record fields, packet ids, and codecs. Each handler:

- Uses `GridMoveMessageGuards.withGridMenu`.
- Constructs the corresponding authoritative source identity and `GridItemTarget.PlayerSlot`.
- Preserves amount in `new GridMoveOptions(amount, false, false)`.
- Calls `GridItemMoveService.move`.
- Calls both `menu.broadcastChanges()` and `ModNetworking.syncMenu(player, menu)` after success.

None directly calls an extraction menu method or writes inventory/storage state.

### Transfer Branch Audit

All four amount branches are before `Transaction transaction = new Transaction(menu)`. Each obtains `requestedCount` through `safeOptions.safeCount()`, passes it to the matching old menu method, logs that count through `debug`, and returns the delegated result.

The branches do not implement extraction, merging, inventory writes, equipment saving, nested writeback, or rollback themselves. The generic transaction flow does not read or apply `GridMoveOptions.count`.

### Count Boundary

Count does not control:

- Ordinary Grid, Nested, or Equipment placement.
- Grid, Nested, or Equipment to Curio.
- Curio to PlayerSlot.
- PlayerSlot to Grid, Nested, Equipment, Curio, or PlayerSlot.
- GroundItem or CreativeItem paths.
- Player-inventory auto-placement.
- Quick insertion, QuickEquip, Drop, or ToggleFold.
- Same-grid reposition.
- MenuSlot or Container Sidecar.

Old packet adapters and new `MoveItemMessage` paths share the same four server-side delegating branches, so validation, capacity clamping, saving, and failure behavior remain centralized. No client `ItemStack` is trusted or serialized.

### Compatibility and Server Audit

Source ids remain `0-7`, target ids remain `0-5`, and nested path segment ids remain unchanged. `MoveItemMessage`, the three old amount packet codecs and record fields, packet ids, and save codecs are unchanged.

The audited common-side transfer, move service, amount messages, options, source, target, and codec classes contain no imports from client, GUI, screen, `net.minecraft.client`, or Blaze3D packages. No dedicated-server client-only loading risk was found.

Recommended next phase:

- Do not immediately migrate `ExtractToPlayerInventoryMessage`. First design a stable `PlayerInventoryAutoPlacement` target or formally retain the dedicated packet.
- Keep quick, drop, and fold actions dedicated.
- If ordinary movement migration continues, consider `MoveGridEntryMessage` same-grid reposition only after verifying same-root transaction behavior.

## Phase 37 Notes

Phase 37 migrated `MoveGridEntryMessage`, the main-grid same-grid reposition path, to `MoveItemMessage`.

The client now represents main-grid reposition as:

- Source: `GridItemSource.MenuGridEntry(entryId)`.
- Target: `GridItemTarget.MenuGridPlacement(targetX, targetY, rotated, targetFolded)`.
- Options: `GridMoveOptions.all(rotated, targetFolded)`.

The existing `gridDragSource()` and `menuGridPlacementTarget()` helpers preserve server-authoritative entry identity, target coordinates, rotation, and folded state. This path does not use amount semantics; its option count remains `Integer.MAX_VALUE`.

`MoveGridEntryMessage` remains registered with unchanged fields, packet id, and codec. Its handler is now an adapter through `GridMoveMessageGuards`, the same source and target types, rotation/fold-preserving options, and `GridItemMoveService`. Successful legacy requests broadcast and synchronize the menu.

No `GridItemTransferService` special branch was added. The path uses the generic same-grid transaction flow: it resolves source and target copies, identifies the source entry as ignored, removes that entry from the validation copy, validates and adds the prepared stack at the target placement, writes the grid to the root copy, and commits. Failed validation or writeback does not modify live data.

No source, target, codec, or `GridMoveOptions` definition was modified. `ExtractToPlayerInventoryMessage`, quick actions, drop, fold toggles, MenuSlot, and Container Sidecar remain on their previous paths.

`GridMoveOptions.count` remains limited to the explicit PlayerSlot amount paths:

- `MenuGridEntry -> PlayerSlot`.
- `EquipmentStorageEntry -> PlayerSlot`.
- `NestedGridEntry -> PlayerSlot`.
- `NestedEquipmentStorageEntry -> PlayerSlot`.

Recommended next phase:

- Perform an ordinary movement and same-root regression audit.
- Do not immediately migrate `ExtractToPlayerInventoryMessage`; first design `PlayerInventoryAutoPlacement` or formally retain its dedicated packet.

## Phase 38 Notes

Phase 38 is an ordinary-movement and same-root regression audit. It migrates no new path and changes only this document. The audit found no issue that requires a code fix.

### Same-Grid Reposition Audit

The main-grid reposition client path sends `MoveItemMessage` with `MenuGridEntry -> MenuGridPlacement`, preserving coordinates, rotation, and folded state through the existing target and option helpers. It no longer sends `MoveGridEntryMessage`.

`MoveGridEntryMessage` remains registered with unchanged fields, packet id, and codec. Its handler is a compatibility adapter through `GridMoveMessageGuards`, `GridItemMoveService`, `GridMoveOptions.all(rotated, targetFolded)`, menu broadcast, and menu synchronization. It does not call `moveEntry` or write grid data directly.

No `MenuGridEntry -> MenuGridPlacement` special branch exists. The generic transaction:

1. Resolves source and target snapshots.
2. Detects a shared grid through `sourceRef.sameGrid(targetRef)`.
3. Uses the source entry id as `ignoredEntryId`.
4. Removes the source entry from the validation copy.
5. Validates placement against that copy.
6. Adds the prepared stack and writes the updated grid to the root copy.
7. Commits only after all validation and writeback steps succeed.

Failure before commit leaves live data unchanged. The generic flow does not read `GridMoveOptions.count`.

### Ordinary Movement Matrix

The audited ordinary movement coverage is:

| Source | Explicit targets |
|---|---|
| `MenuGridEntry` | `MenuGridPlacement`, `NestedGridPlacement`, `NestedEquipmentStoragePlacement`, `EquipmentStoragePlacement`, `AccessorySlot` |
| `EquipmentStorageEntry` | `MenuGridPlacement`, `NestedGridPlacement`, `NestedEquipmentStoragePlacement`, `EquipmentStoragePlacement`, `AccessorySlot` |
| `NestedGridEntry` | `MenuGridPlacement`, `NestedGridPlacement`, `NestedEquipmentStoragePlacement`, `EquipmentStoragePlacement` |
| `NestedEquipmentStorageEntry` | `MenuGridPlacement`, `NestedGridPlacement`, `NestedEquipmentStoragePlacement`, `EquipmentStoragePlacement` |
| `PlayerSlot` | `MenuGridPlacement`, `NestedGridPlacement`, `NestedEquipmentStoragePlacement`, `EquipmentStoragePlacement`, `PlayerSlot`, `AccessorySlot` |
| `AccessorySlot` | `MenuGridPlacement`, `NestedGridPlacement`, `NestedEquipmentStoragePlacement`, `EquipmentStoragePlacement`, `PlayerSlot` |

Explicit grid placements use generic transactions except where an established narrow special branch preserves a distinct old semantic. Cross-root writes remain snapshot-based and commit only after source removal, target re-resolution, placement validation, and target writeback succeed.

### Count and Special-Branch Boundaries

Count remains enabled only for:

- `MenuGridEntry -> PlayerSlot`.
- `EquipmentStorageEntry -> PlayerSlot`.
- `NestedGridEntry -> PlayerSlot`.
- `NestedEquipmentStorageEntry -> PlayerSlot`.

These four pre-transaction branches normalize count with `safeOptions.safeCount()` and delegate to the existing extraction menu methods. Ordinary placement, same-grid reposition, GroundItem, CreativeItem, and quick/action packets do not consume option count.

Other pre-transaction special branches remain:

- GroundItem to Grid, Nested, Equipment, PlayerSlot, and Curio.
- CreativeItem to Grid, Nested, Equipment, PlayerSlot, and Curio.
- `PlayerSlot -> PlayerSlot`.
- The four explicit PlayerSlot amount paths.

GroundItem and CreativeItem are handled before transaction construction and are not resolved through `Transaction.resolveSource`. Their branches continue to delegate to authoritative menu methods rather than duplicating entity, creative-tab, placement, inventory, or save logic.

### Dedicated Actions

The following remain dedicated and outside ordinary `MoveItemMessage` placement semantics:

- `ExtractToPlayerInventoryMessage`.
- `InsertFromPlayerInventoryMessage` with `quick=true`.
- `QuickEquip*Message`.
- `Drop*Message`.
- `Toggle*BackpackFoldMessage`.
- `ManualPickupItemMessage`.
- MenuSlot and Container Sidecar paths.

### Compatibility and Server Audit

Source ids remain `0-7`, target ids remain `0-5`, and nested path segment ids remain unchanged. `MoveItemMessage`, `MoveGridEntryMessage` fields/id/codec, and save codecs are unchanged.

The audited common-side transfer, move service, reposition message, options, source, target, and codec classes contain no client, GUI, screen, `net.minecraft.client`, or Blaze3D imports. No dedicated-server client-only loading risk was found.

Recommended next phase:

- Do not immediately migrate `ExtractToPlayerInventoryMessage`. First design `PlayerInventoryAutoPlacement` or formally retain the dedicated packet.
- Keep quick, drop, and fold actions dedicated.
- Before further ordinary movement migration, audit the remaining old movement packets and identify whether any non-action path is not yet adapterized.

## Phase 39 Notes

Phase 39 audits the remaining old movement packets and makes protocol closure decisions. It migrates no packet and changes only this document. No issue requiring a code fix was found.

### Adapterized Movement Packets

All packets below retain their old fields, ids, and codecs. Their migrated paths construct a source, explicit target, and options and call `GridItemMoveService`; they no longer directly implement ordinary movement:

| Category | Adapterized packets |
|---|---|
| Unified entry point | `MoveItemMessage` |
| Same-root / direct moves | `MoveGridEntryMessage`, `MoveEquipmentStorageEntryMessage`, `MovePlayerFreeSlotMessage` |
| Grid / Equipment / Nested transfers | `TransferGridEntryIntoEquipmentStorageMessage`, `TransferGridEntryIntoNestedGridMessage`, `TransferEquipmentStorageEntryIntoGridMessage`, `TransferEquipmentStorageEntryIntoNestedGridMessage`, `TransferEquipmentStorageEntryMessage`, `TransferNestedGridEntryIntoGridMessage`, `TransferNestedGridEntryIntoEquipmentStorageMessage`, `TransferNestedGridEntryIntoNestedGridMessage` |
| PlayerSlot placement | `InsertFromPlayerInventoryMessage` when `quick=false`, `InsertIntoEquipmentStorageMessage`, `InsertPlayerSlotIntoNestedGridMessage`, `InsertPlayerSlotIntoCurioMessage` |
| Curio movement | `ExtractCurioToGridMessage`, `ExtractCurioToNestedGridMessage`, `ExtractCurioToEquipmentStorageMessage`, `ExtractCurioToPlayerSlotMessage`, `InsertGridEntryIntoCurioMessage`, `InsertEquipmentStorageEntryIntoCurioMessage` |
| Explicit amount movement | `ExtractGridEntryToPlayerSlotMessage`, `ExtractEquipmentStorageEntryMessage`, `ExtractNestedGridEntryToPlayerSlotMessage` |
| GroundItem compatibility | `PickupGroundItemIntoGridMessage`, `PickupGroundItemIntoNestedGridMessage`, `PickupGroundItemIntoEquipmentStorageMessage`, `PickupGroundItemIntoPlayerSlotMessage`, `PickupGroundItemIntoCurioMessage` |
| CreativeItem compatibility | `CreativeInsertIntoGridMessage`, `CreativeInsertIntoNestedGridMessage`, `CreativeInsertIntoEquipmentStorageMessage`, `CreativeInsertIntoPlayerSlotMessage`, `CreativeInsertIntoCurioMessage` |

GroundItem and CreativeItem packets use existing source identities and narrow server-side delegation branches. The three amount packets preserve amount in `GridMoveOptions.count`. `InsertFromPlayerInventoryMessage` remains intentionally mixed: only its explicit `quick=false` placement path is adapterized.

No further work is needed for these compatibility packets unless a behavioral bug is found.

### Dedicated Action Packets

| Packet family | Classification | Decision |
|---|---|---|
| `QuickEquipGridEntryMessage`, `QuickEquipEquipmentStorageEntryMessage`, `QuickEquipNestedGridEntryMessage`, `QuickEquipPlayerSlotMessage` | Quick action; server selects armor/accessory destination | Keep dedicated; a future shared abstraction should be `QuickMoveMessage`, not ordinary `MoveItemMessage` |
| `DropGridEntryMessage`, `DropEquipmentStorageEntryMessage`, `DropNestedGridEntryMessage`, `DropCurioMessage`, `DropPlayerSlotMessage` | Drop action producing a world item | Keep dedicated; a world drop is not an inventory placement target |
| `ToggleGridEntryBackpackFoldMessage`, `ToggleEquipmentStorageEntryBackpackFoldMessage` | Item-state transition with footprint validation | Keep dedicated; folding is not movement |
| `ManualPickupItemMessage` | Manual world interaction using an entity id and pickup handler | Keep dedicated; it does not express an explicit inventory target |

There is no registered `ToggleNestedGridEntryBackpackFoldMessage`.

If action handlers are unified later, quick equip is a candidate for `QuickMoveMessage`, while drop, fold, and manual pickup would require an explicit action protocol. They should not be encoded as placement targets.

### Auto-Placement Packets

| Packet / path | Current semantics | Explicit target | Decision |
|---|---|---:|---|
| `ExtractToPlayerInventoryMessage(entryId, amount)` | Extracts from the main grid and asks the server to merge/add into player inventory | No | Keep dedicated until `PlayerInventoryAutoPlacement` is designed |
| `InsertFromPlayerInventoryMessage` with `quick=true` | Asks the server to select a grid position and insert according to grid merge rules | No | Keep the quick branch dedicated; a future design may use `GridAutoPlacement` or `QuickMoveMessage` |

`ExtractToPlayerInventoryMessage` must not be disguised as `PlayerSlot`: doing so would lose automatic merge/add semantics. If auto-placement joins the unified protocol, quantity should remain in options and a new explicit target type and codec id would be required.

### MenuSlot and Container Sidecar

No runtime MenuSlot or Container Sidecar message, source, or target is registered in `common/network`. The repository currently has only `docs/container-sidecar-design.md`, which proposes future sidecar/menu-slot transfers.

A future menu-slot identity must remain distinct from `PlayerSlot` and requires server validation against the active menu, backing container ownership, `mayPickup`, `mayPlace`, and vanilla carried-stack behavior. Runtime Sidecar work is therefore a candidate for new `MenuSlot` source and target types and new codec ids, but only after a separate protocol design phase. No new type is justified by the current implementation.

### Non-Movement and Lifecycle Packets

| Packet family | Classification |
|---|---|
| `OpenPlayerGridInventoryMessage` | UI/menu lifecycle; not movement |
| `SyncGridInventoryMessage`, `SyncEquipmentStorageMessage` | State synchronization; not movement |
| `SyncBackpackFoldingRulesMessage`, `SyncItemSizeRulesMessage` | Rule/config synchronization; not movement |

`GridMessages`, `GridMoveCodecs`, `GridMoveMessageGuards`, and `ModNetworking` are protocol infrastructure rather than movement messages.

### Closure Decisions

No remaining packet represents an unadapterized ordinary source-to-explicit-target movement path. The remaining candidates are deliberately outside ordinary movement:

- Auto-placement: design `PlayerInventoryAutoPlacement` or retain its dedicated packet.
- Quick equip/insert: retain dedicated packets or design `QuickMoveMessage`.
- Drop, fold, and manual pickup: retain dedicated action packets or design a separate action protocol.
- MenuSlot/Sidecar: requires new identities, ownership validation, and a dedicated compatibility design.

The current unified movement protocol is suitable as a stable baseline:

- Ordinary explicit-target movement is covered.
- Explicit PlayerSlot amount movement is covered.
- GroundItem and CreativeItem compatibility paths are covered.
- Auto-target and non-movement actions remain intentionally dedicated.
- Old packet compatibility, codec ids, and save formats remain stable.

Recommended next phase:

- Do not immediately migrate quick, drop, or fold actions.
- Do not represent `ExtractToPlayerInventoryMessage` as a `PlayerSlot` target.
- If protocol design continues, Phase 40 should document `PlayerInventoryAutoPlacement` before any code change.
- Alternatively, freeze this stable baseline and move into in-game regression testing and focused bug fixing.

## Phase 40 Notes

### Stable Baseline Declaration

The current unified movement protocol is now frozen as the stable baseline. Stable does not mean bug-free. It means ordinary explicit-target movement, explicit PlayerSlot amount movement, and GroundItem/CreativeItem compatibility movement have completed protocol consolidation.

Further work should move from broad packet migration to in-game regression testing and focused bug fixing. Protocol boundaries, codec ids, and compatibility adapters should remain unchanged unless a separately designed phase explicitly revises them.

### Included in Stable Baseline

The baseline includes:

- `MoveItemMessage` as the unified ordinary movement entry point.
- `GridMoveMessageGuards` for server-side menu validation.
- `GridMoveOptions.count`, `rotated`, and `targetFolded`.
- Source codec ids `0-7`, target codec ids `0-5`, and the existing nested path segment ids.
- Old packet compatibility: registrations, record fields, packet ids, and codecs remain; migrated handlers act as adapters.

Ordinary explicit-target movement:

| Source | Targets |
|---|---|
| `MenuGridEntry` | Main grid, nested grid, nested equipment container, equipment storage, Curio |
| `EquipmentStorageEntry` | Main grid, nested grid, nested equipment container, equipment storage, Curio |
| `NestedGridEntry` | Main grid, nested grid, nested equipment container, equipment storage |
| `NestedEquipmentStorageEntry` | Main grid, nested grid, nested equipment container, equipment storage |
| `PlayerSlot` | Main grid, nested grid, nested equipment container, equipment storage, PlayerSlot, Curio |
| `AccessorySlot` | Main grid, nested grid, nested equipment container, equipment storage, PlayerSlot |

Explicit PlayerSlot amount movement:

- `MenuGridEntry -> PlayerSlot`.
- `EquipmentStorageEntry -> PlayerSlot`.
- `NestedGridEntry -> PlayerSlot`.
- `NestedEquipmentStorageEntry -> PlayerSlot`.

GroundItem compatibility movement:

- GroundItem to main grid, nested grid, equipment storage, PlayerSlot, and Curio.

CreativeItem compatibility movement:

- CreativeItem to main grid, nested grid, equipment storage, PlayerSlot, and Curio.

### Excluded from Stable Baseline

The following are intentionally outside unified ordinary movement:

- `ExtractToPlayerInventoryMessage` auto-placement.
- `InsertFromPlayerInventoryMessage` with `quick=true`.
- `QuickEquip*Message`.
- `Drop*Message`.
- `Toggle*BackpackFoldMessage`.
- `ManualPickupItemMessage`.
- MenuSlot and Container Sidecar.
- Open, sync, UI lifecycle, config, and rule-sync packets.
- Future `PlayerInventoryAutoPlacement` and `GridAutoPlacement` targets.
- Future `QuickMoveMessage` and `ActionMessage`.

These are not missed migrations. They lack an explicit placement target, perform server-selected quick behavior, create world entities, change state, or manage lifecycle rather than ordinary movement.

### In-Game Regression Test Matrix

#### A. Ordinary Movement

1. Main grid -> main grid reposition.
2. Main grid -> nested grid.
3. Main grid -> nested equipment container.
4. Main grid -> equipment storage.
5. Main grid -> Curio.
6. Equipment storage -> main grid.
7. Equipment storage -> nested grid.
8. Equipment storage -> nested equipment container.
9. Equipment storage -> equipment storage.
10. Equipment storage -> Curio.
11. Nested grid -> main grid.
12. Nested grid -> nested grid.
13. Nested grid -> nested equipment container.
14. Nested grid -> equipment storage.
15. Nested equipment container -> main grid.
16. Nested equipment container -> nested grid.
17. Nested equipment container -> nested equipment container.
18. Nested equipment container -> equipment storage.
19. PlayerSlot -> main grid.
20. PlayerSlot -> nested grid.
21. PlayerSlot -> nested equipment container.
22. PlayerSlot -> equipment storage.
23. PlayerSlot -> PlayerSlot.
24. PlayerSlot -> Curio.
25. Curio -> main grid.
26. Curio -> nested grid.
27. Curio -> nested equipment container.
28. Curio -> equipment storage.
29. Curio -> PlayerSlot.

#### B. Amount Movement

1. Main grid entry -> empty PlayerSlot.
2. Main grid entry -> compatible partial PlayerSlot stack.
3. Main grid entry -> different item PlayerSlot; must fail.
4. Equipment storage entry -> empty PlayerSlot.
5. Equipment storage entry -> compatible partial PlayerSlot stack.
6. Equipment storage entry -> different item PlayerSlot; must fail.
7. Nested grid entry -> empty PlayerSlot.
8. Nested grid entry -> compatible partial PlayerSlot stack.
9. Nested equipment entry -> empty PlayerSlot.
10. Nested equipment entry -> compatible partial PlayerSlot stack.
11. Non-positive amount compatibility request.
12. Large-stack destination-capacity clamp.
13. Failure does not delete the source.
14. Failure does not duplicate the source.

#### C. GroundItem

1. GroundItem -> main grid.
2. GroundItem -> nested grid.
3. GroundItem -> equipment storage.
4. GroundItem -> PlayerSlot.
5. GroundItem -> Curio.
6. Invalid entity id fails.
7. Full target fails.
8. Failure does not duplicate.

#### D. CreativeItem

1. CreativeItem -> main grid.
2. CreativeItem -> nested grid.
3. CreativeItem -> equipment storage.
4. CreativeItem -> PlayerSlot.
5. CreativeItem -> Curio.
6. Invalid tab or item index fails.
7. Count compatibility remains authoritative on the server.

#### E. Backpack, Fold, and Rotation

1. Move an unfolded backpack.
2. Move a folded backpack.
3. Place using the smaller folded footprint.
4. Unfolded footprint collision fails.
5. Rotated placement succeeds where valid.
6. Rotated collision fails.
7. Nested backpack cycle prevention.
8. Self or descendant move fails.

#### F. Failure and Rollback

1. Occupied target fails.
2. Invalid entry id fails.
3. Invalid owner path fails.
4. Invalid container id fails.
5. Invalid player slot fails.
6. Missing Curio support or invalid accessory fails.
7. Invalid equipment slot fails.
8. Failure does not remove the source.
9. Failure does not create a duplicate.
10. Failure does not corrupt a nested owner.
11. Failure does not corrupt equipment storage.
12. Failure does not leave the client desynchronized after sync.

#### G. Dedicated Packet Smoke Tests

1. `ExtractToPlayerInventoryMessage`.
2. `InsertFromPlayerInventoryMessage` with `quick=true`.
3. `QuickEquip*Message`.
4. `Drop*Message`.
5. `Toggle*BackpackFoldMessage`.
6. `ManualPickupItemMessage`.
7. Open and synchronization lifecycle packets.

For each movement test, verify source identity, destination contents, item count and data, folded/rotated state, menu synchronization, reopening persistence, and failure rollback. Run representative cases on both supported game targets and a dedicated server.

### Bugfix Rules

1. For ordinary explicit-target bugs, fix `GridItemTransferService` generic transaction logic or its focused helper.
2. For PlayerSlot amount bugs, fix the four pre-transaction amount branches or their delegated extraction menu methods.
3. For GroundItem or CreativeItem bugs, fix the corresponding special branch or delegated authoritative menu method.
4. Do not migrate a dedicated action into `MoveItemMessage` while fixing its bug.
5. For auto-placement bugs, fix the dedicated packet; do not disguise auto-placement as `PlayerSlot`.
6. Stop and report codec or id problems before changing any id.
7. Handle save-format problems in a separate phase.
8. For dedicated-server client-only crashes, inspect common network and inventory imports first.
9. Keep fixes narrow, preserve old packet adapters, and add focused regression coverage for the failed source-target pair.

### Failure Triage and Rollback Priority

1. Stop testing immediately for duplication, deletion, save corruption, or server crashes.
2. Capture source/target/options, root identities, nested paths, item data, and server logs.
3. Reproduce with the old compatibility packet and `MoveItemMessage` path where both are available.
4. Determine whether the failure belongs to an adapter, special branch, amount delegate, or generic transaction.
5. Prefer reverting the smallest offending phase or path over changing codec ids or save formats.
6. Re-run the affected matrix row plus neighboring source-target paths after a fix.

### Future Protocol Work

1. Write a `PlayerInventoryAutoPlacement` design document.
2. Write a `GridAutoPlacement` and quick-insert design document.
3. Design `QuickMoveMessage`.
4. Design an `ActionMessage` model for drop, fold, and manual pickup only if unification has clear value.
5. Design MenuSlot and Container Sidecar source/target identities and ownership validation.
6. Add any new source or target only in a dedicated codec-id design phase.

No future auto-placement or action work should begin as an incidental code change.

### Build and Test Note

Phase 40 changes documentation only, so it does not require Java compilation. Documentation review is not equivalent to in-game validation. This stable baseline is considered behaviorally proven only after the regression matrix is exercised in game, including dedicated-server and both supported target versions.
