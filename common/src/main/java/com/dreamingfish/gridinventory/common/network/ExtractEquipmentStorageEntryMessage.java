package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record ExtractEquipmentStorageEntryMessage(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int playerSlot, int amount) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeVarInt(playerSlot);
        buf.writeVarInt(amount);
    }

    public static ExtractEquipmentStorageEntryMessage decode(FriendlyByteBuf buf) {
        return new ExtractEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExtractEquipmentStorageEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractEquipmentEntryToPlayerSlot(packet.equipmentSlot(), packet.containerId(), packet.entryId(), packet.playerSlot(), packet.amount());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_EQUIPMENT_STORAGE_ENTRY;
    }
}
