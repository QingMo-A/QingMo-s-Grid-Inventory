package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidExtractionCheckResult(
        boolean inExtraction, String extractionId, double distance, double radius) {
}
