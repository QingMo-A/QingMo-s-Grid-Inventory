package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidNavigationEdgeConfig(
        String id,
        String from,
        String to,
        boolean bidirectional,
        boolean enabledByDefault,
        List<String> requiredTags,
        List<String> disabledByTags) {
    public RaidNavigationEdgeConfig {
        id = id == null ? "" : id;
        from = from == null ? "" : from;
        to = to == null ? "" : to;
        requiredTags = requiredTags == null ? List.of() : List.copyOf(requiredTags);
        disabledByTags = disabledByTags == null ? List.of() : List.copyOf(disabledByTags);
    }
}
