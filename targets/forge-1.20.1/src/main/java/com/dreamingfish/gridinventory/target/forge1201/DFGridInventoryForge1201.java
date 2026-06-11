package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.DFGridInventoryCommon;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(DFGridInventory.MODID)
public final class DFGridInventoryForge1201 {
    public DFGridInventoryForge1201() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Forge1201Platform platform = new Forge1201Platform();

        GridInventoryServices.init(platform);
        DFGridInventoryCommon.registerContent(platform.registry());
        platform.registry().register(modEventBus);
    }
}
