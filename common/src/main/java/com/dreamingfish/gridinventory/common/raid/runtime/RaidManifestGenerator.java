package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.raid.config.*;
import java.util.*;

public final class RaidManifestGenerator {
    private RaidManifestGenerator() {}

    public static RaidManifest generate(RaidMapConfig map, long raidId, long seed) {
        Random random = new Random(seed ^ map.id().hashCode());
        List<RaidVariantSelection> variants = selectVariants(map, seed);
        List<RaidSpawnActivation> spawns = selectSpawns(map, variants, seed);
        List<RaidExtractionActivation> extractions = selectExtractions(map, variants, seed);
        List<RaidLooseLootActivation> looseLoot = selectLooseLoot(map, variants, seed);
        Map<String, ZoneRaidState> zones = new LinkedHashMap<>();
        List<ContainerAnchorActivation> activations = new ArrayList<>();
        Map<String, RaidContainerTypeConfig> types = new HashMap<>();
        map.containerTypes().forEach(t -> types.put(t.id(), t));
        for (RaidZoneConfig zone : map.zones()) {
            // TODO Phase 44C: enforce group coverage so active container anchors are spread across sub-areas.
            // TODO Phase 44C: enforce minimum distance between active loot anchors.
            List<RaidContainerAnchorConfig> candidates = new ArrayList<>(map.containerAnchors().stream()
                    .filter(a -> a.enabled() && a.zone().equals(zone.id())).toList());
            int requested = roll(random, zone.activeContainers());
            int count = Math.min(requested, candidates.size());
            int budget = roll(random, zone.lootBudget());
            List<RaidContainerAnchorConfig> selected = weightedSample(candidates, count, random);
            double qualityTotal = selected.stream().mapToDouble(a -> quality(a, types.get(a.containerType()))).sum();
            Map<String, Integer> budgets = new LinkedHashMap<>();
            int assigned = 0;
            for (int i = 0; i < selected.size(); i++) {
                RaidContainerAnchorConfig anchor = selected.get(i);
                int share = i == selected.size() - 1 ? budget - assigned
                        : (int) Math.floor(budget * quality(anchor, types.get(anchor.containerType())) / Math.max(qualityTotal, 1));
                assigned += share; budgets.put(anchor.id(), share);
                activations.add(new ContainerAnchorActivation(anchor.id(), zone.id(), anchor.group(), anchor.localBlockPos(),
                        anchor.containerType(), share, quality(anchor, types.get(anchor.containerType())), random.nextLong()));
            }
            zones.put(zone.id(), new ZoneRaidState(zone.id(), selected.size(), budget,
                    selected.stream().map(RaidContainerAnchorConfig::id).toList(), Map.copyOf(budgets)));
        }
        return new RaidManifest(raidId, seed, map.id(), map.dimension(), map.pasteOriginPos(),
                map.defaultSpawnLocalPos(), RaidLifecycleState.CREATED,
                Map.copyOf(zones), List.copyOf(activations), variants, extractions, spawns, looseLoot,
                List.of(), List.of());
        // TODO Phase 44D: allocate global rare items before normal container loot.
        // TODO Phase 44D: generate ContainerLootManifest from pointBudget instead of fallback loot table.
        // TODO Phase 47A: generate mob spawn manifest and event manifest.
    }

    private static List<RaidLooseLootActivation> selectLooseLoot(
            RaidMapConfig map, List<RaidVariantSelection> variants, long seed) {
        Set<String> tags = variants.stream().flatMap(v -> v.tags().stream())
                .collect(java.util.stream.Collectors.toSet());
        List<RaidLooseLootAnchorConfig> eligible = map.looseLootAnchors().stream()
                .filter(RaidLooseLootAnchorConfig::enabled)
                .filter(a -> tags.containsAll(a.requiresTags()))
                .filter(a -> a.forbiddenTags().stream().noneMatch(tags::contains)).toList();
        Random random = new Random(seed ^ map.id().hashCode() ^ 0x4C4F4F53454C4F4FL);
        List<RaidLooseLootAnchorConfig> selected = new ArrayList<>(
                eligible.stream().filter(RaidLooseLootAnchorConfig::alwaysActive).toList());
        Map<String, List<RaidLooseLootAnchorConfig>> groups = new TreeMap<>();
        eligible.stream().filter(a -> !a.alwaysActive())
                .forEach(a -> groups.computeIfAbsent(a.groupId(), ignored -> new ArrayList<>()).add(a));
        groups.forEach((group, candidates) -> {
            IntRangeConfig range = map.looseLootGroupCounts().getOrDefault(group, new IntRangeConfig(0, 0));
            selected.addAll(weightedLooseSample(candidates, Math.min(roll(random, range), candidates.size()), random));
        });
        Set<String> ids = new HashSet<>();
        return selected.stream().filter(a -> ids.add(a.id()))
                .map(a -> new RaidLooseLootActivation(a.id(), a.groupId(), a.localBlockPos(),
                        a.containerType(), a.pointBudget(), a.effectiveQualityMultiplier(), random.nextLong(), a.tags()))
                .toList();
    }

    private static List<RaidLooseLootAnchorConfig> weightedLooseSample(
            List<RaidLooseLootAnchorConfig> source, int count, Random random) {
        List<RaidLooseLootAnchorConfig> pool = new ArrayList<>(source), result = new ArrayList<>();
        while (result.size() < count && !pool.isEmpty()) {
            int roll = random.nextInt(pool.stream().mapToInt(RaidLooseLootAnchorConfig::effectiveWeight).sum());
            for (int i = 0; i < pool.size(); i++) {
                roll -= pool.get(i).effectiveWeight();
                if (roll < 0) { result.add(pool.remove(i)); break; }
            }
        }
        return result;
    }

    private static List<RaidVariantSelection> selectVariants(RaidMapConfig map, long seed) {
        Random random = new Random(seed ^ map.id().hashCode() ^ 0x56415249414E54L);
        List<RaidVariantSelection> selections = new ArrayList<>();
        for (RaidVariantGroupConfig group : map.variantGroups()) {
            if (group.variants().isEmpty()) {
                DFGridInventory.LOGGER.warn("Skipping empty raid variant group mapId={} groupId={}",
                        map.id(), group.id());
                continue;
            }
            int totalWeight = group.variants().stream().mapToInt(RaidVariantConfig::effectiveWeight).sum();
            int roll = random.nextInt(totalWeight);
            RaidVariantConfig selected = group.variants().get(group.variants().size() - 1);
            for (RaidVariantConfig variant : group.variants()) {
                roll -= variant.effectiveWeight();
                if (roll < 0) {
                    selected = variant;
                    break;
                }
            }
            List<RaidBlockPatch> patches = selected.patches().stream()
                    .map(patch -> new RaidBlockPatch(patch.localBlockPos(), patch.block()))
                    .toList();
            selections.add(new RaidVariantSelection(group.id(), selected.id(), selected.tags(), patches));
        }
        return List.copyOf(selections);
    }

    private static List<RaidExtractionActivation> selectExtractions(
            RaidMapConfig map, List<RaidVariantSelection> variants, long seed) {
        Set<String> selectedTags = variants.stream().flatMap(selection -> selection.tags().stream())
                .collect(java.util.stream.Collectors.toSet());
        List<RaidExtractionAnchorConfig> eligible = map.extractionAnchors().stream()
                .filter(RaidExtractionAnchorConfig::enabled)
                .filter(anchor -> selectedTags.containsAll(anchor.requiresTags()))
                .filter(anchor -> anchor.forbiddenTags().stream().noneMatch(selectedTags::contains))
                .toList();
        List<RaidExtractionAnchorConfig> always = eligible.stream()
                .filter(RaidExtractionAnchorConfig::alwaysActive).toList();
        List<RaidExtractionAnchorConfig> randomCandidates = eligible.stream()
                .filter(anchor -> !anchor.alwaysActive()).toList();
        Random random = new Random(seed ^ map.id().hashCode() ^ 0x455854524143544CL);
        int requested = roll(random, map.extractionActiveCount());
        if (requested > randomCandidates.size()) {
            DFGridInventory.LOGGER.warn(
                    "Raid extraction candidates below requested count mapId={} requested={} available={}",
                    map.id(), requested, randomCandidates.size());
        }
        List<RaidExtractionAnchorConfig> selected = new ArrayList<>(always);
        selected.addAll(weightedExtractionSample(randomCandidates,
                Math.min(requested, randomCandidates.size()), random));
        Set<String> ids = new HashSet<>();
        return selected.stream().filter(anchor -> ids.add(anchor.id()))
                .map(anchor -> new RaidExtractionActivation(anchor.id(), anchor.node(),
                        anchor.localBlockPos(), anchor.effectiveRadius(), anchor.displayName(), anchor.tags()))
                .toList();
    }

    private static List<RaidSpawnActivation> selectSpawns(
            RaidMapConfig map, List<RaidVariantSelection> variants, long seed) {
        if (map.spawnAnchors().isEmpty()) {
            return List.of(new RaidSpawnActivation("default_spawn", "spawn",
                    map.defaultSpawnLocalPos(), 0.0F, 0.0F, "Default Spawn",
                    List.of("spawn", "fallback")));
        }
        Set<String> selectedTags = variants.stream().flatMap(selection -> selection.tags().stream())
                .collect(java.util.stream.Collectors.toSet());
        List<RaidSpawnAnchorConfig> eligible = map.spawnAnchors().stream()
                .filter(RaidSpawnAnchorConfig::enabled)
                .filter(anchor -> selectedTags.containsAll(anchor.requiresTags()))
                .filter(anchor -> anchor.forbiddenTags().stream().noneMatch(selectedTags::contains))
                .toList();
        List<RaidSpawnAnchorConfig> always = eligible.stream()
                .filter(RaidSpawnAnchorConfig::alwaysActive).toList();
        List<RaidSpawnAnchorConfig> randomCandidates = eligible.stream()
                .filter(anchor -> !anchor.alwaysActive()).toList();
        Random random = new Random(seed ^ map.id().hashCode() ^ 0x535041574E4CL);
        int requested = roll(random, map.spawnActiveCount());
        if (requested > randomCandidates.size()) {
            DFGridInventory.LOGGER.warn(
                    "Raid spawn candidates below requested count mapId={} requested={} available={}",
                    map.id(), requested, randomCandidates.size());
        }
        List<RaidSpawnAnchorConfig> selected = new ArrayList<>(always);
        selected.addAll(weightedSpawnSample(randomCandidates,
                Math.min(requested, randomCandidates.size()), random));
        Set<String> ids = new HashSet<>();
        return selected.stream().filter(anchor -> ids.add(anchor.id()))
                .map(anchor -> new RaidSpawnActivation(anchor.id(), anchor.node(),
                        anchor.localBlockPos(), anchor.yaw(), anchor.pitch(), anchor.displayName(), anchor.tags()))
                .toList();
    }

    private static int roll(Random random, IntRangeConfig range) {
        return range.min() + (range.max() == range.min() ? 0 : random.nextInt(range.max() - range.min() + 1));
    }
    private static double quality(RaidContainerAnchorConfig a, RaidContainerTypeConfig type) {
        if (a.qualityMultiplier() > 0) return a.qualityMultiplier();
        return type != null && type.defaultQualityMultiplier() > 0 ? type.defaultQualityMultiplier() : 1.0D;
    }
    private static List<RaidContainerAnchorConfig> weightedSample(List<RaidContainerAnchorConfig> source, int count, Random random) {
        List<RaidContainerAnchorConfig> pool = new ArrayList<>(source), result = new ArrayList<>();
        while (result.size() < count && !pool.isEmpty()) {
            int total = pool.stream().mapToInt(RaidContainerAnchorConfig::effectiveWeight).sum();
            int roll = random.nextInt(total);
            for (int i = 0; i < pool.size(); i++) {
                roll -= pool.get(i).effectiveWeight();
                if (roll < 0) { result.add(pool.remove(i)); break; }
            }
        }
        return result;
    }

    private static List<RaidExtractionAnchorConfig> weightedExtractionSample(
            List<RaidExtractionAnchorConfig> source, int count, Random random) {
        List<RaidExtractionAnchorConfig> pool = new ArrayList<>(source), result = new ArrayList<>();
        while (result.size() < count && !pool.isEmpty()) {
            int total = pool.stream().mapToInt(RaidExtractionAnchorConfig::effectiveWeight).sum();
            int roll = random.nextInt(total);
            for (int i = 0; i < pool.size(); i++) {
                roll -= pool.get(i).effectiveWeight();
                if (roll < 0) {
                    result.add(pool.remove(i));
                    break;
                }
            }
        }
        return result;
    }

    private static List<RaidSpawnAnchorConfig> weightedSpawnSample(
            List<RaidSpawnAnchorConfig> source, int count, Random random) {
        List<RaidSpawnAnchorConfig> pool = new ArrayList<>(source), result = new ArrayList<>();
        while (result.size() < count && !pool.isEmpty()) {
            int total = pool.stream().mapToInt(RaidSpawnAnchorConfig::effectiveWeight).sum();
            int roll = random.nextInt(total);
            for (int i = 0; i < pool.size(); i++) {
                roll -= pool.get(i).effectiveWeight();
                if (roll < 0) {
                    result.add(pool.remove(i));
                    break;
                }
            }
        }
        return result;
    }
}
