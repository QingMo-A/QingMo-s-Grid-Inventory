package com.dreamingfish.gridinventory.common.raid.runtime;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class RaidManifestStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private RaidManifestStorage() {}

    public static Path storageRoot(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("df_grid_inventory/raid/manifests");
    }

    public static void save(MinecraftServer server, RaidManifest manifest) throws IOException {
        Path root = storageRoot(server);
        Files.createDirectories(root);
        Files.writeString(root.resolve(manifest.raidId() + ".json"), GSON.toJson(encode(manifest)),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public static Optional<RaidManifest> load(MinecraftServer server, long raidId) {
        Path path = storageRoot(server).resolve(raidId + ".json");
        if (!Files.isRegularFile(path)) return Optional.empty();
        try {
            return Optional.of(decode(JsonParser.parseString(Files.readString(path)).getAsJsonObject()));
        } catch (Exception exception) {
            DFGridInventory.LOGGER.warn("Failed to load raid manifest {}", path, exception);
            return Optional.empty();
        }
    }

    public static List<RaidManifest> loadAll(MinecraftServer server) {
        Path root = storageRoot(server);
        if (!Files.isDirectory(root)) return List.of();
        List<RaidManifest> result = new ArrayList<>();
        try (var paths = Files.list(root)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".json")).forEach(path -> {
                try {
                    result.add(decode(JsonParser.parseString(Files.readString(path)).getAsJsonObject()));
                } catch (Exception exception) {
                    DFGridInventory.LOGGER.warn("Skipping damaged raid manifest {}", path, exception);
                }
            });
        } catch (IOException exception) {
            DFGridInventory.LOGGER.warn("Failed to scan raid manifest storage {}", root, exception);
        }
        result.sort(Comparator.comparingLong(RaidManifest::raidId));
        return List.copyOf(result);
    }

    public static boolean delete(MinecraftServer server, long raidId) throws IOException {
        return Files.deleteIfExists(storageRoot(server).resolve(raidId + ".json"));
    }

    private static JsonObject encode(RaidManifest manifest) {
        JsonObject root = new JsonObject();
        root.addProperty("raid_id", manifest.raidId());
        root.addProperty("raid_seed", manifest.raidSeed());
        root.addProperty("map_id", manifest.mapId());
        root.addProperty("dimension_id", manifest.dimensionId());
        root.add("paste_origin", pos(manifest.pasteOrigin()));
        root.add("default_spawn_local", pos(manifest.defaultSpawnLocal()));
        root.addProperty("state", manifest.state().name());
        JsonArray zones = new JsonArray();
        manifest.zones().values().forEach(zone -> {
            JsonObject value = new JsonObject();
            value.addProperty("zone_id", zone.zoneId());
            value.addProperty("active_container_count", zone.activeContainerCount());
            value.addProperty("loot_budget", zone.lootBudget());
            value.add("active_anchor_ids", GSON.toJsonTree(zone.activeAnchorIds()));
            value.add("anchor_budgets", GSON.toJsonTree(zone.anchorBudgets()));
            zones.add(value);
        });
        root.add("zones", zones);
        JsonArray containers = new JsonArray();
        manifest.activeContainers().forEach(container -> {
            JsonObject value = new JsonObject();
            value.addProperty("anchor_id", container.anchorId());
            value.addProperty("zone_id", container.zoneId());
            value.addProperty("group_id", container.groupId());
            value.add("local_pos", pos(container.localPos()));
            value.addProperty("container_type", container.containerType());
            value.addProperty("point_budget", container.pointBudget());
            value.addProperty("quality_multiplier", container.qualityMultiplier());
            value.addProperty("loot_seed", container.lootSeed());
            containers.add(value);
        });
        root.add("active_containers", containers);
        JsonArray variants = new JsonArray();
        manifest.variantSelections().forEach(selection -> {
            JsonObject value = new JsonObject();
            value.addProperty("group_id", selection.groupId());
            value.addProperty("variant_id", selection.variantId());
            value.add("tags", GSON.toJsonTree(selection.tags()));
            JsonArray patches = new JsonArray();
            selection.patches().forEach(patch -> {
                JsonObject patchValue = new JsonObject();
                patchValue.add("local_pos", pos(patch.localPos()));
                patchValue.addProperty("block_state", patch.blockState());
                patches.add(patchValue);
            });
            value.add("patches", patches);
            variants.add(value);
        });
        root.add("variant_selections", variants);
        JsonArray extractions = new JsonArray();
        manifest.activeExtractions().forEach(extraction -> {
            JsonObject value = new JsonObject();
            value.addProperty("id", extraction.id());
            value.addProperty("node", extraction.node());
            value.add("local_pos", pos(extraction.localPos()));
            value.addProperty("radius", extraction.radius());
            value.addProperty("display_name", extraction.displayName());
            value.add("tags", GSON.toJsonTree(extraction.tags()));
            extractions.add(value);
        });
        root.add("active_extractions", extractions);
        return root;
    }

    private static RaidManifest decode(JsonObject root) {
        Map<String, ZoneRaidState> zones = new LinkedHashMap<>();
        if (root.has("zones")) for (JsonElement element : root.getAsJsonArray("zones")) {
            JsonObject value = element.getAsJsonObject();
            List<String> ids = new ArrayList<>();
            if (value.has("active_anchor_ids")) value.getAsJsonArray("active_anchor_ids").forEach(e -> ids.add(e.getAsString()));
            Map<String, Integer> budgets = new LinkedHashMap<>();
            if (value.has("anchor_budgets")) value.getAsJsonObject("anchor_budgets")
                    .entrySet().forEach(e -> budgets.put(e.getKey(), e.getValue().getAsInt()));
            String zoneId = string(value, "zone_id", "");
            zones.put(zoneId, new ZoneRaidState(zoneId, integer(value, "active_container_count", ids.size()),
                    integer(value, "loot_budget", 0), List.copyOf(ids), Map.copyOf(budgets)));
        }
        List<ContainerAnchorActivation> containers = new ArrayList<>();
        if (root.has("active_containers")) for (JsonElement element : root.getAsJsonArray("active_containers")) {
            JsonObject value = element.getAsJsonObject();
            containers.add(new ContainerAnchorActivation(string(value, "anchor_id", ""),
                    string(value, "zone_id", ""), string(value, "group_id", ""), readPos(value, "local_pos"),
                    string(value, "container_type", ""), integer(value, "point_budget", 0),
                    decimal(value, "quality_multiplier", 1.0D), longValue(value, "loot_seed", 0L)));
        }
        List<RaidVariantSelection> variants = new ArrayList<>();
        if (root.has("variant_selections") && root.get("variant_selections").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("variant_selections")) {
                try {
                    JsonObject value = element.getAsJsonObject();
                    List<String> tags = new ArrayList<>();
                    if (value.has("tags") && value.get("tags").isJsonArray()) {
                        value.getAsJsonArray("tags").forEach(tag -> tags.add(tag.getAsString()));
                    }
                    List<RaidBlockPatch> patches = new ArrayList<>();
                    if (value.has("patches") && value.get("patches").isJsonArray()) {
                        for (JsonElement patchElement : value.getAsJsonArray("patches")) {
                            try {
                                JsonObject patch = patchElement.getAsJsonObject();
                                String blockState = string(patch, "block_state", "");
                                if (blockState.isBlank()) throw new JsonParseException("Empty block_state");
                                patches.add(new RaidBlockPatch(readRequiredPos(patch, "local_pos"), blockState));
                            } catch (Exception exception) {
                                DFGridInventory.LOGGER.warn("Skipping damaged raid variant patch", exception);
                            }
                        }
                    }
                    variants.add(new RaidVariantSelection(string(value, "group_id", ""),
                            string(value, "variant_id", ""), tags, patches));
                } catch (Exception exception) {
                    DFGridInventory.LOGGER.warn("Skipping damaged raid variant selection", exception);
                }
            }
        }
        List<RaidExtractionActivation> extractions = new ArrayList<>();
        if (root.has("active_extractions") && root.get("active_extractions").isJsonArray()) {
            for (JsonElement element : root.getAsJsonArray("active_extractions")) {
                try {
                    JsonObject value = element.getAsJsonObject();
                    String id = string(value, "id", "");
                    if (id.isBlank()) throw new JsonParseException("Empty extraction id");
                    List<String> tags = new ArrayList<>();
                    if (value.has("tags") && value.get("tags").isJsonArray()) {
                        value.getAsJsonArray("tags").forEach(tag -> tags.add(tag.getAsString()));
                    }
                    extractions.add(new RaidExtractionActivation(id, string(value, "node", ""),
                            readRequiredPos(value, "local_pos"), decimal(value, "radius", 3.0D),
                            string(value, "display_name", ""), tags));
                } catch (Exception exception) {
                    DFGridInventory.LOGGER.warn("Skipping damaged raid extraction activation", exception);
                }
            }
        }
        RaidLifecycleState state;
        try { state = RaidLifecycleState.valueOf(string(root, "state", "CREATED")); }
        catch (IllegalArgumentException ignored) { state = RaidLifecycleState.CREATED; }
        return new RaidManifest(longValue(root, "raid_id", 0L), longValue(root, "raid_seed", 0L),
                string(root, "map_id", ""), string(root, "dimension_id", "minecraft:overworld"),
                readPos(root, "paste_origin"), readPos(root, "default_spawn_local"), state,
                Map.copyOf(zones), List.copyOf(containers), List.copyOf(variants), List.copyOf(extractions));
    }

    private static JsonArray pos(BlockPos pos) {
        JsonArray value = new JsonArray();
        value.add(pos.getX()); value.add(pos.getY()); value.add(pos.getZ());
        return value;
    }
    private static BlockPos readPos(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonArray() || root.getAsJsonArray(key).size() != 3) return BlockPos.ZERO;
        JsonArray value = root.getAsJsonArray(key);
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }
    private static BlockPos readRequiredPos(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonArray() || root.getAsJsonArray(key).size() != 3) {
            throw new JsonParseException("Invalid " + key);
        }
        JsonArray value = root.getAsJsonArray(key);
        return new BlockPos(value.get(0).getAsInt(), value.get(1).getAsInt(), value.get(2).getAsInt());
    }
    private static String string(JsonObject o, String key, String fallback) { return o.has(key) ? o.get(key).getAsString() : fallback; }
    private static int integer(JsonObject o, String key, int fallback) { return o.has(key) ? o.get(key).getAsInt() : fallback; }
    private static long longValue(JsonObject o, String key, long fallback) { return o.has(key) ? o.get(key).getAsLong() : fallback; }
    private static double decimal(JsonObject o, String key, double fallback) { return o.has(key) ? o.get(key).getAsDouble() : fallback; }
}
