package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.UUID;
public record RaidExtractionRuntimeState(String extractionId, String state, int remainingUses,
                                         long triggeredAtGameTime, UUID triggeredBy) {
    public RaidExtractionRuntimeState {
        extractionId = extractionId == null ? "" : extractionId;
        state = state == null || state.isBlank() ? "READY" : state;
        triggeredAtGameTime = triggeredAtGameTime < -1L ? -1L : triggeredAtGameTime;
    }
    public boolean unlimited() { return remainingUses < 0; }
    public boolean exhausted() { return remainingUses == 0 || "EXHAUSTED".equals(state); }
    public static RaidExtractionRuntimeState ready(String extractionId, int useLimit) {
        return new RaidExtractionRuntimeState(extractionId,
                useLimit == 0 ? "EXHAUSTED" : "READY", useLimit, -1L, null);
    }
}
