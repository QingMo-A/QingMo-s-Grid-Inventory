package com.dreamingfish.gridinventory.fabric.platform.player;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;

public interface FabricGridInventoryDataHolder {
    GridInventoryData df_grid_inventory$getGridInventoryData();

    void df_grid_inventory$setGridInventoryData(GridInventoryData data);
}
