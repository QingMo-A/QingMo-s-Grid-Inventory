package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

import java.util.ArrayList;
import java.util.List;

public record SyncBackpackFoldingRulesPacket(List<BackpackFoldingDefinition> rules) implements CustomPacketPayload {
    public static final Type<SyncBackpackFoldingRulesPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "sync_backpack_folding_rules"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBackpackFoldingRulesPacket> STREAM_CODEC = StreamCodec.ofMember(SyncBackpackFoldingRulesPacket::encode, SyncBackpackFoldingRulesPacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(rules.size());
        for (BackpackFoldingDefinition rule : rules) {
            rule.encode(buf);
        }
    }

    private static SyncBackpackFoldingRulesPacket decode(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<BackpackFoldingDefinition> rules = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rules.add(BackpackFoldingDefinition.decode(buf));
        }
        return new SyncBackpackFoldingRulesPacket(rules);
    }

    public static void handle(SyncBackpackFoldingRulesPacket packet, GridPacketContext context) {
        context.enqueueWork(() -> BackpackFoldingManager.replaceClientRules(packet.rules()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
