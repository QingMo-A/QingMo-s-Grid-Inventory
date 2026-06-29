package com.dreamingfish.gridinventory.target.neoforge1211.client;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.client.GridInventoryClientLogic;
import com.dreamingfish.gridinventory.client.compat.rarity.GridItemRarityServices;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.platform.GridInventoryClientServices;
import com.dreamingfish.gridinventory.client.render.ItemEntityHighlightRenderer;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.client.screen.SearchableGridContainerScreen;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.target.neoforge1211.client.compat.rarity.NeoForge1211RarityCoreCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = DFGridInventoryMod.MODID, value = Dist.CLIENT)
public final class NeoForge1211ClientEvents {
    private NeoForge1211ClientEvents() {
    }

    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.GRID_INVENTORY.get(), GridInventoryScreen::new);
        event.<GridInventoryMenu, SearchableGridContainerScreen>register(
                ModMenus.SEARCHABLE_GRID_CONTAINER.get(), SearchableGridContainerScreen::new);
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.PICKUP_ITEM);
        event.register(ModKeyMappings.ROTATE_GRID_ITEM);
        event.register(ModKeyMappings.DROP_HOVERED_GRID_ITEM);
        event.register(ModKeyMappings.TOGGLE_BACKPACK_FOLD);
    }

    public static void registerItemProperties() {
        GridInventoryClientServices.init(new NeoForge1211ClientBridge());
        GridItemRarityServices.init(new NeoForge1211RarityCoreCompat());
        ResourceLocation folded = ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "folded");
        ResourceLocation foldedRoll = ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "folded_roll");
        ItemProperties.register(ModItems.GRAY_FIELD_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LEATHER_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LEATHER_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> GridInventoryClientLogic.isFoldedRoll(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LIME_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LIME_HIKING_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> GridInventoryClientLogic.isFoldedRoll(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.MEDIUM_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.MEDIUM_HIKING_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> GridInventoryClientLogic.isFoldedRoll(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.MILITARY_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.TACTICAL_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        GridInventoryClientLogic.onClientTick();
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        PickupPromptHud.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        ItemEntityHighlightRenderer.render();
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        GridInventoryClientLogic.addQuickEquipTooltip(minecraft.player, event.getItemStack(), event.getToolTip());
    }
}
