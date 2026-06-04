package com.dreamingfish.gridinventory.common.folding;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BackpackFoldingManager {
    private static List<BackpackFoldingDefinition> rules = List.of();

    private BackpackFoldingManager() {
    }

    public static int foldedWidth(ItemStack stack) {
        return getDefinition(stack).map(BackpackFoldingDefinition::foldedWidth).orElse(GridInventoryServices.config().foldedBackpackWidth());
    }

    public static int foldedHeight(ItemStack stack) {
        return getDefinition(stack).map(BackpackFoldingDefinition::foldedHeight).orElse(GridInventoryServices.config().foldedBackpackHeight());
    }

    public static boolean usesRollModel(ItemStack stack) {
        return getDefinition(stack).map(BackpackFoldingDefinition::usesRollModel).orElse(false);
    }

    public static Optional<BackpackFoldingDefinition> getDefinition(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        for (GridItemSizeRule.Type type : List.of(GridItemSizeRule.Type.ITEM, GridItemSizeRule.Type.TAG, GridItemSizeRule.Type.MODID)) {
            for (BackpackFoldingDefinition rule : rules) {
                if (rule.type() == type && matches(rule, stack)) {
                    return Optional.of(rule);
                }
            }
        }
        return Optional.empty();
    }

    public static void replaceRules(List<BackpackFoldingDefinition> loadedRules) {
        rules = List.copyOf(loadedRules);
    }

    public static List<BackpackFoldingDefinition> getRules() {
        return rules;
    }

    public static void replaceClientRules(List<BackpackFoldingDefinition> syncedRules) {
        rules = new ArrayList<>(syncedRules);
    }

    private static boolean matches(BackpackFoldingDefinition rule, ItemStack stack) {
        if (rule.type() == GridItemSizeRule.Type.ITEM) {
            return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(rule.targetLocation());
        }
        if (rule.type() == GridItemSizeRule.Type.TAG) {
            TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), rule.targetLocation());
            return stack.is(tag);
        }
        return rule.matchesModId(stack);
    }
}
