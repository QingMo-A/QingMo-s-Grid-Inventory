package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.DFGridInventoryCommon;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.target.forge1201.platform.Forge1201Platform;
import net.minecraftforge.fml.common.Mod;

@Mod(DFGridInventory.MODID)
public final class Forge1201Mod {
    public Forge1201Mod() {
        Forge1201Platform platform = new Forge1201Platform();
        GridInventoryServices.init(platform);
        DFGridInventoryCommon.registerContent(platform.registry());
    }
}
