package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public final class RaidExtractionCountdownService {
    private static final Map<UUID, RaidExtractionPlayerCountdown> PLAYER_COUNTDOWNS = new HashMap<>();
    private static final Map<String, RaidExtractionGlobalCountdown> GLOBAL_COUNTDOWNS = new HashMap<>();
    private static long lastScanGameTime = -1L;
    private RaidExtractionCountdownService() {}

    public static void serverTick(MinecraftServer server) {
        long now = server.overworld().getGameTime();
        if (lastScanGameTime >= 0 && now >= lastScanGameTime && now - lastScanGameTime < 20L) return;
        lastScanGameTime = now;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) tickPlayer(server, player, now);
        for (RaidExtractionGlobalCountdown countdown : List.copyOf(GLOBAL_COUNTDOWNS.values())) {
            if (now - countdown.startedGameTime() >= countdown.requiredTicks()) finishGlobal(server, countdown);
        }
    }

    private static void tickPlayer(MinecraftServer server, ServerPlayer player, long now) {
        Optional<RaidManifest> found = findPlayerRaid(player);
        if (found.isEmpty()) { PLAYER_COUNTDOWNS.remove(player.getUUID()); return; }
        RaidManifest raid = found.get();
        RaidExtractionCheckResult check = RaidExtractionService.check(player, raid);
        RaidExtractionPlayerCountdown current = PLAYER_COUNTDOWNS.get(player.getUUID());
        if (!check.inExtraction()) {
            if (current != null) player.sendSystemMessage(Component.literal("Extraction cancelled: left zone."));
            PLAYER_COUNTDOWNS.remove(player.getUUID()); return;
        }
        RaidExtractionActivation extraction = raid.activeExtractions().stream()
                .filter(value -> value.id().equals(check.extractionId())).findFirst().orElse(null);
        RaidExtractionRuntimeState state = raid.extractionStates().get(extraction == null ? "" : extraction.id());
        if (extraction == null || !"player".equals(extraction.timer().type())
                || !extraction.trigger().isNone() || !"always".equals(extraction.availability().type())
                || state != null && state.exhausted()) {
            cancelPlayerCountdown(player, current, "extraction is no longer available");
            return;
        }
        if (current == null || current.raidId() != raid.raidId()
                || !current.extractionId().equals(extraction.id())) {
            current = new RaidExtractionPlayerCountdown(raid.raidId(), player.getUUID(),
                    player.getName().getString(), extraction.id(), now, now, extraction.timer().seconds() * 20);
            PLAYER_COUNTDOWNS.put(player.getUUID(), current);
            player.sendSystemMessage(Component.literal("Extraction started: " + extraction.id()
                    + ", stay in zone for " + extraction.timer().seconds() + "s."));
            return;
        }
        if (now - current.startedGameTime() >= current.requiredTicks()) {
            RaidExtractionAttemptResult attempt = RaidExtractionService.attempt(player, raid);
            if (attempt.success()) {
                RaidManifest updated = attempt.updatedManifest();
                RaidExtractionRuntimeState old = updated.extractionStates().get(extraction.id());
                int uses = old == null ? extraction.useLimit() : old.remainingUses();
                if (uses > 0) uses--;
                updated = updated.withExtractionState(new RaidExtractionRuntimeState(extraction.id(),
                        uses == 0 ? "EXHAUSTED" : "READY", uses, -1L, null));
                updated = RaidExtractionCompletion.finalizeAfterExtraction(updated);
                persist(server, updated);
                player.sendSystemMessage(Component.literal("Extracted via " + extraction.id() + "."));
            } else {
                player.sendSystemMessage(Component.literal("Extraction cancelled: " + attempt.message()));
            }
            PLAYER_COUNTDOWNS.remove(player.getUUID());
        } else {
            PLAYER_COUNTDOWNS.put(player.getUUID(), new RaidExtractionPlayerCountdown(
                    current.raidId(), current.playerId(), current.playerName(), current.extractionId(),
                    current.startedGameTime(), now, current.requiredTicks()));
            int remaining = (int) Math.ceil((current.requiredTicks() - (now - current.startedGameTime())) / 20.0D);
            player.sendSystemMessage(Component.literal("Extracting via " + extraction.id() + ": " + remaining + "s"));
        }
    }

    public static String trigger(MinecraftServer server, RaidManifest raid, String extractionId, ServerPlayer player) {
        RaidExtractionActivation extraction = raid.activeExtractions().stream()
                .filter(value -> value.id().equals(extractionId)).findFirst().orElse(null);
        if (extraction == null) return "Extraction not found.";
        if (!"global".equals(extraction.timer().type())) return "Extraction is not a global timer extraction.";
        if (!extraction.trigger().requiresGlobalTimer()) return "Extraction has no global trigger.";
        if (!"always".equals(extraction.availability().type())) return "Timed availability needs raid clock in future phase.";
        RaidExtractionRuntimeState old = raid.extractionStates().getOrDefault(extractionId,
                RaidExtractionRuntimeState.ready(extractionId, extraction.useLimit()));
        if (old.exhausted()) return "Extraction is exhausted.";
        String key = globalKey(raid.raidId(), extractionId);
        if (GLOBAL_COUNTDOWNS.containsKey(key)) return "Extraction countdown already running.";
        int remaining = old.remainingUses();
        if ("trigger".equals(extraction.consumeUseOn()) && remaining > 0) remaining--;
        long now = server.overworld().getGameTime();
        RaidExtractionRuntimeState state = new RaidExtractionRuntimeState(extractionId, "TRIGGERED",
                remaining, now, player == null ? null : player.getUUID());
        persist(server, raid.withExtractionState(state));
        GLOBAL_COUNTDOWNS.put(key, new RaidExtractionGlobalCountdown(raid.raidId(), extractionId, now,
                extraction.timer().seconds() * 20, player == null ? null : player.getUUID(),
                player == null ? "" : player.getName().getString()));
        return "Triggered extraction " + extractionId + " countdown=" + extraction.timer().seconds() + "s";
    }

    private static void finishGlobal(MinecraftServer server, RaidExtractionGlobalCountdown countdown) {
        String key = globalKey(countdown.raidId(), countdown.extractionId());
        try {
            RaidManifest raid = RaidManifestRegistry.get(countdown.raidId()).orElse(null);
            if (!isRunnableRaid(raid)) return;
            RaidExtractionActivation extraction = raid.activeExtractions().stream()
                    .filter(value -> value.id().equals(countdown.extractionId())).findFirst().orElse(null);
            if (extraction == null) {
                DFGridInventory.LOGGER.warn("Cancelling dangling global extraction countdown raidId={} extractionId={}",
                        countdown.raidId(), countdown.extractionId());
                return;
            }
            RaidManifest updated = raid;
            int successes = 0;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!isEligibleGlobalPlayer(player, updated)) continue;
                RaidExtractionCheckResult check = RaidExtractionService.check(player, updated);
                if (!check.inExtraction() || !check.extractionId().equals(extraction.id())) continue;
                RaidExtractionAttemptResult attempt = RaidExtractionService.attempt(player, updated);
                if (attempt.success()) {
                    updated = attempt.updatedManifest();
                    successes++;
                    player.sendSystemMessage(Component.literal("Extracted via " + extraction.id() + "."));
                }
            }
            RaidExtractionRuntimeState old = updated.extractionStates().get(extraction.id());
            int remaining = old == null ? extraction.useLimit() : old.remainingUses();
            if ("success".equals(extraction.consumeUseOn()) && successes > 0 && remaining > 0) remaining--;
            updated = updated.withExtractionState(new RaidExtractionRuntimeState(extraction.id(),
                    remaining == 0 ? "EXHAUSTED" : "READY", remaining, -1L, null));
            persist(server, RaidExtractionCompletion.finalizeAfterExtraction(updated));
        } catch (Exception exception) {
            DFGridInventory.LOGGER.warn("Failed to finish global extraction countdown raidId={} extractionId={}",
                    countdown.raidId(), countdown.extractionId(), exception);
        } finally {
            GLOBAL_COUNTDOWNS.remove(key);
        }
    }

    public static void clearRaid(long raidId) {
        PLAYER_COUNTDOWNS.values().removeIf(value -> value.raidId() == raidId);
        GLOBAL_COUNTDOWNS.values().removeIf(value -> value.raidId() == raidId);
    }
    public static RaidManifest normalizeClearedRaid(RaidManifest raid) {
        RaidManifest updated = raid;
        for (RaidExtractionRuntimeState state : raid.extractionStates().values()) {
            if (!"TRIGGERED".equals(state.state()) && state.triggeredAtGameTime() < 0
                    && state.triggeredBy() == null) continue;
            updated = updated.withExtractionState(new RaidExtractionRuntimeState(state.extractionId(),
                    state.remainingUses() == 0 ? "EXHAUSTED" : "READY",
                    state.remainingUses(), -1L, null));
        }
        return updated;
    }
    public static void clearPlayer(UUID playerId) { PLAYER_COUNTDOWNS.remove(playerId); }
    public static boolean cancelGlobal(MinecraftServer server, long raidId, String extractionId) {
        if (GLOBAL_COUNTDOWNS.remove(globalKey(raidId, extractionId)) == null) return false;
        RaidManifestRegistry.get(raidId).ifPresent(raid -> {
            RaidExtractionRuntimeState old = raid.extractionStates().get(extractionId);
            if (old != null) persist(server, raid.withExtractionState(new RaidExtractionRuntimeState(extractionId,
                    old.remainingUses() == 0 ? "EXHAUSTED" : "READY", old.remainingUses(), -1L, null)));
        });
        return true;
    }
    public static List<RaidExtractionPlayerCountdown> playerCountdowns() { return List.copyOf(PLAYER_COUNTDOWNS.values()); }
    public static List<RaidExtractionGlobalCountdown> globalCountdowns() { return List.copyOf(GLOBAL_COUNTDOWNS.values()); }
    private static String globalKey(long raidId, String extractionId) { return raidId + ":" + extractionId; }
    private static Optional<RaidManifest> findPlayerRaid(ServerPlayer player) {
        String dimension = player.serverLevel().dimension().location().toString();
        return RaidManifestRegistry.all().stream().filter(raid -> raid.state() == RaidLifecycleState.APPLIED || raid.state() == RaidLifecycleState.RUNNING)
                .filter(raid -> raid.dimensionId().equals(dimension) && raid.isParticipant(player.getUUID()))
                .max(Comparator.comparingLong(RaidManifest::raidId));
    }
    private static boolean isRunnableRaid(RaidManifest raid) {
        return raid != null && (raid.state() == RaidLifecycleState.APPLIED
                || raid.state() == RaidLifecycleState.RUNNING);
    }
    private static boolean isEligibleGlobalPlayer(ServerPlayer player, RaidManifest raid) {
        return player.serverLevel().dimension().location().toString().equals(raid.dimensionId())
                && raid.isParticipant(player.getUUID()) && !raid.isPlayerExtracted(player.getUUID());
    }
    private static void cancelPlayerCountdown(
            ServerPlayer player, RaidExtractionPlayerCountdown countdown, String reason) {
        if (countdown != null) player.sendSystemMessage(Component.literal("Extraction cancelled: " + reason + "."));
        PLAYER_COUNTDOWNS.remove(player.getUUID());
    }
    private static void persist(MinecraftServer server, RaidManifest manifest) {
        RaidManifestRegistry.put(manifest);
        try { RaidManifestStorage.save(server, manifest); }
        catch (Exception exception) { DFGridInventory.LOGGER.warn("Failed to save extraction countdown raid {}", manifest.raidId(), exception); }
    }
}
