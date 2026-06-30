package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidZoneConfig(String id, int tier, IntRangeConfig activeContainers, IntRangeConfig lootBudget,
                             List<String> allowedCategories) {
}
