package com.dreamingfish.gridinventory.target.forge1201.platform;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.GridInventoryItemStackDataBridge;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class Forge1201ItemStackDataBridge implements GridInventoryItemStackDataBridge {
    private static final String ROOT = "df_grid_inventory";
    private static final String GRID_INVENTORY = "grid_inventory";
    private static final String EQUIPMENT_STORAGE = "equipment_storage";
    private static final String BACKPACK_FOLDED = "backpack_folded";

    @Override
    public GridInventoryData getGridInventory(ItemStack stack) {
        return read(stack, GRID_INVENTORY, GridInventoryData.CODEC);
    }

    @Override
    public void setGridInventory(ItemStack stack, GridInventoryData data) {
        if (data == null) {
            removeGridInventory(stack);
            return;
        }
        write(stack, GRID_INVENTORY, GridInventoryData.CODEC, data);
    }

    @Override
    public void removeGridInventory(ItemStack stack) {
        remove(stack, GRID_INVENTORY);
    }

    @Override
    public EquipmentStorageData getEquipmentStorage(ItemStack stack) {
        return read(stack, EQUIPMENT_STORAGE, EquipmentStorageData.CODEC);
    }

    @Override
    public void setEquipmentStorage(ItemStack stack, EquipmentStorageData data) {
        if (data == null) {
            removeEquipmentStorage(stack);
            return;
        }
        write(stack, EQUIPMENT_STORAGE, EquipmentStorageData.CODEC, data);
    }

    @Override
    public void removeEquipmentStorage(ItemStack stack) {
        remove(stack, EQUIPMENT_STORAGE);
    }

    @Override
    public Boolean isBackpackFolded(ItemStack stack) {
        CompoundTag root = rootOrNull(stack);
        if (root == null || !root.contains(BACKPACK_FOLDED)) {
            return null;
        }
        return root.getBoolean(BACKPACK_FOLDED);
    }

    @Override
    public void setBackpackFolded(ItemStack stack, boolean folded) {
        root(stack).putBoolean(BACKPACK_FOLDED, folded);
    }

    @Override
    public void removeBackpackFolded(ItemStack stack) {
        remove(stack, BACKPACK_FOLDED);
    }

    private static <T> T read(ItemStack stack, String key, Codec<T> codec) {
        CompoundTag root = rootOrNull(stack);
        if (root == null || !root.contains(key)) {
            return null;
        }
        return codec.parse(NbtOps.INSTANCE, root.get(key))
                .resultOrPartial(message -> DFGridInventory.LOGGER.warn("Failed to read ItemStack data {}: {}", key, message))
                .orElse(null);
    }

    private static <T> void write(ItemStack stack, String key, Codec<T> codec, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value)
                .resultOrPartial(message -> DFGridInventory.LOGGER.warn("Failed to write ItemStack data {}: {}", key, message))
                .ifPresent(tag -> root(stack).put(key, tag));
    }

    private static CompoundTag root(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(ROOT, Tag.TAG_COMPOUND)) {
            tag.put(ROOT, new CompoundTag());
        }
        return tag.getCompound(ROOT);
    }

    private static CompoundTag rootOrNull(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ROOT, Tag.TAG_COMPOUND)) {
            return null;
        }
        return tag.getCompound(ROOT);
    }

    private static void remove(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ROOT, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag root = tag.getCompound(ROOT);
        root.remove(key);
        if (root.isEmpty()) {
            tag.remove(ROOT);
        }
    }
}
