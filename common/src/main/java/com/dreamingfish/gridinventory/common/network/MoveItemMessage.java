package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.server.level.ServerPlayer;

public record MoveItemMessage(GridItemSource source, GridItemTarget target, GridMoveOptions options) implements GridMessage {
    public static void handle(MoveItemMessage packet, GridMessageContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            log(packet, "not-server-player", false);
            return;
        }
        if (player.isSpectator()) {
            log(packet, "spectator", false);
            return;
        }
        if (!(player.containerMenu instanceof GridInventoryMenu menu)) {
            log(packet, "wrong-menu", false);
            return;
        }
        boolean moved = GridItemMoveService.move(menu, packet.source(), packet.target(), packet.options());
        log(packet, moved ? "success" : "move-failed", moved);
        if (moved) {
            menu.broadcastChanges();
            ModNetworking.syncMenu(player, menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.MOVE_ITEM;
    }

    private static void log(MoveItemMessage packet, String reason, boolean result) {
        DFGridInventory.LOGGER.debug("MoveItemMessage source={} target={} options={} result={} reason={}",
                packet.source().getClass().getSimpleName(), packet.target().getClass().getSimpleName(),
                packet.options(), result, reason);
    }
}
