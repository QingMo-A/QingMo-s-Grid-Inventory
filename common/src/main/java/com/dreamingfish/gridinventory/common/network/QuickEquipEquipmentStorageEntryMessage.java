package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record QuickEquipEquipmentStorageEntryMessage(EquipmentSlot sourceSlot, String containerId, UUID entryId) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(sourceSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
    }

    public static QuickEquipEquipmentStorageEntryMessage decode(FriendlyByteBuf buf) {
        return new QuickEquipEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID());
    }

    public static void handle(QuickEquipEquipmentStorageEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipEquipmentStorageEntry(packet.sourceSlot(), packet.containerId(), packet.entryId());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.QUICK_EQUIP_EQUIPMENT_STORAGE_ENTRY;
    }
}
