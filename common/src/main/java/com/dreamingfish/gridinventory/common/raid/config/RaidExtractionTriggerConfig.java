package com.dreamingfish.gridinventory.common.raid.config;
import java.util.List;
public record RaidExtractionTriggerConfig(String type, String switchId, List<RaidItemRequirementConfig> requirements) {
    public RaidExtractionTriggerConfig {
        type = type == null || type.isBlank() ? "none" : type;
        switchId = switchId == null ? "" : switchId;
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
    }
    public static RaidExtractionTriggerConfig none() { return new RaidExtractionTriggerConfig("none", "", List.of()); }
    public boolean isNone() { return "none".equals(type); }
    public boolean isSwitch() { return "switch".equals(type); }
    public boolean isItemTurnIn() { return "item_turn_in".equals(type); }
    public boolean requiresGlobalTimer() { return isSwitch() || isItemTurnIn(); }
}
