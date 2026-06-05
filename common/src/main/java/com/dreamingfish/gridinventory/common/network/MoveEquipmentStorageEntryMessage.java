package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record MoveEquipmentStorageEntryMessage(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    public static MoveEquipmentStorageEntryMessage decode(FriendlyByteBuf buf) {
        return new MoveEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(MoveEquipmentStorageEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.moveEquipmentEntry(packet.equipmentSlot(), packet.containerId(), packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_EQUIPMENT_STORAGE_ENTRY;
    }
}
