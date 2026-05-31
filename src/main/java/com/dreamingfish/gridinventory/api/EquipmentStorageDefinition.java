package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record EquipmentStorageDefinition(GridItemSizeRule.Type type, String target, EquipmentSlot slot, List<EquipmentStorageContainerDefinition> containers) {
    public static final Codec<EquipmentStorageDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GridItemSizeRule.Type.CODEC.fieldOf("type").forGetter(EquipmentStorageDefinition::type),
            Codec.STRING.fieldOf("target").forGetter(EquipmentStorageDefinition::target),
            Codec.STRING.xmap(EquipmentStorageDefinition::decodeSlot, EquipmentStorageDefinition::encodeSlot).fieldOf("slot").forGetter(EquipmentStorageDefinition::slot),
            EquipmentStorageContainerDefinition.CODEC.listOf().fieldOf("containers").forGetter(EquipmentStorageDefinition::containers)
    ).apply(instance, EquipmentStorageDefinition::new));

    public ResourceLocation targetLocation() {
        return ResourceLocation.parse(target);
    }

    public boolean matchesModId(ItemStack stack) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(target);
    }

    private static EquipmentSlot decodeSlot(String slot) {
        return switch (slot) {
            case "chest" -> EquipmentSlot.CHEST;
            case "legs" -> EquipmentSlot.LEGS;
            case "back" -> EquipmentSlot.BODY;
            default -> throw new IllegalArgumentException("Unsupported equipment storage slot: " + slot);
        };
    }

    private static String encodeSlot(EquipmentSlot slot) {
        return switch (slot) {
            case CHEST -> "chest";
            case LEGS -> "legs";
            case BODY -> "back";
            default -> throw new IllegalArgumentException("Unsupported equipment storage slot: " + slot);
        };
    }
}
