package com.dreamingfish.gridinventory.target.neoforge1211.registry;

import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.platform.neoforge.NeoForgeGridInventoryRegistryBridge;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public final class NeoForge1211DataComponents {
    private static final StreamCodec<RegistryFriendlyByteBuf, GridInventoryData> GRID_INVENTORY_STREAM_CODEC =
            StreamCodec.of((buf, data) -> data.encode(buf), GridInventoryData::decode);
    private static final StreamCodec<RegistryFriendlyByteBuf, EquipmentStorageData> EQUIPMENT_STORAGE_STREAM_CODEC =
            StreamCodec.of((buf, data) -> data.encode(buf), EquipmentStorageData::decode);

    public static Supplier<DataComponentType<GridInventoryData>> GRID_INVENTORY;
    public static Supplier<DataComponentType<EquipmentStorageData>> EQUIPMENT_STORAGE;
    public static Supplier<DataComponentType<Boolean>> BACKPACK_FOLDED;

    private static boolean registered;

    private NeoForge1211DataComponents() {
    }

    public static void register(NeoForgeGridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;

        GRID_INVENTORY = registry.registerDataComponent(
                "grid_inventory",
                () -> DataComponentType.<GridInventoryData>builder().persistent(GridInventoryData.CODEC).networkSynchronized(GRID_INVENTORY_STREAM_CODEC).build()
        );
        EQUIPMENT_STORAGE = registry.registerDataComponent(
                "equipment_storage",
                () -> DataComponentType.<EquipmentStorageData>builder().persistent(EquipmentStorageData.CODEC).networkSynchronized(EQUIPMENT_STORAGE_STREAM_CODEC).build()
        );
        BACKPACK_FOLDED = registry.registerDataComponent(
                "backpack_folded",
                () -> DataComponentType.<Boolean>builder()
                        .persistent(com.mojang.serialization.Codec.BOOL)
                        .networkSynchronized(StreamCodec.of((buf, value) -> buf.writeBoolean(value), buf -> buf.readBoolean()))
                        .build()
        );
    }
}
