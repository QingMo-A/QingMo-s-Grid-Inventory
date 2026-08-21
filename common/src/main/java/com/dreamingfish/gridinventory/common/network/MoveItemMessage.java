package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.ExternalContainerTransferService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;

public record MoveItemMessage(GridItemSource source, GridItemTarget target, GridMoveOptions options) implements GridMessage {
    public static void handle(MoveItemMessage packet, GridMessageContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        if (player.containerMenu instanceof GridInventoryMenu menu) {
            boolean moved = GridItemMoveService.move(menu, packet.source(), packet.target(), packet.options());
            log(packet, player.getGameProfile().getName(), menu.containerId, moved ? "success" : "move-failed", moved);
            if (moved) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
            return;
        }
        if (!GridInventoryServices.config().enableGridInventory()
                || !GridInventoryServices.config().replaceSurvivalInventory()
                || !ExternalContainerTransferService.supports(packet.source(), packet.target())) {
            log(packet, player.getGameProfile().getName(), player.containerMenu.containerId,
                    "unsupported-external-move", false);
            return;
        }
        boolean moved = ExternalContainerTransferService.move(player, player.containerMenu,
                packet.source(), packet.target(), packet.options());
        log(packet, player.getGameProfile().getName(), player.containerMenu.containerId,
                moved ? "external-success" : "external-move-failed", moved);
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
