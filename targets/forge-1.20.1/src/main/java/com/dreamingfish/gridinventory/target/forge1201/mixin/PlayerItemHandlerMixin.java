package com.dreamingfish.gridinventory.target.forge1201.mixin;

import com.dreamingfish.gridinventory.common.inventory.compat.PlayerGridInventoryAccess;
import com.dreamingfish.gridinventory.target.forge1201.compat.inventory.Forge1201PlayerInventoryCompatHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerItemHandlerMixin {
    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true, remap = false)
    private <T> void df_grid_inventory$appendGridItemHandler(
            Capability<T> capability, Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        Player player = (Player) (Object) this;
        LazyOptional<T> original = cir.getReturnValue();
        if (capability != ForgeCapabilities.ITEM_HANDLER || side != null || !player.isAlive() || !original.isPresent()
                || !PlayerGridInventoryAccess.isEnabled(player)) {
            return;
        }
        LazyOptional<IItemHandler> wrapped = original.<IItemHandler>cast().lazyMap(handler ->
                handler instanceof Forge1201PlayerInventoryCompatHandler
                        ? handler
                        : new Forge1201PlayerInventoryCompatHandler(player, handler));
        cir.setReturnValue(wrapped.cast());
    }
}
