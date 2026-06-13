package com.dreamingfish.gridinventory.target.forge1201.event;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageManager;
import com.dreamingfish.gridinventory.common.equipment.EquipmentStorageLoader;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingLoader;
import com.dreamingfish.gridinventory.common.folding.BackpackFoldingSyncManager;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionLoader;
import com.dreamingfish.gridinventory.common.size.GridItemSizeLoader;
import com.dreamingfish.gridinventory.common.size.GridItemSizeSyncManager;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class Forge1201CommonEvents {
    private Forge1201CommonEvents() {
    }

    @SubscribeEvent
    public static void addReloadListener(AddReloadListenerEvent event) {
        event.addListener(new GridItemSizeLoader());
        event.addListener(new BackpackFoldingLoader());
        event.addListener(new PlayerPocketDefinitionLoader());
        event.addListener(new EquipmentStorageLoader());
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            GridItemSizeSyncManager.syncTo(event.getPlayer());
            BackpackFoldingSyncManager.syncTo(event.getPlayer());
        } else {
            GridItemSizeSyncManager.syncToAll();
            BackpackFoldingSyncManager.syncToAll();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        initializeEquippedStorage(player, EquipmentSlot.CHEST);
        initializeEquippedStorage(player, EquipmentSlot.LEGS);
    }

    @SubscribeEvent
    public static void onEntityItemPickup(EntityItemPickupEvent event) {
        if (GridInventoryServices.config().disableVanillaAutoPickup()
                && event.getEntity() instanceof ServerPlayer) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        GridInventoryData copied = GridInventoryServices.playerData()
                .copyPlayerGridInventory(event.getOriginal());
        GridInventoryServices.playerData()
                .setPlayerGridInventory(event.getEntity(), copied);
    }

    private static void initializeEquippedStorage(ServerPlayer player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty() || GridInventoryServices.itemStackData().getEquipmentStorage(stack) != null) {
            return;
        }
        if (!EquipmentStorageManager.initializeStorage(stack, slot).containers().isEmpty()) {
            player.getInventory().setChanged();
        }
    }
}
