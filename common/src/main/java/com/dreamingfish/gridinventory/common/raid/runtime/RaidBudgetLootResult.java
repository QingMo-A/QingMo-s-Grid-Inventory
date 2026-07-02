package com.dreamingfish.gridinventory.common.raid.runtime;

import java.util.List;

public record RaidBudgetLootResult(
        List<RaidBudgetLootEntry> entries,
        int requestedBudget,
        int consumedBudget,
        int remainingBudget,
        int attempts,
        int warnings,
        int candidateCount) {
    public RaidBudgetLootResult {
        entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public boolean generatedAny() {
        return !entries.isEmpty();
    }
}
