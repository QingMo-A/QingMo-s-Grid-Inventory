package com.dreamingfish.gridinventory.common.item;

import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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
            serverPlayer.openMenu(new Provider(slot, hand), buf -> {
                buf.writeVarInt(slot);
                buf.writeEnum(hand);
                buf.writeBoolean(false);
                getData(stack).encode(buf);
            });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        GridInventoryData data = stack.get(ModDataComponents.GRID_INVENTORY);
        if (data != null) {
            tooltip.add(Component.literal(data.getColumns() + " x " + data.getRows()).withStyle(ChatFormatting.GRAY));
        }
    }

    public static GridInventoryData getData(ItemStack stack) {
        GridInventoryData data = stack.get(ModDataComponents.GRID_INVENTORY);
        if (data == null) {
            data = createDefault();
            stack.set(ModDataComponents.GRID_INVENTORY, data);
        }
        return data.copy();
    }

    public static void setData(ItemStack stack, GridInventoryData data) {
        stack.set(ModDataComponents.GRID_INVENTORY, data.copy());
    }

    public static void ensureData(ItemStack stack) {
        if (!stack.has(ModDataComponents.GRID_INVENTORY)) {
            stack.set(ModDataComponents.GRID_INVENTORY, createDefault());
        }
    }

    public static GridInventoryData createDefault() {
        return new GridInventoryData(GridInventoryConfig.SMALL_GRID_BAG_COLUMNS.get(), GridInventoryConfig.SMALL_GRID_BAG_ROWS.get());
    }

    private record Provider(int slot, InteractionHand hand) implements MenuProvider {
        @Override
        public Component getDisplayName() {
            return Component.translatable("container.df_grid_inventory.small_grid_bag");
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
            return new GridInventoryMenu(containerId, playerInventory, slot, hand, SmallGridBagItem.getData(playerInventory.player.getItemInHand(hand)));
        }

        @Override
        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buffer) {
        }
    }
}
