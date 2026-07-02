package com.dreamingfish.gridinventory.common.raid.config;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class RaidLootItemDefinitionLoader {
    public static final Path RELATIVE_PATH =
            Path.of("df_grid_inventory/raid/loot_item_definitions.json");
    private static final Set<String> TRADE_PRICE_FIELDS = Set.of(
            "buy_price", "sell_price", "market_price", "trade_price", "player_price", "shop_price");

    private RaidLootItemDefinitionLoader() {}

    public static LoadResult load(Path configRoot) throws IOException {
        Path path = configRoot.resolve(RELATIVE_PATH);
        if (!Files.isRegularFile(path)) return new LoadResult(List.of(), List.of());
        JsonElement root;
        try (var reader = Files.newBufferedReader(path)) {
            root = JsonParser.parseReader(reader);
        }
        if (!root.isJsonArray()) throw new JsonParseException("loot_item_definitions.json must be an array");
        List<RaidLootItemDefinitionConfig> definitions = new ArrayList<>();
        List<RaidLootItemDefinitionValidationIssue> issues = new ArrayList<>();
        int index = 0;
        for (JsonElement element : root.getAsJsonArray()) {
            try {
                JsonObject value = element.getAsJsonObject();
                String itemValue = string(value, "item");
                ResourceLocation item = ResourceLocation.tryParse(itemValue);
                if (item == null) {
                    issues.add(new RaidLootItemDefinitionValidationIssue(
                            true, "invalid item id at index " + index + ": " + itemValue));
                    index++;
                    continue;
                }
                for (String field : TRADE_PRICE_FIELDS) {
                    if (value.has(field)) {
                        issues.add(new RaidLootItemDefinitionValidationIssue(false,
                                "trade price fields are ignored by Raid loot definitions: "
                                        + item + "/" + field));
                    }
                }
                definitions.add(new RaidLootItemDefinitionConfig(item,
                        bool(value, "enabled", true), string(value, "category"),
                        string(value, "rarity"), integer(value, "system_value", 0),
                        integer(value, "spawn_weight", 1), integer(value, "combat_score", 0),
                        integer(value, "survival_score", 0), integer(value, "stack_min", 1),
                        integer(value, "stack_max", 1), strings(value, "tags")));
            } catch (Exception exception) {
                issues.add(new RaidLootItemDefinitionValidationIssue(
                        true, "invalid definition at index " + index + ": " + exception.getMessage()));
            }
            index++;
        }
        return new LoadResult(definitions, issues);
    }

    private static String string(JsonObject value, String key) {
        return value.has(key) && !value.get(key).isJsonNull() ? value.get(key).getAsString() : "";
    }

    private static int integer(JsonObject value, String key, int fallback) {
        return value.has(key) ? value.get(key).getAsInt() : fallback;
    }

    private static boolean bool(JsonObject value, String key, boolean fallback) {
        return value.has(key) ? value.get(key).getAsBoolean() : fallback;
    }

    private static List<String> strings(JsonObject value, String key) {
        if (!value.has(key) || !value.get(key).isJsonArray()) return List.of();
        List<String> result = new ArrayList<>();
        value.getAsJsonArray(key).forEach(element -> result.add(element.getAsString()));
        return List.copyOf(result);
    }

    public record LoadResult(
            List<RaidLootItemDefinitionConfig> definitions,
            List<RaidLootItemDefinitionValidationIssue> issues) {
        public LoadResult {
            definitions = List.copyOf(definitions);
            issues = List.copyOf(issues);
        }
    }
}
