package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.ArrayList;
import java.util.List;

public record SyncItemSizeRulesPacket(List<GridItemSizeRule> rules) implements CustomPacketPayload {
    public static final Type<SyncItemSizeRulesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "sync_item_size_rules"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncItemSizeRulesPacket> STREAM_CODEC = StreamCodec.ofMember(SyncItemSizeRulesPacket::encode, SyncItemSizeRulesPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(rules.size());
        for (GridItemSizeRule rule : rules) {
            rule.encode(buf);
        }
    }

    private static SyncItemSizeRulesPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<GridItemSizeRule> rules = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rules.add(GridItemSizeRule.decode(buf));
        }
        return new SyncItemSizeRulesPacket(rules);
    }

    public static void handle(SyncItemSizeRulesPacket packet, GridPacketContext context) {
        GridItemSizeManager.replaceClientRules(packet.rules());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
