package com.dreamingfish.gridinventory.target.forge1201.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public final class Forge1201MenuOpenDataCodec {
    private Forge1201MenuOpenDataCodec() {
    }

    public static void encode(GridInventoryMenuOpenData data, FriendlyByteBuf buf) {
        buf.writeVarInt(data.sourceSlot());
        buf.writeEnum(data.hand());
        buf.writeBoolean(data.playerInventory());
        writeGridInventoryData(buf, data.data());
    }

    public static GridInventoryMenuOpenData decode(FriendlyByteBuf buf) {
        int sourceSlot = buf.readVarInt();
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        boolean playerInventory = buf.readBoolean();
        GridInventoryData data = readGridInventoryData(buf);
        return new GridInventoryMenuOpenData(sourceSlot, hand, playerInventory, data);
    }

    private static void writeGridInventoryData(FriendlyByteBuf buf, GridInventoryData data) {
        if (data == null) {
            throw new IllegalStateException("Cannot encode null Forge 1.20.1 grid inventory menu data");
        }
        data.encode(buf);
    }

    private static GridInventoryData readGridInventoryData(FriendlyByteBuf buf) {
        try {
            return GridInventoryData.decode(buf);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Failed to decode Forge 1.20.1 grid inventory menu data", exception);
        }
    }
}
