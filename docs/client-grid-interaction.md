# Client Grid Interaction Layer

The client interaction layer removes placement and preview state from individual grid panels. It does not change
the wire format or the server-side transaction services.

## Responsibilities

- `GridDragSession` owns the preview stack, rotation, folded state, requested count, and pointer anchor.
- `GridInteractionController` handles rotate/fold input, unified `MoveItemMessage` sends, quick-equip/drop action
  dispatch, and ordered drop-surface resolution.
- `GridInteractionSurface` converts a pointer position into one of three results: pass, block lower layers, or a
  concrete `GridItemTarget`.
- `GridClientMoveRules` is the single client-side compatibility table for main-inventory and external-sidebar
  routes. The server remains authoritative and performs all validation and mutation.

`GridInventoryScreen` and `ExternalContainerSidebar` both use this layer. The main screen now resolves nested
windows, accessory slots, equipment storage, the menu grid, and player slots in visual order instead of repeating
the same target branches for every source type. The external sidebar uses the same drag session and controller for
sidebar-owned items and vanilla menu-carried stacks.

## Adding A Grid Surface

1. Represent the dragged item with an existing `GridItemSource`, or add a source only when the server protocol also
   needs a new identity.
2. Start the shared controller after the view's drag threshold is reached. Keep only view-specific data needed to
   hide or shade the source item.
3. Add one ordered `GridInteractionSurface` resolver that returns the surface's `GridItemTarget`. Return `blocked`
   when the pointer is inside the surface but no lower UI layer may receive the release.
4. Extend `GridClientMoveRules` only when the server already supports the new source/target pair.
5. Route rotate/fold input through `handlePreviewKey`, right-click rotation through `handleMouseButton`, and movement
   through `release`. Do not add another source-by-target release tree to the screen.

Source hit testing that has special click semantics, such as click-to-open nested containers or delayed drag
activation, remains owned by its view. Once the drag starts, preview state, shortcuts, target selection, and packet
dispatch are shared.
