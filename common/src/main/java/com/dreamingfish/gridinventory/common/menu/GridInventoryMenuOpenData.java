package com.dreamingfish.gridinventory.common.menu;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

public record GridInventoryMenuOpenData(int sourceSlot, InteractionHand hand, boolean playerInventory, GridInventoryData data) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GridInventoryMenuOpenData> STREAM_CODEC = StreamCodec.ofMember(GridInventoryMenuOpenData::encode, GridInventoryMenuOpenData::decode);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sourceSlot);
        buf.writeEnum(hand);
        buf.writeBoolean(playerInventory);
        data.encode(buf);
    }

    public static GridInventoryMenuOpenData decode(RegistryFriendlyByteBuf buf) {
        int sourceSlot = buf.readVarInt();
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        boolean playerInventory = buf.readBoolean();
        GridInventoryData data = GridInventoryData.decode(buf);
        return new GridInventoryMenuOpenData(sourceSlot, hand, playerInventory, data);
    }
}
