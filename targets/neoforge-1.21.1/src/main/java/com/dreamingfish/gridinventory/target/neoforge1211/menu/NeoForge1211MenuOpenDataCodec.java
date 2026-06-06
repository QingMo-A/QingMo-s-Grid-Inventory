package com.dreamingfish.gridinventory.target.neoforge1211.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenuOpenData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;

public final class NeoForge1211MenuOpenDataCodec {
    private NeoForge1211MenuOpenDataCodec() {
    }

    public static void encode(GridInventoryMenuOpenData data, RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(data.sourceSlot());
        buf.writeEnum(data.hand());
        buf.writeBoolean(data.playerInventory());
        data.data().encode(buf);
    }

    public static GridInventoryMenuOpenData decode(RegistryFriendlyByteBuf buf) {
        int sourceSlot = buf.readVarInt();
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        boolean playerInventory = buf.readBoolean();
        GridInventoryData data = GridInventoryData.decode(buf);
        return new GridInventoryMenuOpenData(sourceSlot, hand, playerInventory, data);
    }
}
