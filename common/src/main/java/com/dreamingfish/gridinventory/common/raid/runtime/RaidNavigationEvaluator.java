package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfig;
import com.dreamingfish.gridinventory.common.raid.config.RaidNavigationCheckConfig;
import com.dreamingfish.gridinventory.common.raid.config.RaidNavigationEdgeConfig;

import java.util.*;
import java.util.stream.Collectors;

public final class RaidNavigationEvaluator {
    private RaidNavigationEvaluator() {}

    public static RaidNavigationReport evaluate(RaidMapConfig map, RaidManifest manifest) {
        Set<String> selectedTags = manifest.variantSelections().stream()
                .flatMap(selection -> selection.tags().stream())
                .collect(Collectors.toSet());
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        map.navigation().nodes().forEach(node -> graph.put(node.id(), new LinkedHashSet<>()));
        int enabledEdges = 0;
        int disabledEdges = 0;
        for (RaidNavigationEdgeConfig edge : map.navigation().edges()) {
            boolean enabled = edge.enabledByDefault()
                    && selectedTags.containsAll(edge.requiredTags())
                    && edge.disabledByTags().stream().noneMatch(selectedTags::contains);
            if (!enabled) {
                disabledEdges++;
                continue;
            }
            enabledEdges++;
            graph.computeIfAbsent(edge.from(), ignored -> new LinkedHashSet<>()).add(edge.to());
            if (edge.bidirectional()) {
                graph.computeIfAbsent(edge.to(), ignored -> new LinkedHashSet<>()).add(edge.from());
            }
        }
        List<RaidNavigationCheckResult> checks = map.navigation().checks().stream()
                .map(check -> evaluateCheck(map, graph, check))
                .toList();
        return new RaidNavigationReport(map.navigation().nodes().size(),
                enabledEdges, disabledEdges, checks);
    }

    private static RaidNavigationCheckResult evaluateCheck(
            RaidMapConfig map, Map<String, Set<String>> graph, RaidNavigationCheckConfig check) {
        Set<String> targets = new LinkedHashSet<>();
        if (!check.to().isBlank()) targets.add(check.to());
        if (!check.toAnyTag().isBlank()) {
            map.navigation().nodes().stream()
                    .filter(node -> node.tags().contains(check.toAnyTag()))
                    .map(node -> node.id())
                    .forEach(targets::add);
        }
        String target = !check.to().isBlank() ? "node:" + check.to() : "tag:" + check.toAnyTag();
        return new RaidNavigationCheckResult(check.id(), check.from(), target, check.required(),
                reachable(graph, check.from(), targets));
    }

    private static boolean reachable(Map<String, Set<String>> graph, String from, Set<String> targets) {
        if (!graph.containsKey(from) || targets.isEmpty()) return false;
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        visited.add(from);
        queue.add(from);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            if (targets.contains(current)) return true;
            for (String next : graph.getOrDefault(current, Set.of())) {
                if (visited.add(next)) queue.addLast(next);
            }
        }
        return false;
    }
}
