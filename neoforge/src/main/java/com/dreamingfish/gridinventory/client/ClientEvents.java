package com.dreamingfish.gridinventory.client;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.pickup.ClientItemTargeting;
import com.dreamingfish.gridinventory.client.pickup.ClientPickupController;
import com.dreamingfish.gridinventory.client.render.ItemEntityHighlightRenderer;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

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
        event.register(ModKeyMappings.DROP_HOVERED_GRID_ITEM);
        event.register(ModKeyMappings.TOGGLE_BACKPACK_FOLD);
    }

    public static void registerItemProperties() {
        ResourceLocation folded = ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "folded");
        ResourceLocation foldedRoll = ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "folded_roll");
        ItemProperties.register(ModItems.GRAY_FIELD_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LEATHER_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LEATHER_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> isFoldedRoll(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LIME_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.LIME_HIKING_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> isFoldedRoll(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.MEDIUM_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.MEDIUM_HIKING_BACKPACK.get(), foldedRoll,
                (stack, level, entity, seed) -> isFoldedRoll(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.MILITARY_HIKING_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.TACTICAL_BACKPACK.get(), folded,
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
    }

    private static boolean isFoldedRoll(ItemStack stack) {
        return GridBackpackItem.isFolded(stack) && GridBackpackItem.usesRollFoldedModel(stack);
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

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !canQuickEquip(minecraft.player, event.getItemStack())) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.df_grid_inventory.right_click_equip"));
    }

    private static boolean canQuickEquip(net.minecraft.world.entity.player.Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(slot).isEmpty() && stack.canEquip(slot, player)) {
                return true;
            }
        }
        return GridInventoryServices.accessories().canQuickEquip(player, stack);
    }
}
