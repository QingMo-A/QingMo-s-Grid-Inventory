package com.dreamingfish.gridinventory.common.loot;

public record ContainerLootManifestRef(long raidId, String anchorId, String zoneId, String containerType,
                                       int pointBudget) {
}
