package com.dreamingfish.gridinventory.target.forge1201;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionManager;
import com.dreamingfish.gridinventory.platform.GridInventoryPlayerDataBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public final class Forge1201PlayerDataBridge implements GridInventoryPlayerDataBridge {
    private static final String ROOT = "df_grid_inventory";
    private static final String PLAYER_GRID_INVENTORY = "player_grid_inventory";

    @Override
    public GridInventoryData getPlayerGridInventory(Player player) {
        CompoundTag root = rootOrNull(player);
        if (root == null || !root.contains(PLAYER_GRID_INVENTORY)) {
            return PlayerPocketDefinitionManager.createInventory();
        }
        return GridInventoryData.CODEC.parse(NbtOps.INSTANCE, root.get(PLAYER_GRID_INVENTORY))
                .resultOrPartial(message -> DFGridInventory.LOGGER.warn("Failed to read player grid inventory: {}", message))
                .orElseGet(PlayerPocketDefinitionManager::createInventory);
    }

    @Override
    public void setPlayerGridInventory(Player player, GridInventoryData data) {
        if (data == null) {
            removePlayerGridInventory(player);
            player.getInventory().setChanged();
            return;
        }
        GridInventoryData.CODEC.encodeStart(NbtOps.INSTANCE, data)
                .resultOrPartial(message -> DFGridInventory.LOGGER.warn("Failed to write player grid inventory: {}", message))
                .ifPresent(tag -> root(player).put(PLAYER_GRID_INVENTORY, tag));
        player.getInventory().setChanged();
    }

    @Override
    public GridInventoryData copyPlayerGridInventory(Player player) {
        return getPlayerGridInventory(player).copy();
    }

    private static void removePlayerGridInventory(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag root = data.getCompound(ROOT);
        root.remove(PLAYER_GRID_INVENTORY);
        if (root.isEmpty()) {
            data.remove(ROOT);
        }
    }

    private static CompoundTag root(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT, Tag.TAG_COMPOUND)) {
            data.put(ROOT, new CompoundTag());
        }
        return data.getCompound(ROOT);
    }

    private static CompoundTag rootOrNull(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(ROOT, Tag.TAG_COMPOUND)) {
            return null;
        }
        return data.getCompound(ROOT);
    }
}
