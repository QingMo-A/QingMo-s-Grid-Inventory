package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.size.GridItemSizeManager;

import java.util.ArrayList;
import java.util.List;

public record SyncItemSizeRulesMessage(List<GridItemSizeRule> rules) implements GridMessage {

    public static void handle(SyncItemSizeRulesMessage packet, GridMessageContext context) {
        GridItemSizeManager.replaceClientRules(packet.rules());
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_ITEM_SIZE_RULES;
    }
}
