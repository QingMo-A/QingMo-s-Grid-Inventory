package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.DFGridInventoryCommon;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.target.forge1201.config.Forge1201ClientConfig;
import com.dreamingfish.gridinventory.target.forge1201.config.Forge1201ServerConfig;
import com.dreamingfish.gridinventory.target.forge1201.event.Forge1201CommonEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        modEventBus.addListener(this::commonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Forge1201ClientConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Forge1201ServerConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(Forge1201CommonEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> GridInventoryServices.accessories().registerBackpackAccessories());
        DFGridInventory.LOGGER.info("DF Grid Inventory Forge 1.20.1 loaded. Vanilla inventories and creative inventory are left untouched.");
    }
}
