package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.equipment.GridEquipmentSlots;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record SyncEquipmentStorageMessage(EquipmentSlot slot, EquipmentStorageData storage) implements GridMessage {

    public static void handle(SyncEquipmentStorageMessage packet, GridMessageContext context) {
        ItemStack stack = GridEquipmentSlots.isBack(packet.slot())
                ? GridInventoryServices.accessories().getAccessoryStack(context.player(), "back", 0).orElse(ItemStack.EMPTY)
                : context.player().getItemBySlot(packet.slot());
        if (stack.isEmpty()) {
            return;
        }
        com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setEquipmentStorage(stack, packet.storage());
        if (GridEquipmentSlots.isBack(packet.slot())) {
            GridInventoryServices.accessories().setAccessoryStack(context.player(), "back", 0, stack);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_EQUIPMENT_STORAGE;
    }
}
