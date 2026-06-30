package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.List;
import java.util.Map;
public record RaidManifest(long raidId, long raidSeed, String mapId, Map<String, ZoneRaidState> zones,
                           List<ContainerAnchorActivation> activeContainers) {}
