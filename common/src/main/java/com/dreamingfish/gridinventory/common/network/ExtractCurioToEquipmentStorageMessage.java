package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

public record ExtractCurioToEquipmentStorageMessage(String identifier, int index, EquipmentSlot equipmentSlot,
                                                   String containerId, int targetX, int targetY,
                                                   boolean rotated, boolean targetFolded) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeVarInt(index);
        buf.writeEnum(equipmentSlot);
        buf.writeUtf(containerId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
        buf.writeBoolean(targetFolded);
    }

    public static ExtractCurioToEquipmentStorageMessage decode(FriendlyByteBuf buf) {
        return new ExtractCurioToEquipmentStorageMessage(buf.readUtf(), buf.readVarInt(), buf.readEnum(EquipmentSlot.class),
                buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(ExtractCurioToEquipmentStorageMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.insertCurioIntoEquipmentStorage(packet.identifier(), packet.index(), packet.equipmentSlot(), packet.containerId(),
                    packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_CURIO_TO_EQUIPMENT_STORAGE;
    }
}
