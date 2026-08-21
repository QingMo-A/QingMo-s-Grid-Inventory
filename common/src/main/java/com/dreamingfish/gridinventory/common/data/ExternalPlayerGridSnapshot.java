package com.dreamingfish.gridinventory.common.data;

import java.util.Optional;
import java.util.UUID;

public final class ExternalPlayerGridSnapshot {
    private static UUID playerId;
    private static GridInventoryData latest;
    private static long revision;

    private ExternalPlayerGridSnapshot() {
    }

    public static synchronized void update(UUID ownerId, GridInventoryData data) {
        playerId = ownerId;
        latest = data == null ? null : data.copy();
        revision++;
    }

    public static synchronized Optional<Snapshot> copyIfNew(UUID ownerId, long knownRevision) {
        return latest == null || !ownerId.equals(playerId) || revision == knownRevision
                ? Optional.empty()
                : Optional.of(new Snapshot(revision, latest.copy()));
    }

    public record Snapshot(long revision, GridInventoryData data) {
    }
}
