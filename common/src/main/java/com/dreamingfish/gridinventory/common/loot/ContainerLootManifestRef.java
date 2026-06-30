package com.dreamingfish.gridinventory.common.loot;

public record ContainerLootManifestRef(long raidId, long lootSeed, String mapId, String anchorId, String zoneId,
                                       String containerType, int pointBudget, double qualityMultiplier) {
    public static ContainerLootManifestRef fromBinding(SearchableContainerLootBinding binding) {
        return new ContainerLootManifestRef(binding.raidId(), binding.lootSeed(), binding.mapId(),
                binding.anchorId(), binding.zoneId(), binding.containerType(), binding.pointBudget(),
                binding.qualityMultiplier());
    }
}
