package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidNavigationConfig(
        List<RaidNavigationNodeConfig> nodes,
        List<RaidNavigationEdgeConfig> edges,
        List<RaidNavigationCheckConfig> checks) {
    public RaidNavigationConfig {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
        checks = checks == null ? List.of() : List.copyOf(checks);
    }

    public static RaidNavigationConfig empty() {
        return new RaidNavigationConfig(List.of(), List.of(), List.of());
    }
}
