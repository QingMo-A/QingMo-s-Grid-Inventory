package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.List;
import java.util.Map;
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
                           List<RaidExtractionActivation> activeExtractions) {
    public RaidManifest {
        defaultSpawnLocal = defaultSpawnLocal == null ? BlockPos.ZERO : defaultSpawnLocal;
        state = state == null ? RaidLifecycleState.CREATED : state;
        variantSelections = variantSelections == null ? List.of() : List.copyOf(variantSelections);
        activeExtractions = activeExtractions == null ? List.of() : List.copyOf(activeExtractions);
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
                newState, zones, activeContainers, variantSelections, activeExtractions);
    }
}
