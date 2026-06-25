package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class GridMoveMessageGuards {
    private GridMoveMessageGuards() {
    }

    public static boolean withGridMenu(GridMessageContext context, String messageName,
                                       Consumer<GridInventoryMenu> action) {
        return withGridMenu(context, messageName, (player, menu) -> action.accept(menu));
    }

    public static boolean withGridMenu(GridMessageContext context, String messageName,
                                       BiConsumer<ServerPlayer, GridInventoryMenu> action) {
        if (!(context.player() instanceof ServerPlayer player)) {
            logRejected(messageName, "unknown", "not-server-player");
            return false;
        }
        if (player.isSpectator()) {
            logRejected(messageName, player.getGameProfile().getName(), "spectator");
            return false;
        }
        if (!(player.containerMenu instanceof GridInventoryMenu menu)) {
            logRejected(messageName, player.getGameProfile().getName(), "wrong-menu");
            return false;
        }
        action.accept(player, menu);
        return true;
    }

    private static void logRejected(String messageName, String playerName, String reason) {
        DFGridInventory.LOGGER.debug("Grid move message rejected message={} player={} reason={}",
                messageName, playerName, reason);
    }
}
