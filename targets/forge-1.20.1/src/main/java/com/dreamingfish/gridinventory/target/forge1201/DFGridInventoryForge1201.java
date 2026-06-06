package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraftforge.fml.common.Mod;

@Mod(DFGridInventory.MODID)
public final class DFGridInventoryForge1201 {
    public DFGridInventoryForge1201() {
        GridInventoryServices.init(new Forge1201Platform());
    }
}
