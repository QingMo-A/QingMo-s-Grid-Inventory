package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidLootItemDefinitionValidationResult(
        List<RaidLootItemDefinitionValidationIssue> issues) {
    public RaidLootItemDefinitionValidationResult {
        issues = issues == null ? List.of() : List.copyOf(issues);
    }

    public long errors() {
        return issues.stream().filter(RaidLootItemDefinitionValidationIssue::error).count();
    }

    public long warnings() {
        return issues.size() - errors();
    }

    public boolean valid() {
        return errors() == 0;
    }
}
