package com.dreamingfish.gridinventory.common.size;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GridItemSizeLoader extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "item_sizes");

    public GridItemSizeLoader() {
        super(new Gson(), "df_grid_inventory/item_sizes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects, net.minecraft.server.packs.resources.ResourceManager resourceManager, ProfilerFiller profiler) {
        List<GridItemSizeRule> loaded = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonElement root = entry.getValue();
                if (root.isJsonObject() && root.getAsJsonObject().has("rules")) {
                    for (JsonElement ruleElement : GsonHelper.getAsJsonArray(root.getAsJsonObject(), "rules")) {
                        parseRule(ruleElement).ifPresent(loaded::add);
                    }
                } else {
                    parseRule(root).ifPresent(loaded::add);
                }
            } catch (Exception exception) {
                DFGridInventory.LOGGER.warn("Failed to load grid item size rules from {}", entry.getKey(), exception);
            }
        }
        GridItemSizeManager.replaceRules(loaded);
        DFGridInventory.LOGGER.info("Loaded {} grid item size rules", loaded.size());
    }

    private static java.util.Optional<GridItemSizeRule> parseRule(JsonElement element) {
        return GridItemSizeRule.CODEC.parse(JsonOps.INSTANCE, element).resultOrPartial(message -> DFGridInventory.LOGGER.warn("Invalid grid item size rule: {}", message));
    }
}
