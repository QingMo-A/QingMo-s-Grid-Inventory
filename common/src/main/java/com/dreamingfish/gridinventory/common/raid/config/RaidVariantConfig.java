package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidVariantConfig(
        String id, int weight, List<RaidBlockPatchConfig> patches, List<String> tags) {
    public RaidVariantConfig {
        id = id == null ? "" : id;
        patches = patches == null ? List.of() : List.copyOf(patches);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public int effectiveWeight() {
        return weight > 0 ? weight : 1;
    }
}
