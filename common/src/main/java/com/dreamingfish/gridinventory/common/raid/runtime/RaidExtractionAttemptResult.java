package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidExtractionAttemptResult(
        boolean success, String message, RaidManifest updatedManifest) {
}
