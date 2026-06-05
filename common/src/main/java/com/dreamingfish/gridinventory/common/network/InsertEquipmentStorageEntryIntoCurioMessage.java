package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record InsertEquipmentStorageEntryIntoCurioMessage(EquipmentSlot sourceSlot, String containerId, UUID entryId,
                                                         String identifier, int index) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(sourceSlot);
        buf.writeUtf(containerId);
        buf.writeUUID(entryId);
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
    }

    public static InsertEquipmentStorageEntryIntoCurioMessage decode(FriendlyByteBuf buf) {
        return new InsertEquipmentStorageEntryIntoCurioMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(), buf.readUtf(), buf.readVarInt());
    }

    public static void handle(InsertEquipmentStorageEntryIntoCurioMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertEquipmentStorageEntryIntoCurio(packet.sourceSlot(), packet.containerId(), packet.entryId(), packet.identifier(), packet.index());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_EQUIPMENT_STORAGE_ENTRY_INTO_CURIO;
    }
}
