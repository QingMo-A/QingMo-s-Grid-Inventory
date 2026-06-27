package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public final class GridItemMoveService {
    private GridItemMoveService() {
    }

    public static boolean move(GridInventoryMenu menu, GridItemSource source, GridItemTarget target,
                               GridMoveOptions options) {
        GridMoveOptions safeOptions = options == null ? GridMoveOptions.all(false, false) : options;
        boolean moved = GridItemTransferService.transfer(menu, source, target, safeOptions);
        DFGridInventory.LOGGER.debug("Unified grid move container={} source={} target={} options={} result={}",
                menu.containerId, source, target, safeOptions, moved);
        return moved;
    }
}
