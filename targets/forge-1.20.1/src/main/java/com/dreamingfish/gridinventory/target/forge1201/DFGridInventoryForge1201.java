package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.DFGridInventoryCommon;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.target.forge1201.config.Forge1201ClientConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DFGridInventory.MODID)
public final class DFGridInventoryForge1201 {
    public DFGridInventoryForge1201() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Forge1201Platform platform = new Forge1201Platform();

        GridInventoryServices.init(platform);
        DFGridInventoryCommon.registerContent(platform.registry());
        platform.registry().register(modEventBus);
        platform.network().registerMessages();
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Forge1201ClientConfig.SPEC);
    }
}
