package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public final class RaidVariantApplier {
    private RaidVariantApplier() {}

    public static RaidVariantApplyResult apply(ServerLevel level, RaidManifest manifest) {
        int applied = 0;
        int warnings = 0;
        for (RaidVariantSelection selection : manifest.variantSelections()) {
            for (RaidBlockPatch patch : selection.patches()) {
                try {
                    BlockState state = parseBlockState(patch.blockState());
                    level.setBlock(manifest.toWorldPos(patch.localPos()), state, 3);
                    applied++;
                } catch (Exception exception) {
                    warnings++;
                    DFGridInventory.LOGGER.warn(
                            "Skipping invalid raid variant patch raidId={} mapId={} groupId={} variantId={} localPos={} blockState={}",
                            manifest.raidId(), manifest.mapId(), selection.groupId(), selection.variantId(),
                            patch.localPos(), patch.blockState(), exception);
                }
            }
        }
        return new RaidVariantApplyResult(applied, warnings);
    }

    static BlockState parseBlockState(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Empty block state");
        String input = value.trim();
        int propertiesStart = input.indexOf('[');
        String blockId = propertiesStart < 0 ? input : input.substring(0, propertiesStart);
        ResourceLocation id = ResourceLocation.tryParse(blockId);
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            throw new IllegalArgumentException("Unknown block: " + blockId);
        }
        BlockState state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
        if (propertiesStart < 0) return state;
        if (!input.endsWith("]")) throw new IllegalArgumentException("Unclosed block properties");
        String properties = input.substring(propertiesStart + 1, input.length() - 1).trim();
        if (properties.isEmpty()) return state;
        for (String assignment : properties.split(",")) {
            String[] pair = assignment.trim().split("=", 2);
            if (pair.length != 2) throw new IllegalArgumentException("Invalid block property: " + assignment);
            Property<?> property = state.getBlock().getStateDefinition().getProperty(pair[0].trim());
            if (property == null) throw new IllegalArgumentException("Unknown block property: " + pair[0]);
            state = setProperty(state, property, pair[1].trim());
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setProperty(
            BlockState state, Property<T> property, String value) {
        Optional<T> parsed = property.getValue(value);
        if (parsed.isEmpty()) {
            throw new IllegalArgumentException("Invalid value for property " + property.getName() + ": " + value);
        }
        return state.setValue(property, parsed.get());
    }
}
