package com.dreamingfish.gridinventory.common.raid.runtime;

import java.util.UUID;

public record RaidParticipant(UUID playerId, String playerName, long joinedAtMillis) {
    public RaidParticipant {
        playerName = playerName == null ? "" : playerName;
    }
}
