package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionManager;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.server.level.ServerPlayer;

public record RequestExternalPlayerGridMessage() implements GridMessage {
    public static final RequestExternalPlayerGridMessage INSTANCE = new RequestExternalPlayerGridMessage();

    public static void handle(RequestExternalPlayerGridMessage packet, GridMessageContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()
                || !GridInventoryServices.config().enableGridInventory()
                || !GridInventoryServices.config().replaceSurvivalInventory()) {
            return;
        }
        GridInventoryData data = PlayerPocketDefinitionManager.refreshShape(
                GridInventoryServices.playerData().getPlayerGridInventory(player)).copy();
        GridInventoryServices.playerData().setPlayerGridInventory(player, data.copy());
        GridInventoryServices.network().sendToPlayer(player, new SyncExternalPlayerGridMessage(data));
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.REQUEST_EXTERNAL_PLAYER_GRID;
    }
}
