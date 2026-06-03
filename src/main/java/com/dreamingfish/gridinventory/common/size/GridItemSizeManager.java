package com.dreamingfish.gridinventory.common.size;

import com.dreamingfish.gridinventory.api.GridItemSize;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GridItemSizeManager {
    private static List<GridItemSizeRule> rules = List.of();

    private GridItemSizeManager() {
    }

    public static GridItemSize getSize(ItemStack stack) {
        if (stack.isEmpty()) {
            return defaultSize();
        }
        if (GridBackpackItem.isFolded(stack)) {
            return new GridItemSize(GridBackpackItem.foldedWidth(stack), GridBackpackItem.foldedHeight(stack), true);
        }
        for (GridItemSizeRule rule : rules) {
            if (rule.type() == GridItemSizeRule.Type.ITEM && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(rule.targetLocation())) {
                return rule.size();
            }
        }
        for (GridItemSizeRule rule : rules) {
            if (rule.type() == GridItemSizeRule.Type.TAG) {
                TagKey<Item> tag = TagKey.create(BuiltInRegistries.ITEM.key(), rule.targetLocation());
                if (stack.is(tag)) {
                    return rule.size();
                }
            }
        }
        for (GridItemSizeRule rule : rules) {
            if (rule.type() == GridItemSizeRule.Type.MODID && rule.matchesModId(stack)) {
                return rule.size();
            }
        }
        return defaultSize();
    }

    public static GridItemSize defaultSize() {
        return new GridItemSize(GridInventoryConfig.DEFAULT_ITEM_WIDTH.get(), GridInventoryConfig.DEFAULT_ITEM_HEIGHT.get(), GridInventoryConfig.DEFAULT_ROTATABLE.get());
    }

    public static void replaceRules(List<GridItemSizeRule> loadedRules) {
        rules = List.copyOf(loadedRules);
    }

    public static List<GridItemSizeRule> getRules() {
        return rules;
    }

    public static void replaceClientRules(List<GridItemSizeRule> syncedRules) {
        rules = new ArrayList<>(syncedRules);
    }
}
