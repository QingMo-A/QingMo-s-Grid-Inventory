package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.registry.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class PlayerGridInventoryOpener {
    private PlayerGridInventoryOpener() {
    }

    public static void open(ServerPlayer player) {
        GridInventoryData data = PlayerPocketDefinitionManager.refreshShape(player.getData(ModAttachments.PLAYER_GRID_INVENTORY)).copy();
        data.setChangeListener(() -> player.setData(ModAttachments.PLAYER_GRID_INVENTORY, data.copy()));
        EquipmentStorageManager.initializeStorage(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST);
        EquipmentStorageManager.initializeStorage(player.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
        player.setData(ModAttachments.PLAYER_GRID_INVENTORY, data.copy());
        player.openMenu(new Provider(data), buffer -> {
            buffer.writeVarInt(-1);
            buffer.writeEnum(InteractionHand.MAIN_HAND);
            buffer.writeBoolean(true);
            data.encode(buffer);
        });
    }

    private record Provider(GridInventoryData data) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable("container.df_grid_inventory.player_grid_inventory");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new GridInventoryMenu(containerId, playerInventory, -1, InteractionHand.MAIN_HAND, data.copy(), true);
        }

        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        }
    }
}
