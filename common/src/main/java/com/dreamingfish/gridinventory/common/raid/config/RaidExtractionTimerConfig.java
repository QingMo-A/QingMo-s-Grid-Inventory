package com.dreamingfish.gridinventory.common.raid.config;
public record RaidExtractionTimerConfig(String type, int seconds, String leaveBehavior) {
    public RaidExtractionTimerConfig {
        type = type == null || type.isBlank() ? "player" : type;
        leaveBehavior = leaveBehavior == null || leaveBehavior.isBlank() ? ("global".equals(type) ? "ignore" : "reset") : leaveBehavior;
    }
    public static RaidExtractionTimerConfig defaultPlayer() { return new RaidExtractionTimerConfig("player", 5, "reset"); }
}
