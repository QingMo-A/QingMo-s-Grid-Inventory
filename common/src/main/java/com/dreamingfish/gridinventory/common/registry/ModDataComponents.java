package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public final class ModDataComponents {
    public static final Supplier<DataComponentType<GridInventoryData>> GRID_INVENTORY = GridInventoryServices.registry().registerDataComponent(
            "grid_inventory",
            () -> DataComponentType.<GridInventoryData>builder().persistent(GridInventoryData.CODEC).networkSynchronized(GridInventoryData.STREAM_CODEC).build()
    );
    public static final Supplier<DataComponentType<EquipmentStorageData>> EQUIPMENT_STORAGE = GridInventoryServices.registry().registerDataComponent(
            "equipment_storage",
            () -> DataComponentType.<EquipmentStorageData>builder().persistent(EquipmentStorageData.CODEC).networkSynchronized(EquipmentStorageData.STREAM_CODEC).build()
    );
    public static final Supplier<DataComponentType<Boolean>> BACKPACK_FOLDED = GridInventoryServices.registry().registerDataComponent(
            "backpack_folded",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(com.mojang.serialization.Codec.BOOL)
                    .networkSynchronized(StreamCodec.of((buf, value) -> buf.writeBoolean(value), buf -> buf.readBoolean()))
                    .build()
    );

    private ModDataComponents() {
    }

    public static void bootstrap() {
    }
}
