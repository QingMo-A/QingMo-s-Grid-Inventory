package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.*;
import java.util.*;

public final class RaidManifestGenerator {
    private RaidManifestGenerator() {}

    public static RaidManifest generate(RaidMapConfig map, long raidId, long seed) {
        Random random = new Random(seed ^ map.id().hashCode());
        Map<String, ZoneRaidState> zones = new LinkedHashMap<>();
        List<ContainerAnchorActivation> activations = new ArrayList<>();
        Map<String, RaidContainerTypeConfig> types = new HashMap<>();
        map.containerTypes().forEach(t -> types.put(t.id(), t));
        for (RaidZoneConfig zone : map.zones()) {
            // TODO Phase 44B: group coverage and min-distance activation rules.
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
                activations.add(new ContainerAnchorActivation(anchor.id(), zone.id(), anchor.group(), anchor.blockPos(),
                        anchor.containerType(), share, quality(anchor, types.get(anchor.containerType())), random.nextLong()));
            }
            zones.put(zone.id(), new ZoneRaidState(zone.id(), selected.size(), budget,
                    selected.stream().map(RaidContainerAnchorConfig::id).toList(), Map.copyOf(budgets)));
        }
        return new RaidManifest(raidId, seed, map.id(), Map.copyOf(zones), List.copyOf(activations));
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
}
