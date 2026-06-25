package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;

public final class GridItemMoveService {
    private GridItemMoveService() {
    }

    public static boolean move(GridInventoryMenu menu, GridItemSource source, GridItemTarget target,
                               GridMoveOptions options) {
        GridMoveOptions safeOptions = options == null ? GridMoveOptions.all(false, false) : options;
        boolean moved = GridItemTransferService.transfer(menu, source, target);
        DFGridInventory.LOGGER.debug("Unified grid move source={} target={} options={} result={}",
                source.getClass().getSimpleName(), target.getClass().getSimpleName(), safeOptions, moved);
        return moved;
    }
}
