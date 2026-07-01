package com.dreamingfish.gridinventory.common.raid.runtime;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class RaidExtractionService {
    private RaidExtractionService() {}

    public static RaidExtractionCheckResult check(ServerPlayer player, RaidManifest manifest) {
        RaidExtractionActivation nearest = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (RaidExtractionActivation extraction : manifest.activeExtractions()) {
            Vec3 center = Vec3.atCenterOf(manifest.toWorldPos(extraction.localPos()));
            double distance = player.position().distanceTo(center);
            if (distance <= extraction.radius() && distance < nearestDistance) {
                nearest = extraction;
                nearestDistance = distance;
            }
        }
        return nearest == null
                ? new RaidExtractionCheckResult(false, "", Double.POSITIVE_INFINITY, 0.0D)
                : new RaidExtractionCheckResult(true, nearest.id(), nearestDistance, nearest.radius());
    }

    public static RaidExtractionAttemptResult attempt(ServerPlayer player, RaidManifest manifest) {
        String playerDimension = player.serverLevel().dimension().location().toString();
        if (!playerDimension.equals(manifest.dimensionId())) {
            return failure("Player is not in raid dimension.", manifest);
        }
        if (manifest.isPlayerExtracted(player.getUUID())) {
            return failure("Player already extracted.", manifest);
        }
        if (manifest.activeExtractions().isEmpty()) {
            return failure("Raid has no active extractions.", manifest);
        }
        RaidExtractionCheckResult check = check(player, manifest);
        if (!check.inExtraction()) {
            return failure("Player is not inside an active extraction.", manifest);
        }
        RaidManifest running = manifest.state() == RaidLifecycleState.CREATED
                || manifest.state() == RaidLifecycleState.APPLIED
                ? manifest.withState(RaidLifecycleState.RUNNING) : manifest;
        RaidExtractedPlayer extracted = new RaidExtractedPlayer(player.getUUID(),
                player.getName().getString(), check.extractionId(), System.currentTimeMillis());
        RaidManifest updated = running.withExtractedPlayer(extracted);
        String message = "extraction=" + check.extractionId()
                + " distance=" + check.distance() + " radius=" + check.radius();
        return new RaidExtractionAttemptResult(true, message, updated);
    }

    private static RaidExtractionAttemptResult failure(String message, RaidManifest manifest) {
        return new RaidExtractionAttemptResult(false, message, manifest);
    }
}
