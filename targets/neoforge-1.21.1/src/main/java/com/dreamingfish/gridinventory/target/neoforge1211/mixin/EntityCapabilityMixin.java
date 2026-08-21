package com.dreamingfish.gridinventory.target.neoforge1211.mixin;

import com.dreamingfish.gridinventory.common.inventory.compat.PlayerGridInventoryAccess;
import com.dreamingfish.gridinventory.target.neoforge1211.compat.inventory.NeoForge1211PlayerInventoryCompatHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityCapability.class, remap = false)
public abstract class EntityCapabilityMixin {
    @Inject(method = "getCapability", at = @At("RETURN"), cancellable = true, remap = false)
    private void df_grid_inventory$appendGridItemHandler(
            Entity entity, Object context, CallbackInfoReturnable<Object> cir) {
        Object original = cir.getReturnValue();
        if ((Object) this != Capabilities.ItemHandler.ENTITY
                || !(entity instanceof Player player)
                || !player.isAlive()
                || !(original instanceof IItemHandler itemHandler)
                || itemHandler instanceof NeoForge1211PlayerInventoryCompatHandler
                || !PlayerGridInventoryAccess.isEnabled(player)) {
            return;
        }
        cir.setReturnValue(new NeoForge1211PlayerInventoryCompatHandler(player, itemHandler));
    }
}
