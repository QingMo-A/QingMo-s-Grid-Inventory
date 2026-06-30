package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class RaidMapExampleWriter {
    private RaidMapExampleWriter() {}

    public static Path write(Path configRoot, String mapId, String dimension, BlockPos origin) throws IOException {
        Path directory = configRoot.resolve("df_grid_inventory/raid/maps").resolve(mapId);
        Files.createDirectories(directory);
        writeNew(directory.resolve("map.json"), """
                {
                  "id": "%s",
                  "display_name": "Example Raid Map",
                  "dimension": "%s",
                  "template": "",
                  "origin": [%d, %d, %d],
                  "default_spawn": [0, 1, 0],
                  "bounds": {"min": [-32, -16, -32], "max": [32, 32, 32]},
                  "expected_players": 4,
                  "raid_time_seconds": 1800
                }
                """.formatted(mapId, dimension, origin.getX(), origin.getY(), origin.getZ()));
        writeNew(directory.resolve("zones.json"), """
                [
                  {"id":"storage","tier":1,"active_containers":{"min":2,"max":3},"loot_budget":{"min":1000,"max":2000},"allowed_categories":["food","tool"]},
                  {"id":"medical","tier":1,"active_containers":{"min":1,"max":2},"loot_budget":{"min":800,"max":1600},"allowed_categories":["medical"]}
                ]
                """);
        writeNew(directory.resolve("container_types.json"), """
                [
                  {"id":"generic_crate","block":"df_grid_inventory:searchable_grid_container","columns":8,"rows":5,"fallback_loot_table":"df_grid_inventory:grid_containers/searchable_grid_container","default_quality_multiplier":1.0,"allowed_categories":["food","tool"]},
                  {"id":"medical_box","block":"df_grid_inventory:searchable_grid_container","columns":6,"rows":4,"fallback_loot_table":"df_grid_inventory:grid_containers/searchable_grid_container","default_quality_multiplier":1.2,"allowed_categories":["medical"]}
                ]
                """);
        writeNew(directory.resolve("container_anchors.json"), anchors());
        return directory;
    }

    private static String anchors() {
        return """
                [
                  {"id":"storage_01","zone":"storage","group":"left","pos":[%d,%d,%d],"container_type":"generic_crate","quality_multiplier":0.8,"weight":100,"enabled":true,"tags":["storage"]},
                  {"id":"storage_02","zone":"storage","group":"left","pos":[%d,%d,%d],"container_type":"generic_crate","quality_multiplier":1.0,"weight":100,"enabled":true,"tags":["storage"]},
                  {"id":"storage_03","zone":"storage","group":"right","pos":[%d,%d,%d],"container_type":"generic_crate","quality_multiplier":1.2,"weight":100,"enabled":true,"tags":["storage"]},
                  {"id":"medical_01","zone":"medical","group":"room","pos":[%d,%d,%d],"container_type":"medical_box","quality_multiplier":1.0,"weight":100,"enabled":true,"tags":["medical"]},
                  {"id":"medical_02","zone":"medical","group":"room","pos":[%d,%d,%d],"container_type":"medical_box","quality_multiplier":1.2,"weight":100,"enabled":true,"tags":["medical"]}
                ]
                """.formatted(2,0,0, 4,0,0, 6,0,0, 2,0,4, 4,0,4);
    }

    private static void writeNew(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }
}
