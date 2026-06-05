package com.dreamingfish.gridinventory.common.equipment;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.api.EquipmentStorageDefinition;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class EquipmentStorageManager {
    private static List<EquipmentStorageDefinition> rules = List.of();

    private EquipmentStorageManager() {
    }

    public static void replaceRules(List<EquipmentStorageDefinition> loadedRules) {
        rules = List.copyOf(loadedRules);
    }

    public static Optional<EquipmentStorageDefinition> getDefinition(ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty() || !GridInventoryServices.config().equipmentStorageEnabled() || !allowedSlot(slot)) {
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
        EquipmentStorageData current = com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().getEquipmentStorage(stack);
        Optional<EquipmentStorageDefinition> definition = getDefinition(stack, slot);
        if (current != null && !current.isEmpty()) {
            if (definition.isPresent()) {
                EquipmentStorageData refreshed = refreshStorageShape(current, definition.get());
                com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setEquipmentStorage(stack, refreshed);
                return refreshed;
            }
            return current;
        }
        EquipmentStorageData initialized = definition.map(EquipmentStorageManager::createStorage).orElse(EquipmentStorageData.EMPTY);
        if (!initialized.containers().isEmpty()) {
            com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setEquipmentStorage(stack, initialized);
        }
        return initialized;
    }

    private static EquipmentStorageData refreshStorageShape(EquipmentStorageData current, EquipmentStorageDefinition definition) {
        Map<String, NamedGridInventoryData> existing = current.containers().stream()
                .collect(Collectors.toMap(NamedGridInventoryData::id, Function.identity(), (first, second) -> first));
        return new EquipmentStorageData(definition.containers().stream()
                .map(container -> {
                    NamedGridInventoryData old = existing.get(container.id());
                    GridInventoryData inventory = old == null
                            ? GridInventoryData.withSections(container.resolvedColumns(), container.resolvedRows(), container.resolvedSections())
                            : new GridInventoryData(container.resolvedColumns(), container.resolvedRows(),
                                    old.inventory().getEntries(), container.resolvedSections());
                    return new NamedGridInventoryData(container.id(), container.title(), inventory);
                })
                .toList());
    }

    private static EquipmentStorageData createStorage(EquipmentStorageDefinition definition) {
        return new EquipmentStorageData(definition.containers().stream()
                .map(container -> new NamedGridInventoryData(container.id(), container.title(),
                        GridInventoryData.withSections(container.resolvedColumns(), container.resolvedRows(), container.resolvedSections())))
                .toList());
    }

    private static boolean allowedSlot(EquipmentSlot slot) {
        return (slot == EquipmentSlot.CHEST && GridInventoryServices.config().allowChestStorage())
                || (slot == EquipmentSlot.LEGS && GridInventoryServices.config().allowLegsStorage())
                || slot == EquipmentSlot.BODY;
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
