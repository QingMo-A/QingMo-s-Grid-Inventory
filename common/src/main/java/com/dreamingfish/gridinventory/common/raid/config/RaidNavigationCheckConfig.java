package com.dreamingfish.gridinventory.common.raid.config;

public record RaidNavigationCheckConfig(
        String id, String from, String to, String toAnyTag, boolean required) {
    public RaidNavigationCheckConfig {
        id = id == null ? "" : id;
        from = from == null ? "" : from;
        to = to == null ? "" : to;
        toAnyTag = toAnyTag == null ? "" : toAnyTag;
    }
}
