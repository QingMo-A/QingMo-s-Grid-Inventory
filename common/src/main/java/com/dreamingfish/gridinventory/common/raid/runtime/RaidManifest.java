package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
// TODO Phase 50A: implement raid dimension pool.
// TODO Phase 50B: allocate one isolated raid dimension per active RaidInstance.
// TODO Phase 50C: delete or recycle raid dimension after raid ends.
// TODO Phase 50E: persist RaidInstance metadata with dimensionId, pasteOrigin and lifecycle state.
public record RaidManifest(long raidId, long raidSeed, String mapId, String dimensionId, BlockPos pasteOrigin,
                           BlockPos defaultSpawnLocal, RaidLifecycleState state,
                           Map<String, ZoneRaidState> zones,
                           List<ContainerAnchorActivation> activeContainers,
                           List<RaidVariantSelection> variantSelections,
                           List<RaidExtractionActivation> activeExtractions,
                           List<RaidSpawnActivation> activeSpawns,
                           List<RaidExtractedPlayer> extractedPlayers,
                           List<RaidParticipant> participants) {
    public RaidManifest {
        defaultSpawnLocal = defaultSpawnLocal == null ? BlockPos.ZERO : defaultSpawnLocal;
        state = state == null ? RaidLifecycleState.CREATED : state;
        variantSelections = variantSelections == null ? List.of() : List.copyOf(variantSelections);
        activeExtractions = activeExtractions == null ? List.of() : List.copyOf(activeExtractions);
        activeSpawns = activeSpawns == null ? List.of() : List.copyOf(activeSpawns);
        extractedPlayers = extractedPlayers == null ? List.of() : List.copyOf(extractedPlayers);
        participants = participants == null ? List.of() : List.copyOf(participants);
    }
    public BlockPos toWorldPos(BlockPos localPos) { return pasteOrigin.offset(localPos); }
    public BlockPos toWorldPos(int[] localPos) {
        return localPos != null && localPos.length == 3
                ? toWorldPos(new BlockPos(localPos[0], localPos[1], localPos[2])) : pasteOrigin;
    }
    public BlockPos toLocalPos(BlockPos worldPos) { return worldPos.subtract(pasteOrigin); }
    public BlockPos defaultSpawnWorld() { return toWorldPos(defaultSpawnLocal); }
    public RaidManifest withState(RaidLifecycleState newState) {
        return new RaidManifest(raidId, raidSeed, mapId, dimensionId, pasteOrigin, defaultSpawnLocal,
                newState, zones, activeContainers, variantSelections, activeExtractions, activeSpawns,
                extractedPlayers, participants);
    }
    public boolean isPlayerExtracted(UUID playerId) {
        return playerId != null && extractedPlayers.stream()
                .anyMatch(player -> playerId.equals(player.playerId()));
    }
    public RaidManifest withExtractedPlayer(RaidExtractedPlayer player) {
        if (player == null || player.playerId() == null || isPlayerExtracted(player.playerId())) return this;
        List<RaidExtractedPlayer> updated = new java.util.ArrayList<>(extractedPlayers);
        updated.add(player);
        return new RaidManifest(raidId, raidSeed, mapId, dimensionId, pasteOrigin, defaultSpawnLocal,
                state, zones, activeContainers, variantSelections, activeExtractions, activeSpawns,
                List.copyOf(updated), participants);
    }
    public boolean isParticipant(UUID playerId) {
        return playerId != null && participants.stream()
                .anyMatch(participant -> playerId.equals(participant.playerId()));
    }
    public RaidManifest withParticipant(RaidParticipant participant) {
        if (participant == null || participant.playerId() == null
                || isParticipant(participant.playerId())) return this;
        List<RaidParticipant> updated = new java.util.ArrayList<>(participants);
        updated.add(participant);
        return new RaidManifest(raidId, raidSeed, mapId, dimensionId, pasteOrigin, defaultSpawnLocal,
                state, zones, activeContainers, variantSelections, activeExtractions, activeSpawns,
                extractedPlayers, List.copyOf(updated));
    }
    public RaidManifest withClearedRunState(RaidLifecycleState newState) {
        return new RaidManifest(raidId, raidSeed, mapId, dimensionId, pasteOrigin, defaultSpawnLocal,
                newState, zones, activeContainers, variantSelections, activeExtractions, activeSpawns,
                List.of(), List.of());
    }
}
