package com.dreamingfish.gridinventory.client.screen;

import com.dreamingfish.gridinventory.common.menu.SearchableGridContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SearchableGridContainerScreen extends GridInventoryScreen {
    public SearchableGridContainerScreen(SearchableGridContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
