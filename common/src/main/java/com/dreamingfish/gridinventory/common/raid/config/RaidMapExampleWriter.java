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
                  "raid_time_seconds": 1800,
                  "extraction_active_count": {"min": 1, "max": 1},
                  "spawn_active_count": {"min": 1, "max": 1},
                  "loose_loot_group_counts": {
                    "office_desks": {"min": 1, "max": 2}
                  }
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
        writeNew(directory.resolve("variants.json"), """
                [
                  {
                    "id": "example_blocker",
                    "choose": 1,
                    "variants": [
                      {
                        "id": "open",
                        "weight": 50,
                        "patches": [{"pos": [8, 0, 0], "block": "minecraft:air"}],
                        "tags": ["open"]
                      },
                      {
                        "id": "blocked",
                        "weight": 50,
                        "patches": [{"pos": [8, 0, 0], "block": "minecraft:iron_bars"}],
                        "tags": ["blocked"]
                      }
                    ]
                  }
                ]
                """);
        writeNew(directory.resolve("navigation.json"), """
                {
                  "nodes": [
                    {"id": "spawn", "pos": [0, 1, 0], "tags": ["spawn"]},
                    {"id": "main", "pos": [8, 1, 0], "tags": ["central"]},
                    {"id": "storage", "pos": [16, 1, 0], "tags": ["loot_zone"]},
                    {"id": "exit_a", "pos": [24, 1, 0], "tags": ["extraction"]}
                  ],
                  "edges": [
                    {"id": "spawn_to_main", "from": "spawn", "to": "main", "bidirectional": true, "enabled_by_default": true, "required_tags": [], "disabled_by_tags": []},
                    {"id": "main_to_storage", "from": "main", "to": "storage", "bidirectional": true, "enabled_by_default": true, "required_tags": [], "disabled_by_tags": ["blocked"]},
                    {"id": "main_to_exit_a", "from": "main", "to": "exit_a", "bidirectional": true, "enabled_by_default": true, "required_tags": [], "disabled_by_tags": []}
                  ],
                  "checks": [
                    {"id": "spawn_to_exit", "from": "spawn", "to_any_tag": "extraction", "required": true},
                    {"id": "spawn_to_loot", "from": "spawn", "to_any_tag": "loot_zone", "required": false}
                  ]
                }
                """);
        writeNew(directory.resolve("extraction_anchors.json"), """
                [
                  {"id":"exit_a","node":"exit_a","pos":[24,1,0],"radius":3.0,"weight":100,"enabled":true,"always_active":true,"requires_tags":[],"forbidden_tags":[],"display_name":"Always Exit","tags":["extraction"],"availability":{"type":"always"},"trigger":{"type":"none"},"timer":{"type":"player","seconds":5,"leave_behavior":"reset"},"use_limit":-1,"consume_use_on":"trigger"},
                  {"id":"late_exit","node":"exit_a","pos":[22,1,2],"radius":3.0,"weight":100,"enabled":true,"always_active":true,"requires_tags":[],"forbidden_tags":[],"display_name":"Late Exit","tags":["extraction","late"],"availability":{"type":"raid_remaining_lte","seconds":600},"trigger":{"type":"none"},"timer":{"type":"player","seconds":8,"leave_behavior":"reset"},"use_limit":-1,"consume_use_on":"trigger"},
                  {"id":"elevator_exit","node":"exit_a","pos":[22,1,-2],"radius":3.0,"weight":100,"enabled":true,"always_active":true,"requires_tags":[],"forbidden_tags":[],"display_name":"Elevator Exit","tags":["extraction","switch"],"availability":{"type":"always"},"trigger":{"type":"switch","switch_id":"elevator_power_switch"},"timer":{"type":"global","seconds":30,"leave_behavior":"ignore"},"use_limit":1,"consume_use_on":"trigger"},
                  {"id":"delivery_exit","node":"exit_a","pos":[20,1,0],"radius":3.0,"weight":100,"enabled":true,"always_active":true,"requires_tags":[],"forbidden_tags":[],"display_name":"Delivery Exit","tags":["extraction","item_turn_in"],"availability":{"type":"always"},"trigger":{"type":"item_turn_in","requirements":[{"item":"minecraft:emerald","count":3}]},"timer":{"type":"global","seconds":45,"leave_behavior":"ignore"},"use_limit":2,"consume_use_on":"success"}
                ]
                """);
        writeNew(directory.resolve("spawn_anchors.json"), """
                [
                  {
                    "id": "spawn_main",
                    "node": "spawn",
                    "pos": [0, 1, 0],
                    "yaw": 0.0,
                    "pitch": 0.0,
                    "weight": 100,
                    "enabled": true,
                    "always_active": true,
                    "requires_tags": [],
                    "forbidden_tags": [],
                    "display_name": "Example Spawn",
                    "tags": ["spawn"]
                  }
                ]
                """);
        writeNew(directory.resolve("loose_loot_anchors.json"), """
                [
                  {
                    "id": "desk_loot_01",
                    "group_id": "office_desks",
                    "pos": [8, 1, 4],
                    "container_type": "generic_crate",
                    "point_budget": 300,
                    "quality_multiplier": 1.0,
                    "weight": 100,
                    "enabled": true,
                    "always_active": false,
                    "requires_tags": [],
                    "forbidden_tags": [],
                    "tags": ["desk", "office"]
                  },
                  {
                    "id": "desk_loot_02",
                    "group_id": "office_desks",
                    "pos": [10, 1, 4],
                    "container_type": "generic_crate",
                    "point_budget": 400,
                    "quality_multiplier": 1.1,
                    "weight": 75,
                    "enabled": true,
                    "always_active": false,
                    "requires_tags": ["open"],
                    "forbidden_tags": ["blocked"],
                    "tags": ["desk", "office"]
                  }
                ]
                """);
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
