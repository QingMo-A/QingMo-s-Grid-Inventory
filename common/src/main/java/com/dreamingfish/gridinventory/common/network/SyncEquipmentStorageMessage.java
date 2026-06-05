package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record SyncEquipmentStorageMessage(EquipmentSlot slot, EquipmentStorageData storage) implements GridMessage {
        
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(slot);
        EquipmentStorageData.STREAM_CODEC.encode(buf, storage);
    }

    public static SyncEquipmentStorageMessage decode(RegistryFriendlyByteBuf buf) {
        return new SyncEquipmentStorageMessage(buf.readEnum(EquipmentSlot.class), EquipmentStorageData.STREAM_CODEC.decode(buf));
    }

    public static void handle(SyncEquipmentStorageMessage packet, GridMessageContext context) {
        ItemStack stack = packet.slot() == EquipmentSlot.BODY
                ? CuriosIntegration.getCurioStack(context.player(), "back", 0).orElse(ItemStack.EMPTY)
                : context.player().getItemBySlot(packet.slot());
        if (stack.isEmpty()) {
            return;
        }
        com.dreamingfish.gridinventory.platform.GridInventoryServices.itemStackData().setEquipmentStorage(stack, packet.storage());
        if (packet.slot() == EquipmentSlot.BODY) {
            CuriosIntegration.setCurioStack(context.player(), "back", 0, stack);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_EQUIPMENT_STORAGE;
    }
}
