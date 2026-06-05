package com.dreamingfish.gridinventory.common.equipment;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.api.EquipmentStorageDefinition;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EquipmentStorageLoader extends SimpleJsonResourceReloadListener {
    public EquipmentStorageLoader() {
        super(new Gson(), "df_grid_inventory/equipment_storage");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<EquipmentStorageDefinition> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            JsonElement root = entry.getValue();
            Iterable<JsonElement> rules = root.isJsonObject() && root.getAsJsonObject().has("rules")
                    ? GsonHelper.getAsJsonArray(root.getAsJsonObject(), "rules")
                    : List.of(root);
            for (JsonElement rule : rules) {
                EquipmentStorageDefinition.CODEC.parse(JsonOps.INSTANCE, rule)
                        .resultOrPartial(message -> DFGridInventory.LOGGER.warn("Invalid equipment storage rule in {}: {}", entry.getKey(), message))
                        .ifPresent(loaded::add);
            }
        }
        EquipmentStorageManager.replaceRules(loaded);
        DFGridInventory.LOGGER.info("Loaded {} equipment storage rules", loaded.size());
    }
}
