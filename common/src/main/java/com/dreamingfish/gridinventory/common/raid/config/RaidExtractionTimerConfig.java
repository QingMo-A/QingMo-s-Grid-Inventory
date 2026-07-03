package com.dreamingfish.gridinventory.common.raid.config;
public record RaidExtractionTimerConfig(String type, int seconds, String leaveBehavior) {
    public RaidExtractionTimerConfig {
        type = type == null || type.isBlank() ? "player" : type;
        seconds = seconds <= 0 ? 5 : seconds;
        leaveBehavior = leaveBehavior == null || leaveBehavior.isBlank() ? ("global".equals(type) ? "ignore" : "reset") : leaveBehavior;
    }
    public static RaidExtractionTimerConfig defaultPlayer() { return new RaidExtractionTimerConfig("player", 5, "reset"); }
    public static RaidExtractionTimerConfig defaultGlobal(int seconds) {
        return new RaidExtractionTimerConfig("global", seconds <= 0 ? 30 : seconds, "ignore");
    }
}
