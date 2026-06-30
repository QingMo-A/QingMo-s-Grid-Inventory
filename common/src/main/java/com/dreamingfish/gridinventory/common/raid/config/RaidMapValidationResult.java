package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidMapValidationResult(List<RaidMapValidationIssue> issues) {
    public long errors() { return issues.stream().filter(RaidMapValidationIssue::error).count(); }
    public long warnings() { return issues.size() - errors(); }
    public boolean valid() { return errors() == 0; }
}
