package com.dreamingfish.gridinventory.common.raid.config;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class RaidMapConfigLoader {
    private static final Gson GSON = new Gson();

    private RaidMapConfigLoader() {
    }

    public static RaidMapConfig load(Path directory) throws IOException {
        JsonObject map = read(directory.resolve("map.json")).getAsJsonObject();
        String id = string(map, "id");
        JsonObject bounds = map.getAsJsonObject("bounds");
        int[] origin = optionalIntArray(map, "origin");
        int[] defaultSpawn = optionalIntArray(map, "default_spawn");
        JsonObject extractionCount = map.has("extraction_active_count")
                && map.get("extraction_active_count").isJsonObject()
                ? map.getAsJsonObject("extraction_active_count") : null;
        JsonObject spawnCount = map.has("spawn_active_count")
                && map.get("spawn_active_count").isJsonObject()
                ? map.getAsJsonObject("spawn_active_count") : null;
        return new RaidMapConfig(id, string(map, "display_name"), string(map, "dimension"),
                optionalLocation(map, "template"),
                origin, defaultSpawn == null ? new int[]{0, 0, 0} : defaultSpawn,
                new MapBoundsConfig(intArray(bounds, "min"), intArray(bounds, "max")),
                integer(map, "expected_players", 0), integer(map, "raid_time_seconds", 0),
                zones(read(directory.resolve("zones.json")).getAsJsonArray()),
                types(read(directory.resolve("container_types.json")).getAsJsonArray()),
                anchors(read(directory.resolve("container_anchors.json")).getAsJsonArray()),
                variants(directory.resolve("variants.json")),
                navigation(directory.resolve("navigation.json")),
                extractionCount == null ? new IntRangeConfig(1, 1) : range(extractionCount),
                extractions(directory.resolve("extraction_anchors.json")),
                spawnCount == null ? new IntRangeConfig(1, 1) : range(spawnCount),
                spawns(directory.resolve("spawn_anchors.json")),
                ranges(map, "loose_loot_group_counts"),
                looseLoot(directory.resolve("loose_loot_anchors.json")),
                extractionSwitches(directory.resolve("extraction_switches.json")));
    }

    private static JsonElement read(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        }
    }

    private static List<RaidZoneConfig> zones(JsonArray array) {
        List<RaidZoneConfig> result = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject o = element.getAsJsonObject();
            result.add(new RaidZoneConfig(string(o, "id"), integer(o, "tier", 1),
                    range(o.getAsJsonObject("active_containers")), range(o.getAsJsonObject("loot_budget")),
                    strings(o, "allowed_categories")));
        }
        return List.copyOf(result);
    }

    private static List<RaidContainerTypeConfig> types(JsonArray array) {
        List<RaidContainerTypeConfig> result = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject o = element.getAsJsonObject();
            result.add(new RaidContainerTypeConfig(string(o, "id"), location(o, "block"),
                    integer(o, "columns", 1), integer(o, "rows", 1), location(o, "fallback_loot_table"),
                    decimal(o, "default_quality_multiplier", 1.0D), strings(o, "allowed_categories")));
        }
        return List.copyOf(result);
    }

    private static List<RaidContainerAnchorConfig> anchors(JsonArray array) {
        List<RaidContainerAnchorConfig> result = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject o = element.getAsJsonObject();
            result.add(new RaidContainerAnchorConfig(string(o, "id"), string(o, "zone"), string(o, "group"),
                    intArray(o, "pos"), string(o, "container_type"), decimal(o, "quality_multiplier", 0),
                    integer(o, "weight", 100), !o.has("enabled") || o.get("enabled").getAsBoolean(),
                    strings(o, "tags")));
        }
        return List.copyOf(result);
    }

    private static List<RaidVariantGroupConfig> variants(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return List.of();
        List<RaidVariantGroupConfig> result = new ArrayList<>();
        for (JsonElement groupElement : read(path).getAsJsonArray()) {
            JsonObject group = groupElement.getAsJsonObject();
            List<RaidVariantConfig> variants = new ArrayList<>();
            if (group.has("variants") && group.get("variants").isJsonArray()) {
                for (JsonElement variantElement : group.getAsJsonArray("variants")) {
                    JsonObject variant = variantElement.getAsJsonObject();
                    List<RaidBlockPatchConfig> patches = new ArrayList<>();
                    if (variant.has("patches") && variant.get("patches").isJsonArray()) {
                        for (JsonElement patchElement : variant.getAsJsonArray("patches")) {
                            JsonObject patch = patchElement.getAsJsonObject();
                            patches.add(new RaidBlockPatchConfig(flexibleIntArray(patch, "pos"),
                                    string(patch, "block")));
                        }
                    }
                    variants.add(new RaidVariantConfig(string(variant, "id"),
                            integer(variant, "weight", 1), patches, strings(variant, "tags")));
                }
            }
            result.add(new RaidVariantGroupConfig(string(group, "id"),
                    integer(group, "choose", 1), variants));
        }
        return List.copyOf(result);
    }

    private static RaidNavigationConfig navigation(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return RaidNavigationConfig.empty();
        JsonObject root = read(path).getAsJsonObject();
        List<RaidNavigationNodeConfig> nodes = new ArrayList<>();
        if (root.has("nodes") && root.get("nodes").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("nodes")) {
                JsonObject node = element.getAsJsonObject();
                nodes.add(new RaidNavigationNodeConfig(string(node, "id"),
                        flexibleIntArray(node, "pos"), strings(node, "tags")));
            }
        }
        List<RaidNavigationEdgeConfig> edges = new ArrayList<>();
        if (root.has("edges") && root.get("edges").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("edges")) {
                JsonObject edge = element.getAsJsonObject();
                edges.add(new RaidNavigationEdgeConfig(string(edge, "id"), string(edge, "from"),
                        string(edge, "to"), bool(edge, "bidirectional", false),
                        bool(edge, "enabled_by_default", true), strings(edge, "required_tags"),
                        strings(edge, "disabled_by_tags")));
            }
        }
        List<RaidNavigationCheckConfig> checks = new ArrayList<>();
        if (root.has("checks") && root.get("checks").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("checks")) {
                JsonObject check = element.getAsJsonObject();
                checks.add(new RaidNavigationCheckConfig(string(check, "id"), string(check, "from"),
                        string(check, "to"), string(check, "to_any_tag"),
                        bool(check, "required", false)));
            }
        }
        return new RaidNavigationConfig(nodes, edges, checks);
    }

    private static List<RaidExtractionAnchorConfig> extractions(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return List.of();
        List<RaidExtractionAnchorConfig> result = new ArrayList<>();
        for (JsonElement element : read(path).getAsJsonArray()) {
            JsonObject anchor = element.getAsJsonObject();
            result.add(new RaidExtractionAnchorConfig(string(anchor, "id"), string(anchor, "node"),
                    flexibleIntArray(anchor, "pos"), decimal(anchor, "radius", 3.0D),
                    integer(anchor, "weight", 1), bool(anchor, "enabled", true),
                    bool(anchor, "always_active", false), strings(anchor, "requires_tags"),
                    strings(anchor, "forbidden_tags"), string(anchor, "display_name"),
                    strings(anchor, "tags"), availability(anchor), trigger(anchor), timer(anchor),
                    integer(anchor, "use_limit", -1), string(anchor, "consume_use_on")));
        }
        return List.copyOf(result);
    }

    private static List<RaidSpawnAnchorConfig> spawns(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return List.of();
        List<RaidSpawnAnchorConfig> result = new ArrayList<>();
        for (JsonElement element : read(path).getAsJsonArray()) {
            JsonObject anchor = element.getAsJsonObject();
            result.add(new RaidSpawnAnchorConfig(string(anchor, "id"), string(anchor, "node"),
                    flexibleIntArray(anchor, "pos"), (float) decimal(anchor, "yaw", 0.0D),
                    (float) decimal(anchor, "pitch", 0.0D), integer(anchor, "weight", 1),
                    bool(anchor, "enabled", true), bool(anchor, "always_active", false),
                    strings(anchor, "requires_tags"), strings(anchor, "forbidden_tags"),
                    string(anchor, "display_name"), strings(anchor, "tags")));
        }
        return List.copyOf(result);
    }

    private static RaidExtractionAvailabilityConfig availability(JsonObject anchor) {
        if (!anchor.has("availability")) return RaidExtractionAvailabilityConfig.always();
        JsonObject value = anchor.getAsJsonObject("availability");
        return new RaidExtractionAvailabilityConfig(string(value, "type"), integer(value, "seconds", 0));
    }
    private static RaidExtractionTimerConfig timer(JsonObject anchor) {
        if (!anchor.has("timer")) return RaidExtractionTimerConfig.defaultPlayer();
        JsonObject value = anchor.getAsJsonObject("timer");
        return new RaidExtractionTimerConfig(string(value, "type"), integer(value, "seconds", 0), string(value, "leave_behavior"));
    }
    private static RaidExtractionTriggerConfig trigger(JsonObject anchor) {
        if (!anchor.has("trigger")) return RaidExtractionTriggerConfig.none();
        JsonObject value = anchor.getAsJsonObject("trigger");
        List<RaidItemRequirementConfig> requirements = new ArrayList<>();
        if (value.has("requirements")) for (JsonElement element : value.getAsJsonArray("requirements")) {
            JsonObject item = element.getAsJsonObject();
            requirements.add(new RaidItemRequirementConfig(ResourceLocation.tryParse(string(item, "item")),
                    integer(item, "count", 0)));
        }
        return new RaidExtractionTriggerConfig(string(value, "type"), string(value, "switch_id"), requirements);
    }

    private static Map<String, IntRangeConfig> ranges(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonObject()) return Map.of();
        Map<String, IntRangeConfig> result = new LinkedHashMap<>();
        root.getAsJsonObject(key).entrySet().forEach(entry ->
                result.put(entry.getKey(), range(entry.getValue().getAsJsonObject())));
        return Map.copyOf(result);
    }

    private static List<RaidLooseLootAnchorConfig> looseLoot(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return List.of();
        List<RaidLooseLootAnchorConfig> result = new ArrayList<>();
        for (JsonElement element : read(path).getAsJsonArray()) {
            JsonObject anchor = element.getAsJsonObject();
            result.add(new RaidLooseLootAnchorConfig(string(anchor, "id"), string(anchor, "group_id"),
                    flexibleIntArray(anchor, "pos"), string(anchor, "container_type"),
                    integer(anchor, "point_budget", 0), decimal(anchor, "quality_multiplier", 1.0D),
                    integer(anchor, "weight", 1), bool(anchor, "enabled", true),
                    bool(anchor, "always_active", false), strings(anchor, "requires_tags"),
                    strings(anchor, "forbidden_tags"), strings(anchor, "tags")));
        }
        return List.copyOf(result);
    }
    private static List<RaidExtractionSwitchConfig> extractionSwitches(Path path) throws IOException {
        if (!Files.isRegularFile(path)) return List.of();
        List<RaidExtractionSwitchConfig> result = new ArrayList<>();
        for (JsonElement element : read(path).getAsJsonArray()) {
            JsonObject value = element.getAsJsonObject();
            result.add(new RaidExtractionSwitchConfig(string(value, "id"), flexibleIntArray(value, "pos"),
                    decimal(value, "radius", 3.0D), bool(value, "enabled", true),
                    strings(value, "requires_tags"), strings(value, "forbidden_tags"),
                    string(value, "display_name"), strings(value, "tags")));
        }
        return List.copyOf(result);
    }

    private static IntRangeConfig range(JsonObject o) {
        return o == null ? new IntRangeConfig(0, 0)
                : new IntRangeConfig(integer(o, "min", 0), integer(o, "max", 0));
    }

    private static ResourceLocation location(JsonObject o, String key) {
        return ResourceLocation.tryParse(string(o, key));
    }

    private static ResourceLocation optionalLocation(JsonObject o, String key) {
        String value = string(o, key);
        if (value.isBlank()) return null;
        ResourceLocation parsed = ResourceLocation.tryParse(value);
        if (parsed == null) throw new JsonParseException("Invalid resource location for " + key + ": " + value);
        return parsed;
    }

    private static String string(JsonObject o, String key) {
        return o != null && o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static int integer(JsonObject o, String key, int fallback) {
        return o != null && o.has(key) ? o.get(key).getAsInt() : fallback;
    }

    private static double decimal(JsonObject o, String key, double fallback) {
        return o != null && o.has(key) ? o.get(key).getAsDouble() : fallback;
    }

    private static boolean bool(JsonObject o, String key, boolean fallback) {
        return o != null && o.has(key) ? o.get(key).getAsBoolean() : fallback;
    }

    private static int[] intArray(JsonObject o, String key) {
        JsonArray a = o.getAsJsonArray(key);
        return new int[]{a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()};
    }

    private static int[] optionalIntArray(JsonObject o, String key) {
        return o != null && o.has(key) && o.get(key).isJsonArray() ? intArray(o, key) : null;
    }

    private static int[] flexibleIntArray(JsonObject o, String key) {
        if (o == null || !o.has(key) || !o.get(key).isJsonArray()) return null;
        JsonArray array = o.getAsJsonArray(key);
        int[] result = new int[array.size()];
        for (int i = 0; i < array.size(); i++) result[i] = array.get(i).getAsInt();
        return result;
    }

    private static List<String> strings(JsonObject o, String key) {
        if (o == null || !o.has(key)) return List.of();
        List<String> result = new ArrayList<>();
        o.getAsJsonArray(key).forEach(e -> result.add(e.getAsString()));
        return List.copyOf(result);
    }
}
