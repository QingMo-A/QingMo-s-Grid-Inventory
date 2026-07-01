package com.dreamingfish.gridinventory.common.raid.command;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.raid.config.*;
import com.dreamingfish.gridinventory.common.raid.runtime.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import com.dreamingfish.gridinventory.common.registry.ModBlocks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class QmRaidCommands {
    // TODO Phase 48D: auto-load RaidManifestStorage during server starting event.
    // TODO Phase 48B: add /qmraid reset to clear applied containers and restore placeholders.
    // TODO Phase 49A: spawn anchors and spawn group selection.
    // TODO Phase 48A: transition lifecycle on join/end/reset for RUNNING, ENDED and CLEANED.
    private static final AtomicLong RAID_IDS = new AtomicLong(System.currentTimeMillis());
    private QmRaidCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RaidMapConfigRegistry.reload(Path.of("config"));
        dispatcher.register(Commands.literal("qmraid").requires(s -> s.hasPermission(2))
                .then(Commands.literal("map")
                        .then(Commands.literal("reload").executes(c -> reload(c.getSource())))
                        .then(Commands.literal("list").executes(c -> list(c.getSource())))
                        .then(Commands.literal("example").then(Commands.argument("mapId", StringArgumentType.word())
                                .executes(c -> example(c.getSource(), StringArgumentType.getString(c, "mapId")))))
                        .then(Commands.literal("validate_world").then(Commands.argument("mapId", StringArgumentType.word())
                                .executes(c -> validateWorld(c.getSource(), StringArgumentType.getString(c, "mapId")))))
                        .then(Commands.literal("validate").then(Commands.argument("mapId", StringArgumentType.word())
                                .executes(c -> validate(c.getSource(), StringArgumentType.getString(c, "mapId"))))))
                .then(Commands.literal("manifest")
                        .then(Commands.literal("list").executes(c -> manifestList(c.getSource())))
                        .then(Commands.literal("reload").executes(c -> manifestReload(c.getSource())))
                        .then(Commands.literal("save").then(Commands.argument("raid", StringArgumentType.word())
                                .executes(c -> manifestSave(c.getSource(), StringArgumentType.getString(c, "raid")))))
                        .then(Commands.literal("load").then(Commands.argument("raidId", LongArgumentType.longArg())
                                .executes(c -> manifestLoad(c.getSource(), LongArgumentType.getLong(c, "raidId"))))))
                .then(Commands.literal("create").then(Commands.argument("mapId", StringArgumentType.word())
                        .executes(c -> create(c.getSource(), StringArgumentType.getString(c, "mapId"), System.currentTimeMillis()))
                        .then(Commands.argument("seed", LongArgumentType.longArg())
                                .executes(c -> create(c.getSource(), StringArgumentType.getString(c, "mapId"),
                                        LongArgumentType.getLong(c, "seed"))))
                        .then(Commands.literal("seed").then(Commands.argument("seedValue", LongArgumentType.longArg())
                                .executes(c -> create(c.getSource(), StringArgumentType.getString(c, "mapId"),
                                        LongArgumentType.getLong(c, "seedValue")))))))
                .then(Commands.literal("preview").then(Commands.argument("raid", StringArgumentType.word())
                        .executes(c -> preview(c.getSource(), StringArgumentType.getString(c, "raid")))))
                .then(Commands.literal("nav")
                        .then(Commands.literal("preview").then(Commands.argument("raid", StringArgumentType.word())
                                .executes(c -> navigationPreview(c.getSource(),
                                        StringArgumentType.getString(c, "raid"))))))
                .then(Commands.literal("apply").then(Commands.argument("raid", StringArgumentType.word())
                        .executes(c -> apply(c.getSource(), StringArgumentType.getString(c, "raid")))))
                .then(Commands.literal("reset").then(Commands.argument("raid", StringArgumentType.word())
                        .executes(c -> reset(c.getSource(), StringArgumentType.getString(c, "raid"), false))))
                .then(Commands.literal("rebuild").then(Commands.argument("raid", StringArgumentType.word())
                        .executes(c -> reset(c.getSource(), StringArgumentType.getString(c, "raid"), true))))
                .then(Commands.literal("join").then(Commands.argument("raid", StringArgumentType.word())
                        .executes(c -> join(c.getSource(), StringArgumentType.getString(c, "raid"),
                                c.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> join(c.getSource(), StringArgumentType.getString(c, "raid"),
                                        EntityArgument.getPlayer(c, "player"))))))
                .then(Commands.literal("inspect").executes(c -> inspect(c.getSource()))));
    }

    private static int reload(CommandSourceStack source) {
        RaidMapConfigRegistry.reload(Path.of("config"));
        source.sendSuccess(() -> Component.literal("Loaded maps=" + RaidMapConfigRegistry.all().size()
                + " errors=" + RaidMapConfigRegistry.errors().size()), true);
        return 1;
    }
    private static int list(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Raid maps: " + RaidMapConfigRegistry.all().stream()
                .map(RaidMapConfig::id).toList()), false);
        return RaidMapConfigRegistry.all().size();
    }
    private static int validate(CommandSourceStack source, String id) {
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(id);
        if (map.isEmpty()) { source.sendFailure(Component.literal("Unknown/invalid map: " + id + " " + RaidMapConfigRegistry.errors().getOrDefault(id, ""))); return 0; }
        RaidMapValidationResult result = RaidMapConfigValidator.validate(id, map.get());
        source.sendSuccess(() -> Component.literal("Map " + id + " validate: zones=" + map.get().zones().size()
                + " containerTypes=" + map.get().containerTypes().size() + " containerAnchors="
                + map.get().containerAnchors().size() + " errors=" + result.errors() + " warnings=" + result.warnings()), false);
        result.issues().forEach(i -> source.sendSuccess(() -> Component.literal((i.error() ? "ERROR: " : "WARN: ") + i.message()), false));
        return result.valid() ? 1 : 0;
    }
    private static int example(CommandSourceStack source, String id) {
        BlockPos origin = BlockPos.containing(source.getPosition());
        try {
            Path directory = RaidMapExampleWriter.write(Path.of("config"), id,
                    source.getLevel().dimension().location().toString(), origin);
            source.sendSuccess(() -> Component.literal("Created example at " + directory
                    + ". Next: /qmraid map reload; /qmraid map validate " + id
                    + "; /qmraid create " + id + " seed 666; /qmraid apply latest; /qmraid join latest"
                    + ". Origin is your current world position; anchors are local positions."), true);
            return 1;
        } catch (IOException exception) {
            source.sendFailure(Component.literal("Example not written (existing files are never overwritten): "
                    + exception.getMessage()));
            return 0;
        }
    }
    private static int validateWorld(CommandSourceStack source, String id) {
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(id);
        if (map.isEmpty()) { source.sendFailure(Component.literal("Unknown map: " + id)); return 0; }
        ResourceLocation dimension = ResourceLocation.tryParse(map.get().dimension());
        var targetLevel = dimension == null ? null
                : source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
        if (targetLevel == null) { source.sendFailure(Component.literal("Dimension not loaded: " + map.get().dimension())); return 0; }
        int placeholders = 0, air = 0, containers = 0, unexpected = 0;
        for (RaidContainerAnchorConfig anchor : map.get().containerAnchors()) {
            if (!anchor.enabled()) continue;
            BlockPos localPos = anchor.localBlockPos();
            BlockPos worldPos = map.get().toWorldPos(localPos);
            var block = targetLevel.getBlockState(worldPos).getBlock();
            if (block == ModBlocks.RAID_CONTAINER_PLACEHOLDER.get()) placeholders++;
            else if (block == Blocks.AIR) air++;
            else if (block == ModBlocks.SEARCHABLE_GRID_CONTAINER.get()) containers++;
            else {
                unexpected++;
                source.sendSuccess(() -> Component.literal("WARN anchor " + anchor.id()
                        + " expected placeholder/air/searchable_container but found " + block
                        + " local=" + localPos.toShortString() + " world=" + worldPos.toShortString()), false);
            }
        }
        int p = placeholders, a = air, c = containers, u = unexpected;
        source.sendSuccess(() -> Component.literal("Validate world " + id + ": anchors="
                + map.get().containerAnchors().stream().filter(RaidContainerAnchorConfig::enabled).count()
                + " placeholders=" + p + " air=" + a + " searchableContainers=" + c + " unexpected=" + u), false);
        return unexpected == 0 ? 1 : 0;
    }
    private static int create(CommandSourceStack source, String id, long seed) {
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(id);
        if (map.isEmpty()) { source.sendFailure(Component.literal("Unknown map: " + id)); return 0; }
        long raidId = RAID_IDS.incrementAndGet();
        RaidManifest manifest = RaidManifestGenerator.generate(map.get(), raidId, seed);
        RaidManifestRegistry.put(manifest);
        saveManifest(source, manifest);
        source.sendSuccess(() -> Component.literal("Raid " + raidId + " map=" + id + " seed=" + seed
                + " activeContainers=" + manifest.activeContainers().size()
                + " variants=" + manifest.variantSelections().size()
                + " extractions=" + manifest.activeExtractions().size()
                + " spawns=" + manifest.activeSpawns().size()), true);
        if (manifest.activeSpawns().isEmpty()) {
            source.sendSuccess(() -> Component.literal(
                    "WARN: raid has no active spawns; join will use default_spawn fallback."), true);
        }
        if (manifest.activeExtractions().isEmpty()) {
            source.sendSuccess(() -> Component.literal("WARN: raid has no active extractions."), true);
        }
        RaidNavigationReport navigation = RaidNavigationEvaluator.evaluate(map.get(), manifest);
        if (navigation.hasFailedRequiredChecks()) {
            source.sendSuccess(() -> Component.literal("WARN: Raid created, but navigation has "
                    + navigation.failedRequiredChecks() + " failed required checks."), true);
        }
        return 1;
    }
    private static Optional<RaidManifest> manifest(String value) {
        if ("latest".equalsIgnoreCase(value)) return RaidManifestRegistry.latest();
        try { return RaidManifestRegistry.get(Long.parseLong(value)); } catch (NumberFormatException e) { return Optional.empty(); }
    }
    private static int preview(CommandSourceStack source, String value) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        RaidManifest raid = manifest.get();
        source.sendSuccess(() -> Component.literal("Raid " + raid.raidId() + " map=" + raid.mapId()
                + " seed=" + raid.raidSeed() + " state=" + raid.state() + " dimension=" + raid.dimensionId()
                + " pasteOrigin=" + raid.pasteOrigin().toShortString()
                + " defaultSpawnLocal=" + raid.defaultSpawnLocal().toShortString()
                + " defaultSpawnWorld=" + raid.defaultSpawnWorld().toShortString()
                + " activeContainers=" + raid.activeContainers().size()
                + " variants=" + raid.variantSelections().size()
                + " extractions=" + raid.activeExtractions().size()
                + " spawns=" + raid.activeSpawns().size()), false);
        raid.variantSelections().forEach(selection -> source.sendSuccess(() -> Component.literal(
                "variant " + selection.groupId() + "=" + selection.variantId()
                        + " tags=" + selection.tags() + " patches=" + selection.patches().size()), false));
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(raid.mapId());
        if (map.isPresent()) {
            RaidNavigationReport navigation = RaidNavigationEvaluator.evaluate(map.get(), raid);
            source.sendSuccess(() -> Component.literal("navigation nodes=" + navigation.nodes()
                    + " checks=" + navigation.checks().size()
                    + " failedRequired=" + navigation.failedRequiredChecks()), false);
        }
        raid.activeExtractions().forEach(extraction -> {
            BlockPos worldPos = raid.toWorldPos(extraction.localPos());
            source.sendSuccess(() -> Component.literal("extraction " + extraction.id()
                    + " node=" + extraction.node()
                    + " local=" + extraction.localPos().toShortString()
                    + " world=" + worldPos.toShortString()
                    + " radius=" + extraction.radius() + " tags=" + extraction.tags()), false);
        });
        raid.activeSpawns().forEach(spawn -> {
            BlockPos worldPos = raid.toWorldPos(spawn.localPos());
            source.sendSuccess(() -> Component.literal("spawn " + spawn.id()
                    + " node=" + spawn.node()
                    + " local=" + spawn.localPos().toShortString()
                    + " world=" + worldPos.toShortString()
                    + " yaw=" + spawn.yaw() + " pitch=" + spawn.pitch()
                    + " tags=" + spawn.tags()), false);
        });
        raid.zones().values().forEach(zone -> source.sendSuccess(() -> Component.literal("zone " + zone.zoneId()
                + " budget=" + zone.lootBudget() + " active=" + zone.activeContainerCount()
                + " anchors=" + zone.anchorBudgets()), false));
        raid.activeContainers().forEach(anchor -> {
            BlockPos worldPos = raid.toWorldPos(anchor.localPos());
            source.sendSuccess(() -> Component.literal("active " + anchor.anchorId() + " zone=" + anchor.zoneId()
                    + " budget=" + anchor.pointBudget() + " local=" + anchor.localPos().toShortString()
                    + " world=" + worldPos.toShortString()), false);
        });
        return 1;
    }

    private static int navigationPreview(CommandSourceStack source, String value) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) {
            source.sendFailure(Component.literal("Raid not found: " + value));
            return 0;
        }
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(manifest.get().mapId());
        if (map.isEmpty()) {
            source.sendFailure(Component.literal("Map config not loaded: " + manifest.get().mapId()));
            return 0;
        }
        RaidNavigationReport report = RaidNavigationEvaluator.evaluate(map.get(), manifest.get());
        source.sendSuccess(() -> Component.literal("Navigation raid " + manifest.get().raidId()
                + " map=" + manifest.get().mapId() + ": nodes=" + report.nodes()
                + " enabledEdges=" + report.enabledEdges() + " disabledEdges=" + report.disabledEdges()
                + " checks=" + report.checks().size()
                + " activeSpawns=" + manifest.get().activeSpawns().size()
                + " activeExtractions=" + manifest.get().activeExtractions().size()
                + " failedRequired=" + report.failedRequiredChecks()), false);
        report.checks().forEach(check -> source.sendSuccess(() -> Component.literal(
                (check.required() && !check.reachable() ? "REQUIRED FAILED " : "")
                        + "check " + check.id() + " from=" + check.from() + " target=" + check.target()
                        + " required=" + check.required() + " reachable=" + check.reachable()), false));
        return report.hasFailedRequiredChecks() ? 0 : 1;
    }
    private static int apply(CommandSourceStack source, String value) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(manifest.get().mapId());
        if (map.isEmpty()) { source.sendFailure(Component.literal("Map config not loaded: " + manifest.get().mapId())); return 0; }
        var targetLevel = raidLevel(source, manifest.get());
        if (targetLevel == null) return 0;
        RaidWorldApplyResult result = RaidWorldApplier.applyFull(targetLevel, map.get(), manifest.get());
        RaidManifest updated = manifest.get().withState(RaidLifecycleState.APPLIED);
        RaidManifestRegistry.put(updated);
        saveManifest(source, updated);
        source.sendSuccess(() -> Component.literal("Applied raid " + manifest.get().raidId()
                + ": templateApplied=" + result.templateApplied() + " templateMissing=" + result.templateMissing()
                + " variantPatchesApplied=" + result.variantPatchesApplied()
                + " activePlaced=" + result.activePlaced() + " activeRebound=" + result.activeRebound()
                + " inactiveCleared=" + result.inactiveCleared() + " warnings=" + result.warnings()), true);
        return result.activePlaced() + result.activeRebound();
    }
    private static int manifestList(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Raid manifests loaded: " + RaidManifestRegistry.size()), false);
        RaidManifestRegistry.all().forEach(raid -> source.sendSuccess(() -> Component.literal("- "
                + raid.raidId() + " map=" + raid.mapId() + " state=" + raid.state()
                + " dimension=" + raid.dimensionId()), false));
        return RaidManifestRegistry.size();
    }
    private static int reset(CommandSourceStack source, String value, boolean rebuild) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(manifest.get().mapId());
        if (map.isEmpty()) { source.sendFailure(Component.literal("Map config not loaded: " + manifest.get().mapId())); return 0; }
        var targetLevel = raidLevel(source, manifest.get());
        if (targetLevel == null) return 0;
        RaidWorldResetResult result = RaidWorldResetter.reset(targetLevel, map.get(), manifest.get());
        if (result.refused()) {
            source.sendFailure(Component.literal("Raid reset refused because bounds volume exceeds "
                    + RaidWorldResetter.MAX_RESET_VOLUME));
            return 0;
        }
        RaidManifest updated = manifest.get().withState(RaidLifecycleState.CREATED);
        RaidManifestRegistry.put(updated);
        saveManifest(source, updated);
        source.sendSuccess(() -> Component.literal("Reset raid " + updated.raidId()
                + ": clearedBlocks=" + result.clearedBlocks()
                + " templateApplied=" + result.templateApplied()
                + " templateMissing=" + result.templateMissing()
                + " placeholdersRestored=" + result.placeholdersRestored()
                + " warnings=" + result.warnings() + " state=CREATED"), true);
        if (!rebuild) return 1;
        // Reset restored the base template; rebuild reapplies frozen variants before resources.
        RaidWorldApplyResult applied = RaidWorldApplier.applyGeneratedStateOnly(targetLevel, map.get(), updated);
        RaidManifest rebuilt = updated.withState(RaidLifecycleState.APPLIED);
        RaidManifestRegistry.put(rebuilt);
        saveManifest(source, rebuilt);
        source.sendSuccess(() -> Component.literal("Rebuilt raid " + rebuilt.raidId()
                + ": variantPatchesApplied=" + applied.variantPatchesApplied()
                + " activePlaced=" + applied.activePlaced() + " activeRebound=" + applied.activeRebound()
                + " inactiveCleared=" + applied.inactiveCleared() + " warnings=" + applied.warnings()
                + " state=APPLIED"), true);
        return applied.activePlaced() + applied.activeRebound();
    }
    private static int manifestReload(CommandSourceStack source) {
        RaidManifestRegistry.clear();
        RaidManifestStorage.loadAll(source.getServer()).forEach(RaidManifestRegistry::put);
        source.sendSuccess(() -> Component.literal("Reloaded raid manifests: loaded="
                + RaidManifestRegistry.size()), true);
        return RaidManifestRegistry.size();
    }
    private static int manifestSave(CommandSourceStack source, String value) {
        Optional<RaidManifest> raid = manifest(value);
        if (raid.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        return saveManifest(source, raid.get()) ? 1 : 0;
    }
    private static int manifestLoad(CommandSourceStack source, long raidId) {
        Optional<RaidManifest> raid = RaidManifestStorage.load(source.getServer(), raidId);
        if (raid.isEmpty()) { source.sendFailure(Component.literal("Raid manifest not found: " + raidId)); return 0; }
        RaidManifestRegistry.put(raid.get());
        source.sendSuccess(() -> Component.literal("Loaded raid manifest " + raidId + " map="
                + raid.get().mapId() + " state=" + raid.get().state()), true);
        return 1;
    }
    private static boolean saveManifest(CommandSourceStack source, RaidManifest manifest) {
        try {
            RaidManifestStorage.save(source.getServer(), manifest);
            source.sendSuccess(() -> Component.literal("Saved raid manifest " + manifest.raidId()), false);
            return true;
        } catch (IOException exception) {
            com.dreamingfish.gridinventory.DFGridInventory.LOGGER.warn(
                    "Failed to save raid manifest {}", manifest.raidId(), exception);
            source.sendFailure(Component.literal("Failed to save raid manifest: " + exception.getMessage()));
            return false;
        }
    }
    private static int join(CommandSourceStack source, String value, ServerPlayer player) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        var level = raidLevel(source, manifest.get());
        if (level == null) return 0;
        RaidSpawnActivation activeSpawn = manifest.get().activeSpawns().isEmpty()
                ? null : manifest.get().activeSpawns().get(0);
        BlockPos localSpawn = activeSpawn != null
                ? activeSpawn.localPos() : manifest.get().defaultSpawnLocal();
        BlockPos worldSpawn = manifest.get().toWorldPos(localSpawn);
        float yaw = activeSpawn != null ? activeSpawn.yaw() : player.getYRot();
        float pitch = activeSpawn != null ? activeSpawn.pitch() : player.getXRot();
        String spawnId = activeSpawn != null ? activeSpawn.id() : "default_spawn_fallback";
        player.teleportTo(level, worldSpawn.getX() + 0.5D, worldSpawn.getY(), worldSpawn.getZ() + 0.5D,
                yaw, pitch);
        source.sendSuccess(() -> Component.literal("Joined raid " + manifest.get().raidId() + " map="
                + manifest.get().mapId() + " spawn=" + spawnId
                + " localSpawn=" + localSpawn.toShortString()
                + " worldSpawn=" + worldSpawn.toShortString()
                + " player=" + player.getName().getString()), true);
        return 1;
    }
    private static net.minecraft.server.level.ServerLevel raidLevel(CommandSourceStack source, RaidManifest manifest) {
        ResourceLocation id = ResourceLocation.tryParse(manifest.dimensionId());
        if (id == null) { source.sendFailure(Component.literal("Invalid dimension: " + manifest.dimensionId())); return null; }
        var level = source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (level == null) source.sendFailure(Component.literal("Dimension not loaded: " + id));
        return level;
    }
    private static int inspect(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        BlockHitResult hit = (BlockHitResult) player.pick(8.0D, 0.0F, false);
        if (!(source.getLevel().getBlockEntity(hit.getBlockPos()) instanceof SearchableGridContainerBlockEntity c)) {
            source.sendFailure(Component.literal("Not looking at a searchable container")); return 0;
        }
        source.sendSuccess(() -> Component.literal("raidId=" + c.getRaidId() + " mapId=" + c.getMapId()
                + " zoneId=" + c.getZoneId() + " anchorId=" + c.getAnchorId() + " containerType="
                + c.getContainerType() + " lootSeed=" + c.getLootSeed() + " pointBudget=" + c.getPointBudget()
                + " qualityMultiplier=" + c.getQualityMultiplier() + " lootGenerated=" + c.isLootGenerated()), false);
        return 1;
    }
}
