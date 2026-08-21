package com.dreamingfish.gridinventory.target.forge1201.mixin;

import com.dreamingfish.gridinventory.common.inventory.compat.PlayerGridInventoryAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryStackedContentsMixin {
    @Inject(method = "fillStackedContents", at = @At("TAIL"))
    private void df_grid_inventory$accountPlayerGrid(StackedContents contents, CallbackInfo ci) {
        Inventory inventory = (Inventory) (Object) this;
        PlayerGridInventoryAccess.accountPlayerGrid(inventory.player, contents);
    }
}
