package com.dreamingfish.gridinventory.common.raid.runtime;
public final class RaidExtractionCompletion {
    private RaidExtractionCompletion() {}
    public static boolean allParticipantsExtracted(RaidManifest manifest) {
        return !manifest.participants().isEmpty() && manifest.participants().stream()
                .allMatch(player -> manifest.isPlayerExtracted(player.playerId()));
    }
    public static RaidManifest finalizeAfterExtraction(RaidManifest manifest) {
        if (allParticipantsExtracted(manifest)) return manifest.withState(RaidLifecycleState.ENDED);
        return manifest.state() == RaidLifecycleState.CREATED || manifest.state() == RaidLifecycleState.APPLIED
                ? manifest.withState(RaidLifecycleState.RUNNING) : manifest;
    }
}
