package com.dreamingfish.gridinventory.platform.neoforge;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.platform.AccessorySlotView;
import com.dreamingfish.gridinventory.platform.GridInventoryAccessoryBridge;
import com.dreamingfish.gridinventory.platform.neoforge.compat.NeoForgeCuriosIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NeoForgeCuriosAccessoryBridge implements GridInventoryAccessoryBridge {
    @Override
    public boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    @Override
    public void registerBackpackAccessories() {
        CuriosApi.registerCurio(ModItems.GRID_BACKPACK.get(), backpackCurio());
        CuriosApi.registerCurio(ModItems.GRAY_FIELD_BACKPACK.get(), backpackCurio());
        CuriosApi.registerCurio(ModItems.LEATHER_BACKPACK.get(), backpackCurio());
        CuriosApi.registerCurio(ModItems.LIME_HIKING_BACKPACK.get(), backpackCurio());
        CuriosApi.registerCurio(ModItems.MEDIUM_HIKING_BACKPACK.get(), backpackCurio());
        CuriosApi.registerCurio(ModItems.MILITARY_HIKING_BACKPACK.get(), backpackCurio());
        CuriosApi.registerCurio(ModItems.TACTICAL_BACKPACK.get(), backpackCurio());
    }

    @Override
    public boolean canQuickEquip(Player player, ItemStack stack) {
        return NeoForgeCuriosIntegration.canQuickEquip(player, stack);
    }

    @Override
    public boolean quickEquip(Player player, ItemStack source) {
        return NeoForgeCuriosIntegration.quickEquip(player, source);
    }

    @Override
    public List<AccessorySlotView> collectSlots(Player player) {
        return NeoForgeCuriosIntegration.collectSlots(player);
    }

    @Override
    public boolean canPlaceInCurio(Player player, String identifier, int index, ItemStack stack, boolean targetEmpty) {
        return NeoForgeCuriosIntegration.canPlaceInCurio(player, identifier, index, stack, targetEmpty);
    }

    @Override
    public boolean movePlayerSlotToAccessory(Player player, int sourcePlayerSlot, String identifier, int index) {
        return NeoForgeCuriosIntegration.movePlayerSlotToCurio(player, sourcePlayerSlot, identifier, index);
    }

    @Override
    public boolean moveGridEntryToAccessory(Player player, GridInventoryData grid, UUID entryId, String identifier, int index) {
        return NeoForgeCuriosIntegration.moveGridEntryToCurio(player, grid, entryId, identifier, index);
    }

    @Override
    public boolean moveEquipmentEntryToAccessory(Player player, GridInventoryData inventory, UUID entryId, String identifier, int index) {
        return NeoForgeCuriosIntegration.moveEquipmentEntryToCurio(player, inventory, entryId, identifier, index);
    }

    @Override
    public boolean moveAccessoryToPlayerSlot(Player player, String identifier, int index, int targetPlayerSlot) {
        return NeoForgeCuriosIntegration.moveCurioToPlayerSlot(player, identifier, index, targetPlayerSlot);
    }

    @Override
    public boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated) {
        return NeoForgeCuriosIntegration.moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated);
    }

    @Override
    public boolean moveAccessoryToGrid(Player player, GridInventoryData grid, String identifier, int index, int targetX, int targetY, boolean rotated, boolean targetFolded) {
        return NeoForgeCuriosIntegration.moveCurioToGrid(player, grid, identifier, index, targetX, targetY, rotated, targetFolded);
    }

    @Override
    public Optional<ItemStack> getAccessoryStack(Player player, String identifier, int index) {
        return NeoForgeCuriosIntegration.getCurioStack(player, identifier, index);
    }

    @Override
    public void setAccessoryStack(Player player, String identifier, int index, ItemStack stack) {
        NeoForgeCuriosIntegration.setCurioStack(player, identifier, index, stack);
    }

    private static ICurioItem backpackCurio() {
        return new ICurioItem() {
            @Override
            public boolean canEquip(SlotContext slotContext, ItemStack stack) {
                return "back".equals(slotContext.identifier());
            }

            @Override
            public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
                if ("back".equals(slotContext.identifier())) {
                    com.dreamingfish.gridinventory.common.item.GridBackpackItem.unfold(stack);
                }
                return canEquip(slotContext, stack);
            }
        };
    }
}
