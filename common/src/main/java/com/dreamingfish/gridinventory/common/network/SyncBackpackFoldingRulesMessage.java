package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingManager;

import java.util.ArrayList;
import java.util.List;

public record SyncBackpackFoldingRulesMessage(List<BackpackFoldingDefinition> rules) implements GridMessage {

    public static void handle(SyncBackpackFoldingRulesMessage packet, GridMessageContext context) {
        context.enqueueWork(() -> BackpackFoldingManager.replaceClientRules(packet.rules()));
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.SYNC_BACKPACK_FOLDING_RULES;
    }
}
