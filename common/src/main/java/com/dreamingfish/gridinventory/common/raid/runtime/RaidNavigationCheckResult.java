package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidNavigationCheckResult(
        String id, String from, String target, boolean required, boolean reachable) {
}
