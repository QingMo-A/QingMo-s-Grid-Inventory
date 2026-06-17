# Bridge Purity Maintenance Notes

This project uses a Target Adapter architecture:

- `common` contains shared semantics, stable gameplay logic, cross-version data models, and cross-version UI logic.
- `targets/<loader-version>` contains loader APIs, Minecraft-version-specific codecs, registration, events, mixins, networking, and client bootstrap.

The goal is not to make `common` compile once as a universal jar. Each target recompiles the shared source against its own Minecraft and loader APIs. Because of that, `common` must stay free of loader-specific and version-specific surface area.

## Hard Boundaries

Do not add these imports to `common/src/main/java`:

- `net.neoforged.*`
- `net.minecraftforge.*`
- `net.fabricmc.*`
- `top.theillusivec4.curios.*`
- `net.minecraft.client.*` inside server-facing platform/service packages

Do not expose these APIs through common bridge interfaces:

- NeoForge `CustomPacketPayload`, `PayloadRegistrar`, `IPayloadContext`
- Forge `SimpleChannel`, `NetworkEvent.Context`, `NetworkHooks`
- NeoForge `DataComponentType`, attachments, deferred holders
- Forge capabilities or loader event types
- Curios API types
- Loader-specific client event types

If a common API needs one of these types, the abstraction is probably too low-level.

## Service Split

Server-facing services live under:

- `com.dreamingfish.gridinventory.platform`

These classes are loaded on dedicated servers. They must not reference client-only bridge classes or `net.minecraft.client` types.

Client-only services live under:

- `com.dreamingfish.gridinventory.client.platform`

Client bridges must be initialized only from target client setup/event classes guarded by the target loader's client-only mechanism.

## Common Should Express Semantics

Common code should call semantic operations such as:

- `GridInventoryServices.itemStackData().getGridInventory(stack)`
- `GridInventoryServices.playerData().copyPlayerGridInventory(player)`
- `GridInventoryServices.menus().openPlayerGridInventory(player, data)`
- `GridInventoryServices.network().syncGridInventory(player, data)`
- `GridInventoryServices.accessories().quickEquip(player, stack)`

Common code should not call loader mechanics such as:

- `stack.get(DataComponentType)`
- `player.getData(AttachmentType)`
- `NetworkHooks.openScreen`
- `PacketDistributor.sendToPlayer`
- `CuriosApi.getCuriosInventory`

The target adapter is responsible for mapping common semantics to the loader/version API.

## Protocol Rules

Packet ids, directions, and field order must remain compatible with the 1.21.1 baseline unless a migration plan explicitly changes them.

Common message classes should contain:

- semantic id/type
- fields
- handler behavior

Target code should contain:

- buffer codecs
- payload wrappers
- SimpleChannel or CustomPacketPayload registration
- loader network context wrappers

Do not use reflection codecs as a final solution. Missing codecs should fail clearly with the message id.

## Menu Data Rules

`GridInventoryMenuOpenData` is a pure semantic record. It must not depend on `RegistryFriendlyByteBuf`, `StreamCodec`, or Forge/NeoForge menu factory types.

Target codecs own menu buffer encoding. The field order is:

1. `sourceSlot`
2. `hand`
3. `playerInventory`
4. `data`

## Item And Player Data Rules

Common item/player logic must use bridges.

NeoForge 1.21.1 stores:

- item data in data components
- player grid inventory in attachment `player_grid_inventory`

Forge 1.20.1 currently stores:

- item data in ItemStack NBT under `df_grid_inventory`
- player data in persistent NBT as a temporary bridge implementation

Do not move NeoForge data component or attachment types back into common.

## UI Rules

Common UI may contain shared screen layout, rendering, animation, and interaction math. It must not contain target event registration or loader-specific screen registration.

When changing common UI, check both targets because the same UI source is compiled and used by Forge 1.20.1 and NeoForge 1.21.1.

Important UI interaction rules:

- Collapsed storage cards must not register hidden hit regions.
- Hidden/collapsed pocket grid entries must not show tooltip or accept interaction.
- While dragging any item, tooltips from other items/panels should be suppressed.
- Target-specific rendering helpers, if they require client-only APIs, belong in client bridge classes.

## Mixin Rules

Mixin configs and mixin classes should be target-owned unless proven stable across all supported Minecraft versions.

Preferred locations:

- `targets/neoforge-1.21.1/src/main/java/.../mixin`
- `targets/forge-1.20.1/src/main/java/.../mixin`

`common` should not carry mixin configs. Avoid common mixin classes unless they use only stable Minecraft symbols and are verified against every target.

## Review Checklist

Before accepting a bridge-related change, run:

```bash
./gradlew :targets:neoforge-1.21.1:compileJava
./gradlew :targets:forge-1.20.1:compileJava
```

Then check common pollution:

```bash
grep -R "net.neoforged" common/src/main/java
grep -R "net.minecraftforge" common/src/main/java
grep -R "net.fabricmc" common/src/main/java
grep -R "top.theillusivec4.curios" common/src/main/java
grep -R "CustomPacketPayload" common/src/main/java
grep -R "StreamCodec" common/src/main/java
grep -R "RegistryFriendlyByteBuf" common/src/main/java
grep -R "DataComponentType" common/src/main/java
grep -R "SimpleChannel" common/src/main/java
```

Check target cross-contamination:

```bash
grep -R "net.neoforged" targets/forge-1.20.1/src/main/java
grep -R "net.minecraftforge" targets/neoforge-1.21.1/src/main/java
```

Expected result: no loader-specific imports outside their target, except comments or explicitly documented compatibility stubs.

## Baseline Rule

NeoForge 1.21.1 is the behavior baseline. When abstracting logic into common, first map the current 1.21.1 behavior from source or `docs/current-1.21.1-logic-map.md`, then move only the semantic part into common.

Do not make Forge 1.20.1 work by weakening NeoForge 1.21.1 behavior.
