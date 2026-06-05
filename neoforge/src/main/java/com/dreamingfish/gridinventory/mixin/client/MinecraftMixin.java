package com.dreamingfish.gridinventory.mixin.client;

import com.dreamingfish.gridinventory.client.pickup.ClientItemTargeting;
import com.dreamingfish.gridinventory.common.network.OpenPlayerGridInventoryPacket;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public Options options;

    @Shadow
    public LocalPlayer player;

    @Shadow
    public MultiPlayerGameMode gameMode;

    @Inject(
            method = "handleKeybinds",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;keyInventory:Lnet/minecraft/client/KeyMapping;", ordinal = 0),
            cancellable = true
    )
    private void df_grid_inventory$openGridInventoryInstead(CallbackInfo ci) {
        if (player == null || gameMode == null || player.isCreative() || gameMode.isServerControlledInventory()) {
            return;
        }

        boolean consumed = false;
        while (options.keyInventory.consumeClick()) {
            consumed = true;
            GridInventoryServices.network().sendToServer(OpenPlayerGridInventoryPacket.INSTANCE);
        }

        if (consumed) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void df_grid_inventory$highlightTargetedDroppedItem(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (GridInventoryServices.clientConfig().highlightTargetItem()
                && entity instanceof ItemEntity
                && entity.getId() == ClientItemTargeting.currentTargetItemEntityId()) {
            cir.setReturnValue(true);
        }
    }
}
