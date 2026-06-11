package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import com.dreamingfish.gridinventory.target.forge1201.menu.Forge1201MenuOpenDataCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;

public final class Forge1201MenuBridge implements GridInventoryMenuBridge {
    @Override
    public void openGridInventory(ServerPlayer player, int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data) {
        GridInventoryMenuOpenData openData = new GridInventoryMenuOpenData(sourceSlot, hand, playerInventory, data.copy());
        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("container.df_grid_inventory.grid_inventory");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player ignored) {
                return GridInventoryMenu.fromOpenData(containerId, inventory, openData);
            }
        }, buf -> Forge1201MenuOpenDataCodec.encode(openData, buf));
    }
}
