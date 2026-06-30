package com.dreamingfish.gridinventory.common.raid.command;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.raid.config.*;
import com.dreamingfish.gridinventory.common.raid.runtime.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public final class QmRaidCommands {
    private static final AtomicLong RAID_IDS = new AtomicLong(System.currentTimeMillis());
    private QmRaidCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RaidMapConfigRegistry.reload(Path.of("config"));
        dispatcher.register(Commands.literal("qmraid").requires(s -> s.hasPermission(2))
                .then(Commands.literal("map")
                        .then(Commands.literal("reload").executes(c -> reload(c.getSource())))
                        .then(Commands.literal("list").executes(c -> list(c.getSource())))
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
        int count = RaidWorldApplier.apply(source.getLevel(), manifest.get());
        source.sendSuccess(() -> Component.literal("Applied containers=" + count), true);
        return count;
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
