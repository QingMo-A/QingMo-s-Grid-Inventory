package com.dreamingfish.gridinventory.target.neoforge1211.mixin;

import com.dreamingfish.gridinventory.common.inventory.compat.PlayerGridInventoryAccess;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlaceRecipe.class)
public abstract class ServerPlaceRecipeMixin {
    @Shadow
    protected Inventory inventory;

    @Shadow
    protected RecipeBookMenu<?, ?> menu;

    @Inject(method = "moveItemToGrid", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$moveGridItemToCraftingSlot(
            Slot slot, ItemStack target, int amount, CallbackInfoReturnable<Integer> cir) {
        if (!PlayerGridInventoryAccess.isEnabled(inventory.player)
                || inventory.findSlotMatchingUnusedItem(target) != -1) {
            return;
        }
        ItemStack extracted = PlayerGridInventoryAccess.extractPlayerGridMatching(inventory.player, target, amount);
        if (extracted.isEmpty()) {
            return;
        }
        if (slot.getItem().isEmpty()) {
            slot.set(extracted.copy());
        } else {
            slot.getItem().grow(extracted.getCount());
        }
        cir.setReturnValue(amount - extracted.getCount());
    }

    @Inject(method = "testClearGrid", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$allowCombinedInventoryClear(CallbackInfoReturnable<Boolean> cir) {
        if (PlayerGridInventoryAccess.canReturnCraftingItems(inventory, df_grid_inventory$craftingContents())) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(
            method = "clearGrid",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V"
            )
    )
    private void df_grid_inventory$returnCraftingItem(
            Inventory inventory, ItemStack stack, boolean sendPacket) {
        PlayerGridInventoryAccess.returnCraftingItem(inventory, stack);
    }

    @Unique
    private List<ItemStack> df_grid_inventory$craftingContents() {
        List<ItemStack> stacks = new ArrayList<>();
        int size = menu.getGridWidth() * menu.getGridHeight() + 1;
        for (int index = 0; index < size; index++) {
            if (index != menu.getResultSlotIndex()) {
                ItemStack stack = menu.getSlot(index).getItem();
                if (!stack.isEmpty()) {
                    stacks.add(stack.copy());
                }
            }
        }
        return stacks;
    }
}
