package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidWorldApplyResult(boolean templateApplied, boolean templateMissing, int activePlaced,
                                   int activeRebound, int inactiveCleared, int warnings) {
}
