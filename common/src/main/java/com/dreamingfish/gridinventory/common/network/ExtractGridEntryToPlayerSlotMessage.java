package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record ExtractGridEntryToPlayerSlotMessage(UUID entryId, int playerSlot, int amount) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(playerSlot);
        buf.writeVarInt(amount);
    }

    public static ExtractGridEntryToPlayerSlotMessage decode(FriendlyByteBuf buf) {
        return new ExtractGridEntryToPlayerSlotMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExtractGridEntryToPlayerSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractToPlayerSlot(packet.entryId(), packet.playerSlot(), packet.amount());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_GRID_ENTRY_TO_PLAYER_SLOT;
    }
}
