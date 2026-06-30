package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public final class RaidTemplateApplier {
    private RaidTemplateApplier() {}

    public static RaidTemplateApplyResult applyTemplate(ServerLevel level, RaidMapConfig map, RaidManifest manifest) {
        if (map.template() == null) return RaidTemplateApplyResult.skipped();
        var template = level.getStructureManager().get(map.template());
        if (template.isEmpty()) {
            DFGridInventory.LOGGER.warn("Raid template missing mapId={} raidId={} template={} pasteOrigin={}",
                    map.id(), manifest.raidId(), map.template(), manifest.pasteOrigin());
            return new RaidTemplateApplyResult(false, true);
        }
        var origin = manifest.pasteOrigin();
        boolean applied = template.get().placeInWorld(level, origin, origin,
                new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(Rotation.NONE),
                level.random, 3);
        return new RaidTemplateApplyResult(applied, false);
    }
}
