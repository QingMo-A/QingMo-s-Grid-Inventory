package com.dreamingfish.gridinventory.common.loot;

public record SearchableContainerLootBinding(long raidId, long lootSeed, String mapId, String zoneId,
                                             String anchorId, String containerType, int pointBudget,
                                             double qualityMultiplier) {
    public SearchableContainerLootBinding {
        mapId = normalize(mapId);
        zoneId = normalize(zoneId);
        anchorId = normalize(anchorId);
        containerType = normalize(containerType);
        pointBudget = Math.max(0, pointBudget);
        qualityMultiplier = qualityMultiplier > 0.0D ? qualityMultiplier : 1.0D;
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
