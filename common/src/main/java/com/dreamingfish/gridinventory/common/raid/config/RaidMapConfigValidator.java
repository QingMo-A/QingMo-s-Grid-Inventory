package com.dreamingfish.gridinventory.common.raid.config;

import java.util.*;

public final class RaidMapConfigValidator {
    // TODO Phase 44C: validate anchor group limits and group coverage.
    // TODO Phase 49C: load participant/team spawn rules.
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
        validateExtractionSwitches(map, issues);
        validateSpawns(map, issues);
        validateLooseLoot(map, types, issues);
        validateRange("spawn_active_count", map.spawnActiveCount(), issues);
        validateRange("extraction_active_count", map.extractionActiveCount(), issues);
        return new RaidMapValidationResult(List.copyOf(issues));
    }
    private static void validateExtractionSwitches(RaidMapConfig map, List<RaidMapValidationIssue> issues) {
        Set<String> ids = unique(map.extractionSwitches().stream()
                .map(RaidExtractionSwitchConfig::id).toList(), "extraction switch", issues);
        for (RaidExtractionSwitchConfig value : map.extractionSwitches()) {
            if (value.pos() == null || value.pos().length != 3
                    || !map.bounds().contains(value.localBlockPos())) {
                error(issues, "extraction switch outside bounds: " + value.id());
            }
            if (value.radius() <= 0) warn(issues, "extraction switch radius corrected to 3.0: " + value.id());
        }
        for (RaidExtractionAnchorConfig extraction : map.extractionAnchors()) {
            if (extraction.trigger().isSwitch() && !ids.contains(extraction.trigger().switchId())) {
                error(issues, "switch extraction references unknown switch_id: " + extraction.trigger().switchId());
            }
        }
    }

    private static void validateLooseLoot(RaidMapConfig map, Set<String> types,
                                          List<RaidMapValidationIssue> issues) {
        Set<String> ids = new HashSet<>();
        Set<String> groups = new HashSet<>();
        for (RaidLooseLootAnchorConfig anchor : map.looseLootAnchors()) {
            if (anchor.id().isBlank() || !ids.add(anchor.id())) {
                error(issues, "duplicate/empty loose loot anchor id: " + anchor.id());
            }
            if (anchor.groupId().isBlank()) error(issues, "empty loose loot group: " + anchor.id());
            else groups.add(anchor.groupId());
            if (anchor.pos() == null || anchor.pos().length != 3
                    || !map.bounds().contains(anchor.localBlockPos())) {
                error(issues, "loose loot anchor outside bounds: " + anchor.id());
            }
            if (anchor.containerType().isBlank() || !types.contains(anchor.containerType())) {
                error(issues, "unknown container type for loose loot anchor " + anchor.id());
            }
            if (anchor.pointBudget() <= 0) warn(issues, "loose loot point_budget <= 0: " + anchor.id());
            if (anchor.qualityMultiplier() <= 0) warn(issues, "loose loot quality_multiplier corrected to 1: " + anchor.id());
            if (anchor.weight() <= 0) warn(issues, "loose loot weight corrected to 1: " + anchor.id());
            if (anchor.enabled() && !anchor.alwaysActive()
                    && !map.looseLootGroupCounts().containsKey(anchor.groupId())) {
                warn(issues, "loose loot group has no count rule and will only spawn always_active anchors: "
                        + anchor.groupId());
            }
        }
        map.looseLootGroupCounts().forEach((group, range) -> {
            if (range.min() < 0 || range.max() < range.min()) {
                error(issues, "invalid loose loot group count range: " + group);
            }
            if (!groups.contains(group)) warn(issues, "loose loot count rule references unknown group: " + group);
        });
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
            String availability = anchor.availability().type();
            if (!Set.of("always", "raid_remaining_lte").contains(availability)) {
                error(issues, "unknown extraction availability type: " + anchor.id() + "/" + availability);
            } else if ("raid_remaining_lte".equals(availability) && anchor.availability().seconds() <= 0) {
                error(issues, "raid_remaining_lte extraction requires seconds > 0: " + anchor.id());
            }
            String trigger = anchor.trigger().type();
            if (!Set.of("none", "switch", "item_turn_in").contains(trigger)) {
                error(issues, "unknown extraction trigger type: " + anchor.id() + "/" + trigger);
            } else if ("switch".equals(trigger) && anchor.trigger().switchId().isBlank()) {
                error(issues, "switch extraction requires switch_id: " + anchor.id());
            } else if ("item_turn_in".equals(trigger)) {
                if (anchor.trigger().requirements().isEmpty()) {
                    error(issues, "item_turn_in extraction requires items: " + anchor.id());
                }
                anchor.trigger().requirements().forEach(requirement -> {
                    if (requirement.item() == null || requirement.count() <= 0) {
                        error(issues, "invalid extraction item requirement: " + anchor.id());
                    }
                });
            }
            String timer = anchor.timer().type();
            if (!Set.of("player", "global").contains(timer)) error(issues, "unknown extraction timer type: " + anchor.id());
            if (!Set.of("reset", "ignore").contains(anchor.timer().leaveBehavior())) {
                error(issues, "unknown extraction leave_behavior: " + anchor.id());
            }
            if (anchor.timer().seconds() <= 0) error(issues, "extraction timer seconds must be > 0: " + anchor.id());
            if ("player".equals(timer) && !"reset".equals(anchor.timer().leaveBehavior())) warn(issues, "player extraction timer should reset on leave: " + anchor.id());
            if ("global".equals(timer) && !"ignore".equals(anchor.timer().leaveBehavior())) warn(issues, "global extraction timer should ignore leave: " + anchor.id());
            if ("none".equals(trigger) && "global".equals(timer)) warn(issues, "global timer without trigger will not start until future systems trigger it: " + anchor.id());
            if (Set.of("switch", "item_turn_in").contains(trigger) && !"global".equals(timer)) warn(issues, "triggered extraction should use global timer: " + anchor.id());
            if (anchor.useLimit() < -1) error(issues, "extraction use_limit must be -1 or >= 0: " + anchor.id());
            if (anchor.useLimit() == 0) warn(issues, "extraction use_limit=0 is exhausted: " + anchor.id());
            if (!Set.of("trigger", "success").contains(anchor.consumeUseOn())) error(issues, "unknown consume_use_on: " + anchor.id());
        }
        long enabled = map.extractionAnchors().stream().filter(RaidExtractionAnchorConfig::enabled).count();
        if (map.extractionActiveCount().min() > enabled) {
            warn(issues, "extraction_active_count min exceeds enabled extraction anchors");
        }
    }

    private static void validateSpawns(RaidMapConfig map, List<RaidMapValidationIssue> issues) {
        Set<String> nodeIds = map.navigation().nodes().stream()
                .map(RaidNavigationNodeConfig::id).collect(java.util.stream.Collectors.toSet());
        unique(map.spawnAnchors().stream().map(RaidSpawnAnchorConfig::id).toList(), "spawn", issues);
        for (RaidSpawnAnchorConfig anchor : map.spawnAnchors()) {
            if (anchor.pos() == null || anchor.pos().length != 3
                    || !map.bounds().contains(anchor.localBlockPos())) {
                error(issues, "spawn outside bounds: " + anchor.id());
            }
            if (anchor.weight() <= 0) warn(issues, "spawn weight corrected to 1: " + anchor.id());
            if (anchor.enabled() && !anchor.node().isBlank()) {
                if (map.navigation().nodes().isEmpty()) {
                    warn(issues, "spawn node cannot be validated without navigation: " + anchor.id());
                } else if (!nodeIds.contains(anchor.node())) {
                    error(issues, "spawn has unknown navigation node: " + anchor.id() + "/" + anchor.node());
                }
            }
        }
        long enabled = map.spawnAnchors().stream().filter(RaidSpawnAnchorConfig::enabled).count();
        if (!map.spawnAnchors().isEmpty() && map.spawnActiveCount().min() > enabled) {
            warn(issues, "spawn_active_count min exceeds enabled spawn anchors");
        }
    }

    private static void validateRange(
            String name, IntRangeConfig range, List<RaidMapValidationIssue> issues) {
        if (range.min() < 0) error(issues, name + ".min must be >= 0");
        if (range.max() < range.min()) error(issues, name + ".max must be >= min");
    }

    private static Set<String> unique(List<String> ids, String kind, List<RaidMapValidationIssue> issues) {
        Set<String> result = new HashSet<>();
        ids.forEach(id -> { if (id.isEmpty() || !result.add(id)) error(issues, "duplicate/empty " + kind + " id: " + id); });
        return result;
    }
    private static void error(List<RaidMapValidationIssue> issues, String message) { issues.add(new RaidMapValidationIssue(true, message)); }
    private static void warn(List<RaidMapValidationIssue> issues, String message) { issues.add(new RaidMapValidationIssue(false, message)); }
}
