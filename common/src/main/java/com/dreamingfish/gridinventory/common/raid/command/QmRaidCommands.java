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
    // TODO Phase 48D: persist RaidManifest to disk and reload after server restart.
    // TODO Phase 48B: add /qmraid reset to clear applied containers and restore placeholders.
    // TODO Phase 49A: spawn anchors and spawn group selection.
    // TODO Phase 49B: add raid lifecycle states: CREATED, APPLIED, RUNNING, ENDED, CLEANED.
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
                .then(Commands.literal("apply").then(Commands.argument("raid", StringArgumentType.word())
                        .executes(c -> apply(c.getSource(), StringArgumentType.getString(c, "raid")))))
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
                    + "; /qmraid create " + id + " seed 666; /qmraid apply latest; /qmraid join latest"), true);
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
        int placeholders = 0, air = 0, containers = 0, unexpected = 0;
        for (RaidContainerAnchorConfig anchor : map.get().containerAnchors()) {
            if (!anchor.enabled()) continue;
            var block = source.getLevel().getBlockState(anchor.blockPos()).getBlock();
            if (block == ModBlocks.RAID_CONTAINER_PLACEHOLDER.get()) placeholders++;
            else if (block == Blocks.AIR) air++;
            else if (block == ModBlocks.SEARCHABLE_GRID_CONTAINER.get()) containers++;
            else {
                unexpected++;
                source.sendSuccess(() -> Component.literal("WARN anchor " + anchor.id()
                        + " expected placeholder/air/searchable_container but found " + block
                        + " at " + anchor.blockPos().toShortString()), false);
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
        source.sendSuccess(() -> Component.literal("Raid " + raidId + " map=" + id + " seed=" + seed
                + " activeContainers=" + manifest.activeContainers().size()), true);
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
                + " seed=" + raid.raidSeed() + " activeContainers=" + raid.activeContainers().size()), false);
        raid.zones().values().forEach(zone -> source.sendSuccess(() -> Component.literal("zone " + zone.zoneId()
                + " budget=" + zone.lootBudget() + " active=" + zone.activeContainerCount()
                + " anchors=" + zone.anchorBudgets()), false));
        return 1;
    }
    private static int apply(CommandSourceStack source, String value) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(manifest.get().mapId());
        if (map.isEmpty()) { source.sendFailure(Component.literal("Map config not loaded: " + manifest.get().mapId())); return 0; }
        RaidWorldApplyResult result = RaidWorldApplier.apply(source.getLevel(), map.get(), manifest.get());
        source.sendSuccess(() -> Component.literal("Applied raid " + manifest.get().raidId()
                + ": activePlaced=" + result.activePlaced() + " activeRebound=" + result.activeRebound()
                + " inactiveCleared=" + result.inactiveCleared() + " warnings=" + result.warnings()), true);
        return result.activePlaced() + result.activeRebound();
    }
    private static int join(CommandSourceStack source, String value, ServerPlayer player) {
        Optional<RaidManifest> manifest = manifest(value);
        if (manifest.isEmpty()) { source.sendFailure(Component.literal("Raid not found: " + value)); return 0; }
        Optional<RaidMapConfig> map = RaidMapConfigRegistry.get(manifest.get().mapId());
        if (map.isEmpty()) { source.sendFailure(Component.literal("Map config not loaded: " + manifest.get().mapId())); return 0; }
        ResourceLocation id = ResourceLocation.tryParse(map.get().dimension());
        if (id == null) { source.sendFailure(Component.literal("Invalid dimension: " + map.get().dimension())); return 0; }
        var level = source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
        if (level == null) { source.sendFailure(Component.literal("Dimension not loaded: " + id)); return 0; }
        BlockPos spawn = map.get().defaultSpawnPos();
        player.teleportTo(level, spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D,
                player.getYRot(), player.getXRot());
        source.sendSuccess(() -> Component.literal("Joined raid " + manifest.get().raidId() + " map="
                + map.get().id() + " spawn=" + spawn.toShortString() + " player=" + player.getName().getString()), true);
        return 1;
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
