package com.dreamingfish.gridinventory.client.creative;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class VanillaCreativeTabsSnapshot implements VanillaCreativeTabProvider {
    private List<VanillaCreativeTabView> cachedTabs;

    @Override
    public List<VanillaCreativeTabView> tabs() {
        if (cachedTabs == null) {
            cachedTabs = buildTabs();
        }
        return cachedTabs;
    }

    @Override
    public List<ItemStack> search(String query) {
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<ItemStack> source = tabs().stream()
                .filter(VanillaCreativeTabView::searchTab)
                .findFirst()
                .or(() -> tabs().stream().findFirst())
                .map(VanillaCreativeTabView::displayItems)
                .orElse(List.of());
        if (needle.isEmpty()) {
            return source.stream().map(ItemStack::copy).toList();
        }
        return source.stream()
                .filter(stack -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(needle))
                .map(ItemStack::copy)
                .toList();
    }

    @Override
    public Optional<VanillaCreativeTabView> selectedDefaultTab() {
        return tabs().stream().filter(tab -> !tab.searchTab()).findFirst().or(() -> tabs().stream().findFirst());
    }

    @Override
    public void refresh() {
        cachedTabs = null;
    }

    private static List<VanillaCreativeTabView> buildTabs() {
        rebuildVanillaContents();
        List<VanillaCreativeTabView> views = new ArrayList<>();
        int sourceIndex = 0;
        for (CreativeModeTab tab : CreativeModeTabs.tabs()) {
            if (tab == null || !tab.shouldDisplay() || !tab.hasAnyItems()) {
                continue;
            }
            boolean vanillaSearch = tab == CreativeModeTabs.searchTab();
            boolean search = vanillaSearch || tab.hasSearchBar();
            List<ItemStack> items = (search ? tab.getSearchTabDisplayItems() : tab.getDisplayItems())
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            if (items.isEmpty()) {
                continue;
            }
            VanillaCreativeTabView view = new VanillaCreativeTabView(tabId(tab, sourceIndex), tab.getDisplayName(), tab.getIconItem(), items, search, sourceIndex);
            if (vanillaSearch) {
                views.add(0, view);
            } else {
                views.add(view);
            }
            sourceIndex++;
        }
        moveVanillaSearchTabFirst(views);
        return List.copyOf(views);
    }

    private static void moveVanillaSearchTabFirst(List<VanillaCreativeTabView> views) {
        if (!views.isEmpty() && views.get(0).searchTab()) {
            return;
        }
        for (int index = 0; index < views.size(); index++) {
            if (views.get(index).searchTab()) {
                if (index > 0) {
                    views.add(0, views.remove(index));
                }
                return;
            }
        }
    }

    private static ResourceLocation tabId(CreativeModeTab tab, int index) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "creative_tab_" + index);
    }

    private static void rebuildVanillaContents() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.getConnection() == null) {
            return;
        }
        CreativeModeTabs.tryRebuildTabContents(minecraft.level.enabledFeatures(),
                minecraft.player.getPermissionLevel() >= 2, minecraft.getConnection().registryAccess());
    }
}
