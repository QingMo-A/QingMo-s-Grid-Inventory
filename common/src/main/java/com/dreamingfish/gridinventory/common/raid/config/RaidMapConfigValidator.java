package com.dreamingfish.gridinventory.common.raid.config;

import java.util.*;

public final class RaidMapConfigValidator {
    // TODO Phase 44C: validate anchor group limits and group coverage.
    // TODO Phase 45A: load variant_groups.json.
    // TODO Phase 45B: load navigation.json and validate reachability.
    // TODO Phase 45B: load extraction_anchors.json and spawn_anchors.json.
    // TODO Phase 46A: load loose_loot_anchors.json.
    // TODO Phase 44D: load item value config with spawn_cost/sell_value/combat_score/rarity_weight.
    private RaidMapConfigValidator() {
    }

    public static RaidMapValidationResult validate(String directoryId, RaidMapConfig map) {
        List<RaidMapValidationIssue> issues = new ArrayList<>();
        if (!directoryId.equals(map.id())) error(issues, "map.id must match directory name");
        if (net.minecraft.resources.ResourceLocation.tryParse(map.dimension()) == null) {
            error(issues, "invalid dimension: " + map.dimension());
        }
        if (!map.bounds().contains(map.defaultSpawnLocalPos())) {
            error(issues, "default_spawn outside bounds");
        }
        Set<String> zones = unique(map.zones().stream().map(RaidZoneConfig::id).toList(), "zone", issues);
        Set<String> types = unique(map.containerTypes().stream().map(RaidContainerTypeConfig::id).toList(), "container type", issues);
        unique(map.containerAnchors().stream().map(RaidContainerAnchorConfig::id).toList(), "anchor", issues);
        for (RaidContainerTypeConfig type : map.containerTypes()) {
            if (type.block() == null || type.fallbackLootTable() == null) error(issues, "invalid resource location for type " + type.id());
        }
        for (RaidContainerAnchorConfig anchor : map.containerAnchors()) {
            if (!zones.contains(anchor.zone())) error(issues, "unknown zone for anchor " + anchor.id());
            if (!types.contains(anchor.containerType())) error(issues, "unknown container type for anchor " + anchor.id());
            if (anchor.pos() == null || anchor.pos().length != 3 || !map.bounds().contains(anchor.localBlockPos())) error(issues, "anchor outside bounds " + anchor.id());
            if (anchor.weight() <= 0) warn(issues, "anchor weight corrected to 100: " + anchor.id());
            if (anchor.qualityMultiplier() <= 0) warn(issues, "anchor uses container type quality: " + anchor.id());
        }
        for (RaidZoneConfig zone : map.zones()) {
            long available = map.containerAnchors().stream().filter(a -> a.enabled() && a.zone().equals(zone.id())).count();
            if (available < zone.activeContainers().min()) error(issues, "insufficient enabled anchors in zone " + zone.id());
        }
        return new RaidMapValidationResult(List.copyOf(issues));
    }

    private static Set<String> unique(List<String> ids, String kind, List<RaidMapValidationIssue> issues) {
        Set<String> result = new HashSet<>();
        ids.forEach(id -> { if (id.isEmpty() || !result.add(id)) error(issues, "duplicate/empty " + kind + " id: " + id); });
        return result;
    }
    private static void error(List<RaidMapValidationIssue> issues, String message) { issues.add(new RaidMapValidationIssue(true, message)); }
    private static void warn(List<RaidMapValidationIssue> issues, String message) { issues.add(new RaidMapValidationIssue(false, message)); }
}
