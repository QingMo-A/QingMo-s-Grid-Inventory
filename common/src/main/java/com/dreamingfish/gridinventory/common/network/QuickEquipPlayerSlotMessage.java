package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;

public record QuickEquipPlayerSlotMessage(int playerSlot) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(playerSlot);
    }

    public static QuickEquipPlayerSlotMessage decode(FriendlyByteBuf buf) {
        return new QuickEquipPlayerSlotMessage(buf.readVarInt());
    }

    public static void handle(QuickEquipPlayerSlotMessage packet, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipPlayerSlot(packet.playerSlot());
            menu.broadcastChanges();
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.QUICK_EQUIP_PLAYER_SLOT;
    }
}
