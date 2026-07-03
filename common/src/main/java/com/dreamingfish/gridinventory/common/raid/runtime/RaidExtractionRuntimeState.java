package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.UUID;
public record RaidExtractionRuntimeState(String extractionId, String state, int remainingUses,
                                         long triggeredAtGameTime, UUID triggeredBy) {
    public RaidExtractionRuntimeState {
        extractionId = extractionId == null ? "" : extractionId;
        state = state == null || state.isBlank() ? "READY" : state;
    }
}
