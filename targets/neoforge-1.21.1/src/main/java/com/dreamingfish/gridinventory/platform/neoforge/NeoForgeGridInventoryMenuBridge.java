package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import com.dreamingfish.gridinventory.platform.menu.GridInventoryMenuBridge;
import com.dreamingfish.gridinventory.target.neoforge1211.menu.NeoForge1211MenuOpenDataCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public final class NeoForgeGridInventoryMenuBridge implements GridInventoryMenuBridge {
    @Override
    public void openGridInventory(ServerPlayer player, int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data) {
        GridInventoryMenuOpenData openData = new GridInventoryMenuOpenData(sourceSlot, hand, playerInventory, data.copy());
        player.openMenu(new Provider(openData), buffer -> NeoForge1211MenuOpenDataCodec.encode(openData, buffer));
    }

    private record Provider(GridInventoryMenuOpenData data) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable(data.playerInventory()
                    ? "container.df_grid_inventory.player_grid_inventory"
                    : "container.df_grid_inventory.small_grid_bag");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return GridInventoryMenu.fromOpenData(containerId, playerInventory, data);
        }
    }
}
