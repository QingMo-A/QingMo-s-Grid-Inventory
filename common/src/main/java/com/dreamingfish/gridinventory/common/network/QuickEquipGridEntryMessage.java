package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record QuickEquipGridEntryMessage(UUID entryId) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
    }

    public static QuickEquipGridEntryMessage decode(FriendlyByteBuf buf) {
        return new QuickEquipGridEntryMessage(buf.readUUID());
    }

    public static void handle(QuickEquipGridEntryMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipGridEntry(packet.entryId());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.QUICK_EQUIP_GRID_ENTRY;
    }
}
