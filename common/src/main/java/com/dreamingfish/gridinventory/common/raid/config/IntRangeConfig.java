package com.dreamingfish.gridinventory.common.raid.config;

public record IntRangeConfig(int min, int max) {
    public IntRangeConfig {
        min = Math.max(0, min);
        max = Math.max(min, max);
    }
}
