package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidWorldResetResult(boolean refused, int clearedBlocks, boolean templateApplied,
                                   boolean templateMissing, int placeholdersRestored, int warnings) {
}
