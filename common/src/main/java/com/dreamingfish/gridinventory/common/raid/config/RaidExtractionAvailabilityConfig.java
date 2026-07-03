package com.dreamingfish.gridinventory.common.raid.config;
public record RaidExtractionAvailabilityConfig(String type, int seconds) {
    public RaidExtractionAvailabilityConfig {
        type = type == null || type.isBlank() ? "always" : type;
        seconds = Math.max(0, seconds);
    }
    public static RaidExtractionAvailabilityConfig always() { return new RaidExtractionAvailabilityConfig("always", 0); }
}
