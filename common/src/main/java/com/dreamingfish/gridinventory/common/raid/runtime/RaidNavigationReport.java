package com.dreamingfish.gridinventory.common.raid.runtime;

import java.util.List;

public record RaidNavigationReport(
        int nodes, int enabledEdges, int disabledEdges, List<RaidNavigationCheckResult> checks) {
    public RaidNavigationReport {
        checks = checks == null ? List.of() : List.copyOf(checks);
    }

    public long failedRequiredChecks() {
        return checks.stream().filter(check -> check.required() && !check.reachable()).count();
    }

    public boolean hasFailedRequiredChecks() {
        return failedRequiredChecks() > 0;
    }
}
