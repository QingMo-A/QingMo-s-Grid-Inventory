package com.dreamingfish.gridinventory.common.raid.command;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.raid.config.*;
import com.dreamingfish.gridinventory.common.raid.runtime.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
    private static final AtomicLong RAID_IDS = new AtomicLong(System.currentTimeMillis());
    private QmRaidCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RaidMapConfigRegistry.reload(Path.of("config"));
        RaidLootItemDefinitionRegistry.reload(Path.of("config"));
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
                .then(Commands.literal("loot")
                        .then(Commands.literal("reload").executes(c -> lootReload(c.getSource())))
                        .then(Commands.literal("list").executes(c -> lootList(c.getSource())))
                        .then(Commands.literal("validate").executes(c -> lootValidate(c.getSource())))
                        .then(Commands.literal("categories").executes(c -> lootCategories(c.getSource())))
                        .then(Commands.literal("rarities").executes(c -> lootRarities(c.getSource())))
                        .then(Commands.literal("example").executes(c -> lootExample(c.getSource())))
                        .then(Commands.literal("simulate")
                                .then(Commands.argument("containerTypeId", StringArgumentType.word())
                                        .then(Commands.argument("budget", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                                        .executes(c -> lootSimulate(c.getSource(),
                                                                StringArgumentType.getString(c, "containerTypeId"),
                                                                IntegerArgumentType.getInteger(c, "budget"),
                                                                LongArgumentType.getLong(c, "seed"),
                                                                1.0D))
                                                        .then(Commands.argument("qualityMultiplier",
                                                                        DoubleArgumentType.doubleArg(0.01D))
                                                                .executes(c -> lootSimulate(c.getSource(),
                                                                        StringArgumentType.getString(c,
                                                                                "containerTypeId"),
                                                                        IntegerArgumentType.getInteger(c, "budget"),
                                                                        LongArgumentType.getLong(c, "seed"),
                                                                        DoubleArgumentType.getDouble(c,
                                                                                "qualityMultiplier"))))))))
                        .then(Commands.literal("candidates")
                                .then(Commands.argument("containerTypeId", StringArgumentType.word())
                                        .executes(c -> lootCandidates(c.getSource(),
                                                StringArgumentType.getString(c, "containerTypeId")))))
                        .then(Commands.literal("loose_preview")
                                .then(Commands.argument("raid", StringArgumentType.word())
                                        .executes(c -> looseLootPreview(c.getSource(),
                                                StringArgumentType.getString(c, "raid")))))
                        .then(Commands.literal("inspect")
                                .then(Commands.argument("itemId", StringArgumentType.word())
                                        .executes(c -> lootInspect(c.getSource(),
                                                StringArgumentType.getString(c, "itemId"))))))
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
                .then(Commands.literal("extract")
                        .then(Commands.literal("check")
                                .then(Commands.argument("raid", StringArgumentType.word())
                                        .executes(c -> extractionCheck(c.getSource(),
                                                StringArgumentType.getString(c, "raid"),
                                                c.getSource().getPlayerOrException()))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(c -> extractionCheck(c.getSource(),
                                                        StringArgumentType.getString(c, "raid"),
                                                        EntityArgument.getPlayer(c, "player"))))))
                        .then(Commands.argument("raid", StringArgumentType.word())
                                .executes(c -> extract(c.getSource(), StringArgumentType.getString(c, "raid"),
                                        c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> extract(c.getSource(),
                                                StringArgumentType.getString(c, "raid"),
                                        EntityArgument.getPlayer(c, "player"))))))
                .then(Commands.literal("extraction")
                        .then(Commands.literal("rules")
                                .then(Commands.argument("raid", StringArgumentType.word())
                                        .executes(c -> extractionRules(c.getSource(),
                                                StringArgumentType.getString(c, "raid")))))
                        .then(Commands.literal("trigger").then(Commands.argument("raid", StringArgumentType.word())
                                .then(Commands.argument("extractionId", StringArgumentType.word())
                                        .executes(c -> extractionTrigger(c.getSource(),
                                                StringArgumentType.getString(c, "raid"),
                                                StringArgumentType.getString(c, "extractionId"), null))
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(c -> extractionTrigger(c.getSource(),
                                                        StringArgumentType.getString(c, "raid"),
                                                        StringArgumentType.getString(c, "extractionId"),
                                                        EntityArgument.getPlayer(c, "player")))))))
                        .then(Commands.literal("countdowns").executes(c -> extractionCountdowns(c.getSource())))
                        .then(Commands.literal("cancel").then(Commands.argument("player", EntityArgument.player())
                                .executes(c -> extractionCancel(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("cancel_global").then(Commands.argument("raid", StringArgumentType.word())
                                .then(Commands.argument("extractionId", StringArgumentType.word())
                                        .executes(c -> extractionCancelGlobal(c.getSource(),
                                                StringArgumentType.getString(c, "raid"),
                                                StringArgumentType.getString(c, "extractionId")))))))
                .then(Commands.literal("inspect").executes(c -> inspect(c.getSource()))));
    }

    private static int lootReload(CommandSourceStack source) {
        RaidLootItemDefinitionRegistry.reload(Path.of("config"));
        source.sendSuccess(() -> Component.literal("Loaded raid loot item definitions: total="
                + RaidLootItemDefinitionRegistry.all().size()
                + " enabled=" + RaidLootItemDefinitionRegistry.enabled().size()
                + " errors=" + RaidLootItemDefinitionRegistry.validation().errors()), true);
        return RaidLootItemDefinitionRegistry.validation().valid() ? 1 : 0;
    }

    private static int lootList(CommandSourceStack source) {
        RaidLootItemDefinitionRegistry.all().stream().limit(20).forEach(definition ->
                source.sendSuccess(() -> Component.literal(formatLootDefinition(definition)), false));
        source.sendSuccess(() -> Component.literal("Raid loot item definitions: total="
                + RaidLootItemDefinitionRegistry.all().size() + " showing="
                + Math.min(20, RaidLootItemDefinitionRegistry.all().size())), false);
        return RaidLootItemDefinitionRegistry.all().size();
    }

    private static int lootValidate(CommandSourceStack source) {
        RaidLootItemDefinitionValidationResult result = RaidLootItemDefinitionRegistry.validation();
        source.sendSuccess(() -> Component.literal("Raid loot item definitions validate: total="
                + RaidLootItemDefinitionRegistry.all().size()
                + " enabled=" + RaidLootItemDefinitionRegistry.enabled().size()
                + " errors=" + result.errors() + " warnings=" + result.warnings()), false);
        result.issues().forEach(issue -> source.sendSuccess(() -> Component.literal(
                (issue.error() ? "ERROR: " : "WARN: ") + issue.message()), false));
        return result.valid() ? 1 : 0;
    }

    private static int lootInspect(CommandSourceStack source, String value) {
        ResourceLocation item = ResourceLocation.tryParse(value);
        if (item == null) {
            source.sendFailure(Component.literal("Invalid item id: " + value));
            return 0;
        }
        Optional<RaidLootItemDefinitionConfig> definition = RaidLootItemDefinitionRegistry.get(item);
        if (definition.isEmpty()) {
            source.sendFailure(Component.literal("Unknown raid loot item definition: " + value));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(formatLootDefinition(definition.get())
                + " combatScore=" + definition.get().combatScore()
                + " survivalScore=" + definition.get().survivalScore()), false);
        return 1;
    }

    private static int lootCategories(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Raid loot categories: "
                + RaidLootItemDefinitionRegistry.categoryCounts()), false);
        return RaidLootItemDefinitionRegistry.categoryCounts().size();
    }

    private static int lootRarities(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Raid loot rarities: "
                + RaidLootItemDefinitionRegistry.rarityCounts()), false);
        return RaidLootItemDefinitionRegistry.rarityCounts().size();
    }

    private static int lootExample(CommandSourceStack source) {
        try {
            Path path = RaidLootItemDefinitionExampleWriter.write(Path.of("config"));
            RaidLootItemDefinitionRegistry.reload(Path.of("config"));
            source.sendSuccess(() -> Component.literal("Created raid loot item definition example at " + path), true);
            return 1;
        } catch (IOException exception) {
            source.sendFailure(Component.literal("Loot example not written (existing file is never overwritten): "
                    + exception.getMessage()));
            return 0;
        }
    }

    private static int lootSimulate(
            CommandSourceStack source, String containerTypeId, int budget, long seed, double qualityMultiplier) {
        Optional<RaidContainerTypeConfig> type = RaidMapConfigRegistry.all().stream()
                .flatMap(map -> map.containerTypes().stream())
                .filter(candidate -> candidate.id().equals(containerTypeId))
                .findFirst();
        if (type.isEmpty()) {
            source.sendFailure(Component.literal("Unknown container type: " + containerTypeId));
            return 0;
        }
        RaidBudgetLootResult result = RaidBudgetLootGenerator.generate(new RaidBudgetLootContext(
                "simulate", containerTypeId, budget, qualityMultiplier, seed,
                Math.max(1, type.get().columns() * type.get().rows()),
                type.get().allowedCategories(), RaidLootItemDefinitionRegistry.enabled()));
        if (!result.generatedAny()) {
            source.sendFailure(Component.literal(
                    "No eligible loot definitions for container type: " + containerTypeId
                            + " candidates=" + result.candidateCount()
                            + " pointBudget=" + budget
                            + " qualityMultiplier=" + qualityMultiplier
                            + " effectiveBudget=" + result.requestedBudget()));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Simulated loot containerType=" + containerTypeId
                + " pointBudget=" + budget
                + " qualityMultiplier=" + qualityMultiplier
                + " effectiveBudget=" + result.requestedBudget()
                + " seed=" + seed + " candidates=" + result.candidateCount()
                + " generated=" + result.entries().size()
                + " consumed=" + result.consumedBudget() + " remaining=" + result.remainingBudget()
                + " attempts=" + result.attempts() + " warnings=" + result.warnings()), false);
        result.entries().forEach(entry -> source.sendSuccess(() -> Component.literal(
                "item " + entry.definition().item() + " x" + entry.stack().getCount()
                        + " value=" + entry.consumedValue()
                        + " category=" + entry.definition().category()
                        + " rarity=" + entry.definition().rarity()), false));
        return result.entries().size();
    }

    private static int lootCandidates(CommandSourceStack source, String containerTypeId) {
        Optional<RaidContainerTypeConfig> type = RaidMapConfigRegistry.all().stream()
                .flatMap(map -> map.containerTypes().stream())
                .filter(candidate -> candidate.id().equals(containerTypeId))
                .findFirst();
        if (type.isEmpty()) {
            source.sendFailure(Component.literal("Unknown container type: " + containerTypeId));
            return 0;
        }
        var candidates = RaidBudgetLootGenerator.eligibleDefinitions(
                RaidLootItemDefinitionRegistry.enabled(), type.get().allowedCategories());
        source.sendSuccess(() -> Component.literal("Loot candidates containerType=" + containerTypeId
                + " allowedCategories=" + type.get().allowedCategories()
                + " candidates=" + candidates.size()), false);
        if (candidates.isEmpty()) {
            source.sendFailure(Component.literal("No eligible loot candidates for container type: "
                    + containerTypeId));
            return 0;
        }
        candidates.stream().limit(20).forEach(definition -> source.sendSuccess(() -> Component.literal(
                definition.item() + " category=" + definition.category()
                        + " rarity=" + definition.rarity()
                        + " systemValue=" + definition.systemValue()
                        + " spawnWeight=" + definition.spawnWeight()
                        + " stack=" + definition.stackMin() + "-" + definition.stackMax()
                        + " registered=" + BuiltInRegistries.ITEM.containsKey(definition.item())
                        + " tags=" + definition.tags()), false));
        return candidates.size();
    }

    private static int looseLootPreview(CommandSourceStack source, String value) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(manifest.get().mapId());
        if (map.isEmpty()) { source.sendFailure(Component.literal("Map config not loaded: " + manifest.get().mapId())); return 0; }
        var types = map.get().containerTypes().stream().collect(
                java.util.stream.Collectors.toMap(RaidContainerTypeConfig::id, type -> type, (a, b) -> a));
        int stacks = 0, consumed = 0, warnings = 0, candidates = 0, printed = 0;
        for (RaidLooseLootActivation anchor : manifest.get().activeLooseLoot()) {
            RaidContainerTypeConfig type = types.get(anchor.containerType());
            if (type == null) continue;
            RaidBudgetLootResult result = RaidBudgetLootGenerator.generate(new RaidBudgetLootContext(
                    map.get().id(), type.id(), anchor.pointBudget(), anchor.qualityMultiplier(),
                    anchor.lootSeed(), Math.min(3, Math.max(1, type.columns() * type.rows())),
                    type.allowedCategories(), RaidLootItemDefinitionRegistry.enabled()));
            stacks += result.entries().size();
            consumed += result.consumedBudget();
            warnings += result.warnings();
            candidates += result.candidateCount();
            for (RaidBudgetLootEntry entry : result.entries()) {
                if (printed++ >= 50) continue;
                source.sendSuccess(() -> Component.literal("anchor " + anchor.anchorId()
                        + " item " + entry.definition().item() + " x" + entry.stack().getCount()
                        + " value=" + entry.consumedValue()), false);
            }
        }
        int finalStacks = stacks, finalConsumed = consumed, finalWarnings = warnings, finalCandidates = candidates;
        source.sendSuccess(() -> Component.literal("Loose loot preview raid=" + manifest.get().raidId()
                + " anchors=" + manifest.get().activeLooseLoot().size()
                + " stacks=" + finalStacks + " consumed=" + finalConsumed
                + " warnings=" + finalWarnings + " candidates=" + finalCandidates), false);
        return stacks;
    }

    private static String formatLootDefinition(RaidLootItemDefinitionConfig definition) {
        return definition.item() + " enabled=" + definition.enabled()
                + " category=" + definition.category() + " rarity=" + definition.rarity()
                + " systemValue=" + definition.systemValue()
                + " spawnWeight=" + definition.spawnWeight()
                + " stack=" + definition.stackMin() + "-" + definition.stackMax()
                + " tags=" + definition.tags();
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
                + " spawns=" + manifest.activeSpawns().size()
                + " looseLoot=" + manifest.activeLooseLoot().size()), true);
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
                + " spawns=" + raid.activeSpawns().size()
                + " looseLoot=" + raid.activeLooseLoot().size()
                + " participants=" + raid.participants().size()
                + " extractedPlayers=" + raid.extractedPlayers().size()), false);
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
            RaidExtractionRuntimeState runtime = raid.extractionStates().get(extraction.id());
            source.sendSuccess(() -> Component.literal("extraction " + extraction.id()
                    + " node=" + extraction.node()
                    + " local=" + extraction.localPos().toShortString()
                    + " world=" + worldPos.toShortString()
                    + " radius=" + extraction.radius() + " tags=" + extraction.tags()
                    + " availability=" + availabilitySummary(extraction)
                    + " trigger=" + triggerSummary(extraction)
                    + " timer=" + extraction.timer().type() + "/" + extraction.timer().seconds() + "s"
                    + " leave=" + extraction.timer().leaveBehavior()
                    + " useLimit=" + extraction.useLimit() + " consumeUseOn=" + extraction.consumeUseOn()
                    + " runtimeState=" + (runtime == null ? "READY" : runtime.state())
                    + " remainingUses=" + (runtime == null ? extraction.useLimit() : runtime.remainingUses())
                    + " triggeredAt=" + (runtime == null ? -1L : runtime.triggeredAtGameTime())
                    + " triggeredBy=" + (runtime == null || runtime.triggeredBy() == null ? "" : runtime.triggeredBy())), false);
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
        raid.activeLooseLoot().stream().limit(20).forEach(anchor -> {
            BlockPos worldPos = raid.toWorldPos(anchor.localPos());
            source.sendSuccess(() -> Component.literal("loose anchor=" + anchor.anchorId()
                    + " group=" + anchor.groupId() + " type=" + anchor.containerType()
                    + " budget=" + anchor.pointBudget() + " quality=" + anchor.qualityMultiplier()
                    + " local=" + anchor.localPos().toShortString()
                    + " world=" + worldPos.toShortString() + " tags=" + anchor.tags()), false);
        });
        raid.extractedPlayers().stream().limit(10).forEach(player -> source.sendSuccess(
                () -> Component.literal("extracted " + player.playerName()
                        + " uuid=" + player.playerId() + " extraction=" + player.extractionId()
                        + " at=" + player.extractedAtMillis()), false));
        raid.participants().stream().limit(10).forEach(participant -> source.sendSuccess(
                () -> Component.literal("participant " + participant.playerName()
                        + " uuid=" + participant.playerId()
                        + " joinedAt=" + participant.joinedAtMillis()), false));
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

    private static int extractionRules(CommandSourceStack source, String value) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        RaidManifest raid = manifest.get();
        source.sendSuccess(() -> Component.literal("Extraction rules raid=" + raid.raidId()
                + " active=" + raid.activeExtractions().size()), false);
        raid.activeExtractions().forEach(extraction -> {
            RaidExtractionRuntimeState runtime = raid.extractionStates().get(extraction.id());
            source.sendSuccess(() -> Component.literal("- " + extraction.id()
                    + " availability=" + availabilitySummary(extraction)
                    + " trigger=" + triggerSummary(extraction)
                    + " timer=" + extraction.timer().type() + "/" + extraction.timer().seconds() + "s"
                    + " leave=" + extraction.timer().leaveBehavior() + " useLimit=" + extraction.useLimit()
                    + " consumeUseOn=" + extraction.consumeUseOn()
                    + " state=" + (runtime == null ? "READY" : runtime.state())
                    + " remaining=" + (runtime == null ? extraction.useLimit() : runtime.remainingUses())
                    + " triggeredAt=" + (runtime == null ? -1L : runtime.triggeredAtGameTime())
                    + " triggeredBy=" + (runtime == null || runtime.triggeredBy() == null ? "" : runtime.triggeredBy())), false);
        });
        return raid.activeExtractions().size();
    }

    private static String triggerSummary(RaidExtractionActivation extraction) {
        if ("switch".equals(extraction.trigger().type())) return "switch/" + extraction.trigger().switchId();
        if ("item_turn_in".equals(extraction.trigger().type())) return "item_turn_in requirements="
                + extraction.trigger().requirements().stream()
                .map(item -> item.item() + " x" + item.count()).toList();
        return extraction.trigger().type();
    }

    private static String availabilitySummary(RaidExtractionActivation extraction) {
        return "raid_remaining_lte".equals(extraction.availability().type())
                ? extraction.availability().type() + "/" + extraction.availability().seconds() + "s"
                : extraction.availability().type();
    }

    private static int extractionTrigger(CommandSourceStack source, String raidValue,
                                         String extractionId, ServerPlayer player) {
        Optional<RaidManifest> raid = manifest(raidValue);
        if (raid.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + raidValue)); return 0; }
        String result = RaidExtractionCountdownService.trigger(source.getServer(), raid.get(), extractionId, player);
        if (result.startsWith("Triggered")) { source.sendSuccess(() -> Component.literal(result), true); return 1; }
        source.sendFailure(Component.literal(result)); return 0;
    }

    private static int extractionCountdowns(CommandSourceStack source) {
        long now = source.getServer().overworld().getGameTime();
        var players = RaidExtractionCountdownService.playerCountdowns();
        source.sendSuccess(() -> Component.literal("Player countdowns: " + players.size()), false);
        players.forEach(value -> source.sendSuccess(() -> Component.literal("player=" + value.playerName()
                + " raid=" + value.raidId() + " extraction=" + value.extractionId()
                + " elapsed=" + (now - value.startedGameTime()) / 20 + "s remaining="
                + Math.max(0, value.requiredTicks() - (now - value.startedGameTime())) / 20 + "s"
                + " lastSeenAgo=" + Math.max(0, now - value.lastSeenGameTime()) / 20 + "s"), false));
        var globals = RaidExtractionCountdownService.globalCountdowns();
        source.sendSuccess(() -> Component.literal("Global countdowns: " + globals.size()), false);
        globals.forEach(value -> {
            RaidExtractionRuntimeState state = RaidManifestRegistry.get(value.raidId())
                    .map(raid -> raid.extractionStates().get(value.extractionId())).orElse(null);
            source.sendSuccess(() -> Component.literal("raid=" + value.raidId()
                    + " extraction=" + value.extractionId() + " elapsed=" + (now - value.startedGameTime()) / 20
                    + "s remaining=" + Math.max(0, value.requiredTicks() - (now - value.startedGameTime())) / 20
                    + "s triggeredBy=" + value.triggeredByName()
                    + " state=" + (state == null ? "missing" : state.state())
                    + " remainingUses=" + (state == null ? "unknown" : state.remainingUses())), false);
        });
        return players.size() + globals.size();
    }

    private static int extractionCancel(CommandSourceStack source, ServerPlayer player) {
        RaidExtractionCountdownService.clearPlayer(player.getUUID());
        source.sendSuccess(() -> Component.literal("Cancelled extraction countdown for "
                + player.getName().getString()), true);
        return 1;
    }

    private static int extractionCancelGlobal(CommandSourceStack source, String raidValue, String extractionId) {
        Optional<RaidManifest> raid = manifest(raidValue);
        if (raid.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + raidValue)); return 0; }
        boolean cancelled = RaidExtractionCountdownService.cancelGlobal(
                source.getServer(), raid.get().raidId(), extractionId);
        if (!cancelled) { source.sendFailure(Component.literal("Global countdown not found.")); return 0; }
        source.sendSuccess(() -> Component.literal("Cancelled global extraction " + extractionId), true);
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
                + " inactiveCleared=" + result.inactiveCleared()
                + " looseLootEntitiesCleared=" + result.looseLootEntitiesCleared()
                + " looseLootAnchorsApplied=" + result.looseLootAnchorsApplied()
                + " looseLootStacksSpawned=" + result.looseLootStacksSpawned()
                + " looseLootConsumedBudget=" + result.looseLootConsumedBudget()
                + " warnings=" + result.warnings()), true);
        return result.activePlaced() + result.activeRebound();
    }
    private static int manifestList(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Raid manifests loaded: " + RaidManifestRegistry.size()), false);
        RaidManifestRegistry.all().forEach(raid -> source.sendSuccess(() -> Component.literal("- "
                + raid.raidId() + " map=" + raid.mapId() + " state=" + raid.state()
                + " dimension=" + raid.dimensionId()
                + " participants=" + raid.participants().size()
                + " extracted=" + raid.extractedPlayers().size()
                + " looseLoot=" + raid.activeLooseLoot().size()), false));
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
        RaidManifest updated = manifest.get().withClearedRunState(RaidLifecycleState.CREATED);
        RaidExtractionCountdownService.clearRaid(updated.raidId());
        RaidManifest resetManifest = RaidExtractionCountdownService.normalizeClearedRaid(updated);
        RaidManifestRegistry.put(resetManifest);
        saveManifest(source, resetManifest);
        source.sendSuccess(() -> Component.literal("Reset raid " + resetManifest.raidId()
                + ": clearedBlocks=" + result.clearedBlocks()
                + " templateApplied=" + result.templateApplied()
                + " templateMissing=" + result.templateMissing()
                + " placeholdersRestored=" + result.placeholdersRestored()
                + " looseLootEntitiesCleared=" + result.looseLootEntitiesCleared()
                + " warnings=" + result.warnings()
                + " participantsCleared=true extractedPlayersCleared=true state=CREATED"), true);
        if (!rebuild) return 1;
        // Reset restored the base template; rebuild reapplies frozen variants before resources.
        RaidWorldApplyResult applied = RaidWorldApplier.applyGeneratedStateOnly(targetLevel, map.get(), resetManifest);
        RaidManifest rebuilt = resetManifest.withState(RaidLifecycleState.APPLIED);
        RaidManifestRegistry.put(rebuilt);
        saveManifest(source, rebuilt);
        source.sendSuccess(() -> Component.literal("Rebuilt raid " + rebuilt.raidId()
                + ": variantPatchesApplied=" + applied.variantPatchesApplied()
                + " activePlaced=" + applied.activePlaced() + " activeRebound=" + applied.activeRebound()
                + " inactiveCleared=" + applied.inactiveCleared()
                + " looseLootEntitiesCleared=" + applied.looseLootEntitiesCleared()
                + " looseLootAnchorsApplied=" + applied.looseLootAnchorsApplied()
                + " looseLootStacksSpawned=" + applied.looseLootStacksSpawned()
                + " looseLootConsumedBudget=" + applied.looseLootConsumedBudget()
                + " warnings=" + applied.warnings()
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
        RaidManifest updated = manifest.get().withParticipant(new RaidParticipant(
                player.getUUID(), player.getName().getString(), System.currentTimeMillis()));
        if (updated.state() == RaidLifecycleState.APPLIED
                || updated.state() == RaidLifecycleState.CREATED) {
            updated = updated.withState(RaidLifecycleState.RUNNING);
        }
        RaidManifestRegistry.put(updated);
        if (!saveManifest(source, updated)) return 0;
        int participantCount = updated.participants().size();
        source.sendSuccess(() -> Component.literal("Joined raid " + manifest.get().raidId() + " map="
                + manifest.get().mapId() + " spawn=" + spawnId
                + " localSpawn=" + localSpawn.toShortString()
                + " worldSpawn=" + worldSpawn.toShortString()
                + " player=" + player.getName().getString()
                + " participants=" + participantCount), true);
        return 1;
    }

    private static int extract(CommandSourceStack source, String value, ServerPlayer player) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) {
            source.sendFailure(Component.literal("Raid not found: " + value));
            return 0;
        }
        RaidExtractionAttemptResult result = RaidExtractionService.attempt(player, manifest.get());
        if (!result.success()) {
            source.sendFailure(Component.literal(result.message()));
            return 0;
        }
        RaidManifest updated = result.updatedManifest();
        if (!updated.isParticipant(player.getUUID())) {
            updated = updated.withParticipant(new RaidParticipant(
                    player.getUUID(), player.getName().getString(), System.currentTimeMillis()));
        }
        updated = RaidExtractionCompletion.finalizeAfterExtraction(updated);
        if (!saveManifest(source, updated)) return 0;
        RaidManifestRegistry.put(updated);
        RaidManifest saved = updated;
        source.sendSuccess(() -> Component.literal("Extracted player " + player.getName().getString()
                + " from raid " + saved.raidId() + " " + result.message()
                + " state=" + saved.state()), true);
        return 1;
    }

    private static int extractionCheck(CommandSourceStack source, String value, ServerPlayer player) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) {
            source.sendFailure(Component.literal("Raid not found: " + value));
            return 0;
        }
        if (!player.serverLevel().dimension().location().toString().equals(manifest.get().dimensionId())) {
            source.sendFailure(Component.literal("Player is not in raid dimension."));
            return 0;
        }
        RaidExtractionCheckResult result = RaidExtractionService.check(player, manifest.get());
        source.sendSuccess(() -> Component.literal("Extraction check raid=" + manifest.get().raidId()
                + " player=" + player.getName().getString()
                + " inExtraction=" + result.inExtraction()
                + " extraction=" + result.extractionId()
                + " distance=" + result.distance() + " radius=" + result.radius()), false);
        return result.inExtraction() ? 1 : 0;
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
