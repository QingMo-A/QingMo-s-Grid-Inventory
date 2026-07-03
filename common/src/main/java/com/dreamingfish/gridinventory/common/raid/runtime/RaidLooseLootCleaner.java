package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public final class RaidLooseLootCleaner {
    private static final String MARKER = "df_grid_inventory_raid_loose_loot";
    private static final String RAID_PREFIX = "df_grid_inventory_raid_id_";
    private static final String ANCHOR_PREFIX = "df_grid_inventory_anchor_";

    private RaidLooseLootCleaner() {}

    public static void mark(ItemEntity entity, RaidManifest manifest, RaidLooseLootActivation anchor) {
        entity.addTag(MARKER);
        entity.addTag(RAID_PREFIX + manifest.raidId());
        entity.addTag(ANCHOR_PREFIX + sanitize(anchor.anchorId()));
    }

    public static RaidLooseLootCleanResult clean(
            ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        var min = manifest.toWorldPos(map.bounds().minPos());
        var max = manifest.toWorldPos(map.bounds().maxPos());
        AABB bounds = new AABB(min.getX(), min.getY(), min.getZ(),
                max.getX() + 1.0D, max.getY() + 1.0D, max.getZ() + 1.0D);
        String raidTag = RAID_PREFIX + manifest.raidId();
        var entities = level.getEntitiesOfClass(ItemEntity.class, bounds,
                entity -> entity.getTags().contains(MARKER) && entity.getTags().contains(raidTag));
        entities.forEach(ItemEntity::discard);
        return new RaidLooseLootCleanResult(entities.size());
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}
