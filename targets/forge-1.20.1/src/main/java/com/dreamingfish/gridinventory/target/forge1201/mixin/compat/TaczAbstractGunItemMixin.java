package com.dreamingfish.gridinventory.target.forge1201.mixin.compat;

import com.dreamingfish.gridinventory.common.inventory.compat.GridItemHandlerMutationCommitter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.tacz.guns.api.item.gun.AbstractGunItem", remap = false)
public abstract class TaczAbstractGunItemMixin {
    @Inject(
            method = "findAndExtractInventoryAmmo",
            at = @At("RETURN"),
            require = 0,
            remap = false
    )
    private void df_grid_inventory$commitAmmoBoxMutation(
            IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount,
            CallbackInfoReturnable<Integer> cir) {
        if (itemHandler instanceof GridItemHandlerMutationCommitter committer) {
            committer.commitExternalMutations();
        }
    }
}
