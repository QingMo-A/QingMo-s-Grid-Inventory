package com.dreamingfish.gridinventory.common.raid.config;

import java.util.*;

public final class RaidMapConfigValidator {
    // TODO Phase 44C: validate anchor group limits and group coverage.
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
        validateVariants(map, issues);
        validateNavigation(map, issues);
        validateExtractions(map, issues);
        return new RaidMapValidationResult(List.copyOf(issues));
    }

    private static void validateVariants(RaidMapConfig map, List<RaidMapValidationIssue> issues) {
        Set<String> groupIds = new HashSet<>();
        for (RaidVariantGroupConfig group : map.variantGroups()) {
            if (group.id().isBlank() || !groupIds.add(group.id())) {
                error(issues, "duplicate/empty variant group id: " + group.id());
            }
            if (group.choose() != 1) {
                warn(issues, "First version only supports choose=1: " + group.id());
            }
            if (group.variants().isEmpty()) {
                error(issues, "variant group has no variants: " + group.id());
            }
            Set<String> variantIds = new HashSet<>();
            for (RaidVariantConfig variant : group.variants()) {
                if (variant.id().isBlank() || !variantIds.add(variant.id())) {
                    error(issues, "duplicate/empty variant id in group " + group.id() + ": " + variant.id());
                }
                if (variant.weight() <= 0) {
                    warn(issues, "variant weight corrected to 1: " + group.id() + "/" + variant.id());
                }
                for (RaidBlockPatchConfig patch : variant.patches()) {
                    if (patch.pos() == null || patch.pos().length != 3
                            || !map.bounds().contains(patch.localBlockPos())) {
                        error(issues, "variant patch outside bounds: " + group.id() + "/" + variant.id());
                    }
                    if (patch.block().isBlank()) {
                        error(issues, "empty variant patch block: " + group.id() + "/" + variant.id());
                    }
                    // Runtime performs the cross-version BlockState property parsing and reports failures.
                }
            }
        }
    }

    private static void validateNavigation(RaidMapConfig map, List<RaidMapValidationIssue> issues) {
        RaidNavigationConfig navigation = map.navigation();
        Set<String> nodeIds = unique(navigation.nodes().stream().map(RaidNavigationNodeConfig::id).toList(),
                "navigation node", issues);
        for (RaidNavigationNodeConfig node : navigation.nodes()) {
            if (node.pos() == null || node.pos().length != 3
                    || !map.bounds().contains(node.localBlockPos())) {
                error(issues, "navigation node outside bounds: " + node.id());
            }
        }
        unique(navigation.edges().stream().map(RaidNavigationEdgeConfig::id).toList(),
                "navigation edge", issues);
        for (RaidNavigationEdgeConfig edge : navigation.edges()) {
            if (!nodeIds.contains(edge.from())) {
                error(issues, "navigation edge has unknown from node: " + edge.id() + "/" + edge.from());
            }
            if (!nodeIds.contains(edge.to())) {
                error(issues, "navigation edge has unknown to node: " + edge.id() + "/" + edge.to());
            }
        }
        unique(navigation.checks().stream().map(RaidNavigationCheckConfig::id).toList(),
                "navigation check", issues);
        for (RaidNavigationCheckConfig check : navigation.checks()) {
            if (!nodeIds.contains(check.from())) {
                error(issues, "navigation check has unknown from node: " + check.id() + "/" + check.from());
            }
            if (check.to().isBlank() && check.toAnyTag().isBlank()) {
                error(issues, "navigation check has no target: " + check.id());
            }
            if (!check.to().isBlank() && !nodeIds.contains(check.to())) {
                error(issues, "navigation check has unknown target node: " + check.id() + "/" + check.to());
            }
            if (!check.toAnyTag().isBlank() && navigation.nodes().stream()
                    .noneMatch(node -> node.tags().contains(check.toAnyTag()))) {
                error(issues, "navigation check target tag has no nodes: "
                        + check.id() + "/" + check.toAnyTag());
            }
        }
    }

    private static void validateExtractions(RaidMapConfig map, List<RaidMapValidationIssue> issues) {
        Set<String> nodeIds = map.navigation().nodes().stream()
                .map(RaidNavigationNodeConfig::id).collect(java.util.stream.Collectors.toSet());
        unique(map.extractionAnchors().stream().map(RaidExtractionAnchorConfig::id).toList(),
                "extraction", issues);
        for (RaidExtractionAnchorConfig anchor : map.extractionAnchors()) {
            if (anchor.pos() == null || anchor.pos().length != 3
                    || !map.bounds().contains(anchor.localBlockPos())) {
                error(issues, "extraction outside bounds: " + anchor.id());
            }
            if (anchor.radius() <= 0) warn(issues, "extraction radius corrected to 3.0: " + anchor.id());
            if (anchor.weight() <= 0) warn(issues, "extraction weight corrected to 1: " + anchor.id());
            if (anchor.enabled() && !anchor.node().isBlank()) {
                if (map.navigation().nodes().isEmpty()) {
                    warn(issues, "extraction node cannot be validated without navigation: " + anchor.id());
                } else if (!nodeIds.contains(anchor.node())) {
                    error(issues, "extraction has unknown navigation node: "
                            + anchor.id() + "/" + anchor.node());
                }
            }
        }
        long enabled = map.extractionAnchors().stream().filter(RaidExtractionAnchorConfig::enabled).count();
        if (map.extractionActiveCount().min() > enabled) {
            warn(issues, "extraction_active_count min exceeds enabled extraction anchors");
        }
    }

    private static Set<String> unique(List<String> ids, String kind, List<RaidMapValidationIssue> issues) {
        Set<String> result = new HashSet<>();
        ids.forEach(id -> { if (id.isEmpty() || !result.add(id)) error(issues, "duplicate/empty " + kind + " id: " + id); });
        return result;
    }
    private static void error(List<RaidMapValidationIssue> issues, String message) { issues.add(new RaidMapValidationIssue(true, message)); }
    private static void warn(List<RaidMapValidationIssue> issues, String message) { issues.add(new RaidMapValidationIssue(false, message)); }
}
