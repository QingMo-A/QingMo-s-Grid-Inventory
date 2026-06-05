package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record SyncBackpackFoldingRulesMessage(List<BackpackFoldingDefinition> rules) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(rules.size());
        for (BackpackFoldingDefinition rule : rules) {
            rule.encode(buf);
        }
    }

    public static SyncBackpackFoldingRulesMessage decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<BackpackFoldingDefinition> rules = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            rules.add(BackpackFoldingDefinition.decode(buf));
        }
        return new SyncBackpackFoldingRulesMessage(rules);
    }

    public static void handle(SyncBackpackFoldingRulesMessage packet, GridMessageContext context) {
        context.enqueueWork(() -> BackpackFoldingManager.replaceClientRules(packet.rules()));
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_BACKPACK_FOLDING_RULES;
    }
}
