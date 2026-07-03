package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.UUID;
public record RaidExtractionPlayerCountdown(long raidId, UUID playerId, String playerName,
        String extractionId, long startedGameTime, long lastSeenGameTime, int requiredTicks) {}
