package com.dreamingfish.gridinventory.common.folding;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
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

public class BackpackFoldingLoader extends SimpleJsonResourceReloadListener {
    public BackpackFoldingLoader() {
        super(new Gson(), "df_grid_inventory/backpack_folding");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<BackpackFoldingDefinition> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonElement root = entry.getValue();
                Iterable<JsonElement> rules = root.isJsonObject() && root.getAsJsonObject().has("rules")
                        ? GsonHelper.getAsJsonArray(root.getAsJsonObject(), "rules")
                        : List.of(root);
                for (JsonElement rule : rules) {
                    BackpackFoldingDefinition.CODEC.parse(JsonOps.INSTANCE, rule)
                            .resultOrPartial(message -> DFGridInventory.LOGGER.warn("Invalid backpack folding rule in {}: {}", entry.getKey(), message))
                            .ifPresent(loaded::add);
                }
            } catch (Exception exception) {
                DFGridInventory.LOGGER.warn("Failed to load backpack folding rules from {}", entry.getKey(), exception);
            }
        }
        BackpackFoldingManager.replaceRules(loaded);
        DFGridInventory.LOGGER.info("Loaded {} backpack folding rules", loaded.size());
    }
}
