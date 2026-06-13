package com.dreamingfish.gridinventory.target.forge1201.client;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.client.GridInventoryClientLogic;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class Forge1201ClientEvents {
    private Forge1201ClientEvents() {
    }

    @Mod.EventBusSubscriber(modid = DFGridInventory.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ModKeyMappings.PICKUP_ITEM);
            event.register(ModKeyMappings.ROTATE_GRID_ITEM);
            event.register(ModKeyMappings.DROP_HOVERED_GRID_ITEM);
            event.register(ModKeyMappings.TOGGLE_BACKPACK_FOLD);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(ModMenus.GRID_INVENTORY.get(), GridInventoryScreen::new);
                Forge1201ClientEvents.registerItemProperties();
            });
        }
    }

    @Mod.EventBusSubscriber(modid = DFGridInventory.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeBus {
        private ForgeBus() {
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                GridInventoryClientLogic.onClientTick();
            }
        }

        @SubscribeEvent
        public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
            PickupPromptHud.render(event.getGuiGraphics());
        }

        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            GridInventoryClientLogic.addQuickEquipTooltip(minecraft.player, event.getItemStack(), event.getToolTip());
        }
    }

    private static void registerItemProperties() {
        ResourceLocation folded = new ResourceLocation(DFGridInventory.MODID, "folded");
        ResourceLocation foldedRoll = new ResourceLocation(DFGridInventory.MODID, "folded_roll");
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
}
