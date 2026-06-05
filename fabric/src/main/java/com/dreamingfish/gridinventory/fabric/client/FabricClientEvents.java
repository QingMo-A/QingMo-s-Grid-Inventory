package com.dreamingfish.gridinventory.fabric.client;

import com.dreamingfish.gridinventory.client.GridInventoryClientLogic;
import com.dreamingfish.gridinventory.client.render.ItemEntityHighlightRenderer;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;

public final class FabricClientEvents {
    private FabricClientEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> GridInventoryClientLogic.onClientTick());
        HudRenderCallback.EVENT.register((graphics, tickCounter) -> PickupPromptHud.render(graphics));
        WorldRenderEvents.AFTER_ENTITIES.register(context -> ItemEntityHighlightRenderer.render());
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) ->
                GridInventoryClientLogic.addQuickEquipTooltip(Minecraft.getInstance().player, stack, lines));
    }
}
