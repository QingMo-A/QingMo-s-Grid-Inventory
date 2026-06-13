package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import com.dreamingfish.gridinventory.common.network.OpenPlayerGridInventoryMessage;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
            at = @At("HEAD"),
            cancellable = true
    )
    private void df_grid_inventory$openGridInventoryInstead(CallbackInfo ci) {
        if (player == null || gameMode == null) {
            return;
        }
        if (player.isCreative() || player.isSpectator() || gameMode.isServerControlledInventory()) {
            return;
        }
        if (!GridInventoryServices.config().enableGridInventory()
                || !GridInventoryServices.config().replaceSurvivalInventory()
                || !GridInventoryServices.clientConfig().enableSurvivalInventoryGridUi()) {
            return;
        }

        boolean consumed = false;
        while (options.keyInventory.consumeClick()) {
            consumed = true;
            GridInventoryServices.network().sendToServer(OpenPlayerGridInventoryMessage.INSTANCE);
        }

        if (consumed) {
            ci.cancel();
        }
    }
}
