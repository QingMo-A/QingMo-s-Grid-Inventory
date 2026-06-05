package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public record SyncItemSizeRulesMessage(List<GridItemSizeRule> rules) implements GridMessage {
        
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(rules.size());
        for (GridItemSizeRule rule : rules) {
            rule.encode(buf);
        }
    }

    public static SyncItemSizeRulesMessage decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<GridItemSizeRule> rules = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rules.add(GridItemSizeRule.decode(buf));
        }
        return new SyncItemSizeRulesMessage(rules);
    }

    public static void handle(SyncItemSizeRulesMessage packet, GridMessageContext context) {
        GridItemSizeManager.replaceClientRules(packet.rules());
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_ITEM_SIZE_RULES;
    }
}
