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
