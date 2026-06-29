package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SearchableGridContainerMenu extends GridInventoryMenu {
    private static final double MAX_DISTANCE_SQUARED = 64.0D;

    private final Inventory playerInventory;
    private final BlockPos blockPos;
    private final Component containerTitle;
    private final boolean playerCreative;

    public SearchableGridContainerMenu(int containerId, Inventory playerInventory, BlockPos blockPos,
                                       GridInventoryData containerGrid, Component containerTitle,
                                       boolean playerCreative) {
        super(ModMenus.SEARCHABLE_GRID_CONTAINER.get(), containerId, playerInventory, -1,
                InteractionHand.MAIN_HAND, containerGrid.copy(), false);
        this.playerInventory = playerInventory;
        this.blockPos = blockPos;
        this.containerTitle = containerTitle;
        this.playerCreative = playerCreative;
    }

    public static SearchableGridContainerMenu fromOpenData(int containerId, Inventory playerInventory,
                                                           SearchableGridContainerMenuOpenData data) {
        Component title = Component.translatable(data.titleKey());
        return new SearchableGridContainerMenu(containerId, playerInventory, data.blockPos(),
                data.containerGrid(), title, data.creativeMode());
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public Component containerTitle() {
        return containerTitle;
    }

    public boolean playerCreative() {
        return playerCreative;
    }

    @Override
    public void save() {
        Level level = playerInventory.player.level();
        if (level.isClientSide) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof SearchableGridContainerBlockEntity container) {
            container.setGridData(getGridData().copy());
            container.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.distanceToSqr(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D) > MAX_DISTANCE_SQUARED) {
            return false;
        }
        return player.level().getBlockEntity(blockPos) instanceof SearchableGridContainerBlockEntity;
    }
}
