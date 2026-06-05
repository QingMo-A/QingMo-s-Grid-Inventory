package com.dreamingfish.gridinventory.common.item;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class SmallGridBagItem extends Item {
    public SmallGridBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ensureData(stack);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : 40;
            GridInventoryServices.menus().openGridInventory(serverPlayer, slot, hand, false, getData(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GridInventoryData data = stack.get(ModDataComponents.GRID_INVENTORY.get());
        if (data != null) {
            tooltip.add(Component.literal(data.getColumns() + " x " + data.getRows()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static GridInventoryData getData(ItemStack stack) {
        GridInventoryData data = stack.get(ModDataComponents.GRID_INVENTORY.get());
        if (data == null) {
            data = createDefault();
            stack.set(ModDataComponents.GRID_INVENTORY.get(), data);
        }
        return data.copy();
    }

    public static void setData(ItemStack stack, GridInventoryData data) {
        stack.set(ModDataComponents.GRID_INVENTORY.get(), data.copy());
    }

    public static void ensureData(ItemStack stack) {
        if (!stack.has(ModDataComponents.GRID_INVENTORY.get())) {
            stack.set(ModDataComponents.GRID_INVENTORY.get(), createDefault());
        }
    }

    public static GridInventoryData createDefault() {
        return new GridInventoryData(GridInventoryServices.config().smallGridBagColumns(), GridInventoryServices.config().smallGridBagRows());
    }
}
