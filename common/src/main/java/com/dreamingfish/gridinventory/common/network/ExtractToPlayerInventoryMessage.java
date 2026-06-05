package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record ExtractToPlayerInventoryMessage(UUID entryId, int amount) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(amount);
    }

    public static ExtractToPlayerInventoryMessage decode(FriendlyByteBuf buf) {
        return new ExtractToPlayerInventoryMessage(buf.readUUID(), buf.readVarInt());
    }

    public static void handle(ExtractToPlayerInventoryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.extractToPlayerInventory(packet.entryId(), packet.amount());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_TO_PLAYER_INVENTORY;
    }
}
