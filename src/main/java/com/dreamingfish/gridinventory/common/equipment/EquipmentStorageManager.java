package com.dreamingfish.gridinventory.common.equipment;

import com.dreamingfish.gridinventory.api.EquipmentStorageDefinition;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class EquipmentStorageManager {
    private static List<EquipmentStorageDefinition> rules = List.of();

    private EquipmentStorageManager() {
    }

    public static void replaceRules(List<EquipmentStorageDefinition> loadedRules) {
        rules = List.copyOf(loadedRules);
    }

    public static Optional<EquipmentStorageDefinition> getDefinition(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty() || !GridInventoryConfig.EQUIPMENT_STORAGE_ENABLED.get() || !allowedSlot(slot)) {
            return Optional.empty();
        }
        for (GridItemSizeRule.Type type : List.of(GridItemSizeRule.Type.ITEM, GridItemSizeRule.Type.TAG, GridItemSizeRule.Type.MODID)) {
            for (EquipmentStorageDefinition rule : rules) {
                if (rule.slot() == slot && rule.type() == type && matches(rule, stack)) {
                    return Optional.of(rule);
                }
            }
        }
        return Optional.empty();
    }

    public static EquipmentStorageData initializeStorage(ItemStack stack, EquipmentSlot slot) {
        EquipmentStorageData current = stack.get(ModDataComponents.EQUIPMENT_STORAGE.get());
        if (current != null) {
            return current;
        }
        EquipmentStorageData initialized = getDefinition(stack, slot)
                .map(definition -> new EquipmentStorageData(definition.containers().stream()
                        .map(container -> new NamedGridInventoryData(container.id(), container.title(), new GridInventoryData(container.columns(), container.rows())))
                        .toList()))
                .orElse(EquipmentStorageData.EMPTY);
        if (!initialized.containers().isEmpty()) {
            stack.set(ModDataComponents.EQUIPMENT_STORAGE.get(), initialized);
        }
        return initialized;
    }

    public static boolean preventsUnequip(ItemStack stack) {
        EquipmentStorageData storage = stack.get(ModDataComponents.EQUIPMENT_STORAGE.get());
        return GridInventoryConfig.PREVENT_UNEQUIP_WHEN_STORAGE_NOT_EMPTY.get() && storage != null && !storage.isEmpty();
    }

    private static boolean allowedSlot(EquipmentSlot slot) {
        return (slot == EquipmentSlot.CHEST && GridInventoryConfig.ALLOW_CHEST_STORAGE.get())
                || (slot == EquipmentSlot.LEGS && GridInventoryConfig.ALLOW_LEGS_STORAGE.get());
    }

    private static boolean matches(EquipmentStorageDefinition rule, ItemStack stack) {
        if (rule.type() == GridItemSizeRule.Type.ITEM) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(rule.targetLocation());
        }
        if (rule.type() == GridItemSizeRule.Type.TAG) {
            return stack.is(TagKey.create(BuiltInRegistries.ITEM.key(), rule.targetLocation()));
        }
        return rule.matchesModId(stack);
    }
}
