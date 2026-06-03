package com.dreamingfish.gridinventory.client;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.client.key.ModKeyMappings;
import com.dreamingfish.gridinventory.client.pickup.ClientItemTargeting;
import com.dreamingfish.gridinventory.client.pickup.ClientPickupController;
import com.dreamingfish.gridinventory.client.render.ItemEntityHighlightRenderer;
import com.dreamingfish.gridinventory.client.render.PickupPromptHud;
import com.dreamingfish.gridinventory.client.screen.GridInventoryScreen;
import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import com.dreamingfish.gridinventory.common.item.GridBackpackItem;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.common.registry.ModItems;
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
        ItemProperties.register(ModItems.GRAY_FIELD_BACKPACK.get(),
                ResourceLocation.fromNamespaceAndPath(DFGridInventoryMod.MODID, "folded"),
                (stack, level, entity, seed) -> GridBackpackItem.isFolded(stack) ? 1.0F : 0.0F);
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
        return CuriosIntegration.canQuickEquip(player, stack);
    }
}
