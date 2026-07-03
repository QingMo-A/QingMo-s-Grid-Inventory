package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.UUID;
public record RaidExtractionGlobalCountdown(long raidId, String extractionId, long startedGameTime,
        int requiredTicks, UUID triggeredBy, String triggeredByName) {}
