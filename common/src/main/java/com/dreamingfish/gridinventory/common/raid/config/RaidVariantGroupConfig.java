package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidVariantGroupConfig(String id, int choose, List<RaidVariantConfig> variants) {
    public RaidVariantGroupConfig {
        id = id == null ? "" : id;
        variants = variants == null ? List.of() : List.copyOf(variants);
    }
}
