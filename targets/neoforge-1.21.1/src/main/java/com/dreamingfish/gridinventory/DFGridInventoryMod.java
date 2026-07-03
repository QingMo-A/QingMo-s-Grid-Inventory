package com.dreamingfish.gridinventory;

import com.dreamingfish.gridinventory.DFGridInventoryCommon;
import com.dreamingfish.gridinventory.client.config.GridInventoryClientConfig;
import com.dreamingfish.gridinventory.common.config.GridInventoryConfig;
import com.dreamingfish.gridinventory.common.size.GridItemSizeLoader;
import com.dreamingfish.gridinventory.common.size.GridItemSizeSyncManager;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingLoader;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingSyncManager;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageLoader;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionLoader;
import com.dreamingfish.gridinventory.common.raid.command.QmRaidCommands;
import com.dreamingfish.gridinventory.common.raid.runtime.RaidExtractionCountdownService;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import com.dreamingfish.gridinventory.platform.neoforge.NeoForgeGridInventoryPlatform;
import com.dreamingfish.gridinventory.target.neoforge1211.protocol.NeoForge1211ProtocolCompat;
import com.dreamingfish.gridinventory.target.neoforge1211.client.NeoForge1211ClientEvents;
import com.dreamingfish.gridinventory.target.neoforge1211.event.NeoForge1211EquipmentStorageEvents;
import com.dreamingfish.gridinventory.target.neoforge1211.event.NeoForge1211ManualPickupEvents;
import com.dreamingfish.gridinventory.target.neoforge1211.registry.NeoForge1211Attachments;
import com.dreamingfish.gridinventory.target.neoforge1211.registry.NeoForge1211DataComponents;
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
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(DFGridInventoryMod.MODID)
public class DFGridInventoryMod {
    public static final String MODID = DFGridInventory.MODID;

    public DFGridInventoryMod(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeGridInventoryPlatform platform = new NeoForgeGridInventoryPlatform();
        GridInventoryServices.init(platform);

        DFGridInventoryCommon.registerContent(platform.registry());
        NeoForge1211DataComponents.register(platform.registry());
        platform.registry().registerBlocks(modEventBus);
        platform.registry().registerBlockEntities(modEventBus);
        platform.registry().registerItems(modEventBus);
        platform.registry().registerMenus(modEventBus);
        NeoForge1211Attachments.ATTACHMENT_TYPES.register(modEventBus);
        platform.registry().registerDataComponents(modEventBus);
        platform.registry().registerCreativeTabs(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(NeoForge1211ProtocolCompat::register);
        modContainer.registerConfig(ModConfig.Type.CLIENT, GridInventoryClientConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, GridInventoryConfig.SPEC);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(NeoForge1211ManualPickupEvents.class);
        NeoForge.EVENT_BUS.register(NeoForge1211EquipmentStorageEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> GridInventoryServices.accessories().registerBackpackAccessories());
        DFGridInventory.LOGGER.info("DF Grid Inventory loaded. Vanilla inventories and creative inventory are left untouched.");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        QmRaidCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        RaidExtractionCountdownService.serverTick(event.getServer());
    }

    @SubscribeEvent
    public void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new GridItemSizeLoader());
        event.addListener(new BackpackFoldingLoader());
        event.addListener(new PlayerPocketDefinitionLoader());
        event.addListener(new EquipmentStorageLoader());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            GridItemSizeSyncManager.syncTo(event.getPlayer());
            BackpackFoldingSyncManager.syncTo(event.getPlayer());
        } else {
            GridItemSizeSyncManager.syncToAll();
            BackpackFoldingSyncManager.syncToAll();
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        private ClientModEvents() {
        }

        @SubscribeEvent
        public static void registerKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent event) {
            NeoForge1211ClientEvents.registerKeys(event);
        }

        @SubscribeEvent
        public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            NeoForge1211ClientEvents.registerScreens(event);
        }

        @SubscribeEvent
        public static void clientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
            event.enqueueWork(NeoForge1211ClientEvents::registerItemProperties);
        }
    }
}
