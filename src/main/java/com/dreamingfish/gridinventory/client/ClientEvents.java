package com.dreamingfish.gridinventory.client;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.pickup.ClientItemTargeting;
import com.dreamingfish.gridinventory.client.pickup.ClientPickupController;
import com.dreamingfish.gridinventory.client.render.ItemEntityHighlightRenderer;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = DFGridInventoryMod.MODID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GRID_INVENTORY.get(), GridInventoryScreen::new);
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.PICKUP_ITEM);
        event.register(ModKeyMappings.ROTATE_GRID_ITEM);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientItemTargeting.tick();
        PickupPromptHud.tick();
        ClientPickupController.tick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        PickupPromptHud.render(event);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        ItemEntityHighlightRenderer.render(event);
    }
}
