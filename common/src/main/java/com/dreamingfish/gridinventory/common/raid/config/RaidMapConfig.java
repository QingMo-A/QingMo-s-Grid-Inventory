package com.dreamingfish.gridinventory.common.raid.config;

import java.util.List;

public record RaidMapConfig(String id, String displayName, MapBoundsConfig bounds, int expectedPlayers,
                            int raidTimeSeconds, List<RaidZoneConfig> zones,
                            List<RaidContainerTypeConfig> containerTypes,
                            List<RaidContainerAnchorConfig> containerAnchors) {
}
