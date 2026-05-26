package com.dreamingfish.gridinventory;

import com.dreamingfish.gridinventory.client.ClientEvents;
import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.common.pickup.ManualPickupEvents;
import com.dreamingfish.gridinventory.common.network.ModNetworking;
import com.dreamingfish.gridinventory.common.registry.ModCreativeTabs;
import com.dreamingfish.gridinventory.common.registry.ModAttachments;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import com.dreamingfish.gridinventory.common.registry.ModItems;
import com.dreamingfish.gridinventory.common.registry.ModMenus;
import com.dreamingfish.gridinventory.common.size.GridItemSizeLoader;
import com.dreamingfish.gridinventory.common.size.GridItemSizeSyncManager;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageLoader;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.slf4j.Logger;

@Mod(DFGridInventoryMod.MODID)
public class DFGridInventoryMod {
    public static final String MODID = "df_grid_inventory";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DFGridInventoryMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModNetworking::register);
        modEventBus.addListener(ClientEvents::registerKeys);
        modContainer.registerConfig(ModConfig.Type.CLIENT, GridInventoryClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, GridInventoryConfig.SPEC);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ManualPickupEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("DF Grid Inventory loaded. Vanilla inventories and creative inventory are left untouched.");
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new GridItemSizeLoader());
        event.addListener(new EquipmentStorageLoader());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            GridItemSizeSyncManager.syncTo(event.getPlayer());
        } else {
            GridItemSizeSyncManager.syncToAll();
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            ClientEvents.registerScreens(event);
        }
    }
}
