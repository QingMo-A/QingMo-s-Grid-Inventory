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
