package com.dreamingfish.gridinventory.common.raid.config;

import com.dreamingfish.gridinventory.DFGridInventory;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class RaidLootItemDefinitionRegistry {
    private static List<RaidLootItemDefinitionConfig> definitions = List.of();
    private static Map<ResourceLocation, RaidLootItemDefinitionConfig> byItem = Map.of();
    private static RaidLootItemDefinitionValidationResult validation =
            new RaidLootItemDefinitionValidationResult(List.of());
    private static Map<String, String> errors = Map.of();

    private RaidLootItemDefinitionRegistry() {}

    public static void reload(Path configRoot) {
        try {
            RaidLootItemDefinitionLoader.LoadResult loaded =
                    RaidLootItemDefinitionLoader.load(configRoot);
            definitions = loaded.definitions();
            validation = RaidLootItemDefinitionValidator.validate(definitions, loaded.issues());
            Map<ResourceLocation, RaidLootItemDefinitionConfig> indexed = new LinkedHashMap<>();
            definitions.forEach(definition -> indexed.putIfAbsent(definition.item(), definition));
            byItem = Map.copyOf(indexed);
            Map<String, String> currentErrors = new LinkedHashMap<>();
            int index = 0;
            for (RaidLootItemDefinitionValidationIssue issue : validation.issues()) {
                if (issue.error()) currentErrors.put("error_" + index++, issue.message());
            }
            errors = Map.copyOf(currentErrors);
        } catch (Exception exception) {
            definitions = List.of();
            byItem = Map.of();
            validation = new RaidLootItemDefinitionValidationResult(List.of(
                    new RaidLootItemDefinitionValidationIssue(true, exception.getMessage())));
            errors = Map.of("load", String.valueOf(exception.getMessage()));
            DFGridInventory.LOGGER.error("Failed to load raid loot item definitions", exception);
        }
    }

    public static List<RaidLootItemDefinitionConfig> all() {
        return definitions;
    }

    public static List<RaidLootItemDefinitionConfig> enabled() {
        return definitions.stream().filter(RaidLootItemDefinitionConfig::enabled).toList();
    }

    public static Optional<RaidLootItemDefinitionConfig> get(ResourceLocation item) {
        return Optional.ofNullable(byItem.get(item));
    }

    public static Map<String, String> errors() {
        return errors;
    }

    public static RaidLootItemDefinitionValidationResult validation() {
        return validation;
    }

    public static Map<String, Long> categoryCounts() {
        return counts(RaidLootItemDefinitionConfig::category);
    }

    public static Map<String, Long> rarityCounts() {
        return counts(RaidLootItemDefinitionConfig::rarity);
    }

    private static Map<String, Long> counts(
            java.util.function.Function<RaidLootItemDefinitionConfig, String> classifier) {
        return enabled().stream().collect(Collectors.groupingBy(classifier, TreeMap::new, Collectors.counting()));
    }
}
