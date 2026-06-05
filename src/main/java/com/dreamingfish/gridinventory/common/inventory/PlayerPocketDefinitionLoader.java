package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.api.EquipmentStorageContainerDefinition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class PlayerPocketDefinitionLoader extends SimpleJsonResourceReloadListener {
    public PlayerPocketDefinitionLoader() {
        super(new Gson(), "df_grid_inventory/player_pocket");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        EquipmentStorageContainerDefinition definition = objects.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .map(entry -> parse(entry.getKey(), entry.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
        PlayerPocketDefinitionManager.replaceDefinition(definition);
        if (definition == null) {
            DFGridInventoryMod.LOGGER.info("Loaded default player pocket definition from config");
        } else {
            DFGridInventoryMod.LOGGER.info("Loaded player pocket definition {}", definition.id());
        }
    }

    private static Optional<EquipmentStorageContainerDefinition> parse(ResourceLocation id, JsonElement json) {
        return EquipmentStorageContainerDefinition.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(message -> DFGridInventoryMod.LOGGER.warn("Invalid player pocket definition in {}: {}", id, message));
    }
}
