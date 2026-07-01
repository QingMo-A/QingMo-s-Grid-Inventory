package com.dreamingfish.gridinventory.common.raid.runtime;

import java.util.UUID;

public record RaidExtractedPlayer(
        UUID playerId, String playerName, String extractionId, long extractedAtMillis) {
    public RaidExtractedPlayer {
        playerName = playerName == null ? "" : playerName;
        extractionId = extractionId == null ? "" : extractionId;
    }
}
