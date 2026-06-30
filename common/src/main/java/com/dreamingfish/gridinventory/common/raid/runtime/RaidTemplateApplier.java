package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.raid.config.RaidMapConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public final class RaidTemplateApplier {
    private RaidTemplateApplier() {}

    public static RaidTemplateApplyResult applyTemplate(ServerLevel level, RaidMapConfig map) {
        if (map.template() == null) return RaidTemplateApplyResult.skipped();
        var template = level.getStructureManager().get(map.template());
        if (template.isEmpty()) {
            DFGridInventory.LOGGER.warn("Raid template missing mapId={} template={}", map.id(), map.template());
            return new RaidTemplateApplyResult(false, true);
        }
        var origin = map.pasteOriginPos();
        boolean applied = template.get().placeInWorld(level, origin, origin,
                new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(Rotation.NONE),
                level.random, 3);
        return new RaidTemplateApplyResult(applied, false);
    }
}
