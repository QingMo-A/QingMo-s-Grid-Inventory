package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

public record InsertIntoEquipmentStorageMessage(int playerSlot, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(playerSlot);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    public static InsertIntoEquipmentStorageMessage decode(FriendlyByteBuf buf) {
        return new InsertIntoEquipmentStorageMessage(buf.readVarInt(), buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(InsertIntoEquipmentStorageMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertFromPlayerIntoEquipmentStorage(packet.playerSlot(), packet.equipmentSlot(), packet.containerId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_INTO_EQUIPMENT_STORAGE;
    }
}
