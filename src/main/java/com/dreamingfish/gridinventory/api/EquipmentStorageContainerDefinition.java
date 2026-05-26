package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record EquipmentStorageContainerDefinition(String id, String title, int columns, int rows, List<String> allowedTags, List<String> blockedTags) {
    public static final Codec<EquipmentStorageContainerDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EquipmentStorageContainerDefinition::id),
            Codec.STRING.fieldOf("title").forGetter(EquipmentStorageContainerDefinition::title),
            Codec.INT.fieldOf("columns").forGetter(EquipmentStorageContainerDefinition::columns),
            Codec.INT.fieldOf("rows").forGetter(EquipmentStorageContainerDefinition::rows),
            Codec.STRING.listOf().optionalFieldOf("allowed_tags", List.of()).forGetter(EquipmentStorageContainerDefinition::allowedTags),
            Codec.STRING.listOf().optionalFieldOf("blocked_tags", List.of()).forGetter(EquipmentStorageContainerDefinition::blockedTags)
    ).apply(instance, EquipmentStorageContainerDefinition::new));
}
