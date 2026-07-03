package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidLooseLootApplyResult(
        int anchorsApplied, int stacksSpawned, int consumedBudget, int warnings) {
}
