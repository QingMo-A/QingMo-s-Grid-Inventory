package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

public record MoveItemMessage(GridItemSource source, GridItemTarget target, GridMoveOptions options) implements GridMessage {
    public static void handle(MoveItemMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "MoveItemMessage", (player, menu) -> {
            boolean moved = GridItemMoveService.move(menu, packet.source(), packet.target(), packet.options());
            log(packet, player.getGameProfile().getName(), menu.containerId, moved ? "success" : "move-failed", moved);
            if (moved) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_ITEM;
    }

    private static void log(MoveItemMessage packet, String playerName, int containerId, String reason, boolean result) {
        DFGridInventory.LOGGER.debug("MoveItemMessage player={} container={} source={} target={} options={} result={} reason={}",
                playerName, containerId,
                packet.source().getClass().getSimpleName(), packet.target().getClass().getSimpleName(),
                packet.options(), result, reason);
    }
}
