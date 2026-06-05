package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.common.inventory.PlayerGridInventoryOpener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record OpenPlayerGridInventoryMessage() implements GridMessage {
    public static final OpenPlayerGridInventoryMessage INSTANCE = new OpenPlayerGridInventoryMessage();
        
    public static void handle(OpenPlayerGridInventoryMessage packet, GridMessageContext context) {
        if (GridInventoryServices.config().replaceSurvivalInventory() && context.player() instanceof ServerPlayer player && !player.isCreative()) {
            PlayerGridInventoryOpener.open(player);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.OPEN_PLAYER_GRID_INVENTORY;
    }
}
