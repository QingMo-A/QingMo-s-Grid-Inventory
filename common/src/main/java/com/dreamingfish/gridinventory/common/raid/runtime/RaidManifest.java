package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
// TODO Phase 50A: implement raid dimension pool.
// TODO Phase 50B: allocate one isolated raid dimension per active RaidInstance.
// TODO Phase 50C: delete or recycle raid dimension after raid ends.
// TODO Phase 50D: move template paste from map.dimension to manifest.dimensionId.
// TODO Phase 50E: persist RaidInstance metadata with dimensionId, pasteOrigin and lifecycle state.
public record RaidManifest(long raidId, long raidSeed, String mapId, String dimensionId, BlockPos pasteOrigin,
                           Map<String, ZoneRaidState> zones,
                           List<ContainerAnchorActivation> activeContainers) {}
