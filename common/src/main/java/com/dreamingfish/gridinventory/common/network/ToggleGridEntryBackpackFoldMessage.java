package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record ToggleGridEntryBackpackFoldMessage(UUID entryId, int targetX, int targetY, boolean rotated) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeVarInt(targetX);
        buf.writeVarInt(targetY);
        buf.writeBoolean(rotated);
    }

    public static ToggleGridEntryBackpackFoldMessage decode(FriendlyByteBuf buf) {
        return new ToggleGridEntryBackpackFoldMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(ToggleGridEntryBackpackFoldMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.toggleGridEntryBackpackFold(packet.entryId(), packet.targetX(), packet.targetY(), packet.rotated());
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TOGGLE_GRID_ENTRY_BACKPACK_FOLD;
    }
}
