package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record TransferEquipmentStorageEntryMessage(EquipmentSlot sourceSlot, String sourceContainerId, UUID entryId,
                                                  EquipmentSlot targetSlot, String targetContainerId,
                                                  int targetX, int targetY, boolean rotated) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(sourceSlot);
        buf.writeUtf(sourceContainerId);
        buf.writeUUID(entryId);
        buf.writeEnum(targetSlot);
        buf.writeUtf(targetContainerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    public static TransferEquipmentStorageEntryMessage decode(FriendlyByteBuf buf) {
        return new TransferEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(TransferEquipmentStorageEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.transferEquipmentEntryBetweenStorages(packet.sourceSlot(), packet.sourceContainerId(), packet.entryId(),
                    packet.targetSlot(), packet.targetContainerId(), packet.targetX(), packet.targetY(), packet.rotated());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY;
    }
}
