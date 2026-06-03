package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DFGridInventoryMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GridInventoryData>> GRID_INVENTORY = DATA_COMPONENTS.register(
            "grid_inventory",
            () -> DataComponentType.<GridInventoryData>builder().persistent(GridInventoryData.CODEC).networkSynchronized(GridInventoryData.STREAM_CODEC).build()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EquipmentStorageData>> EQUIPMENT_STORAGE = DATA_COMPONENTS.register(
            "equipment_storage",
            () -> DataComponentType.<EquipmentStorageData>builder().persistent(EquipmentStorageData.CODEC).networkSynchronized(EquipmentStorageData.STREAM_CODEC).build()
    );
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> BACKPACK_FOLDED = DATA_COMPONENTS.register(
            "backpack_folded",
            () -> DataComponentType.<Boolean>builder()
                    .persistent(com.mojang.serialization.Codec.BOOL)
                    .networkSynchronized(StreamCodec.of((buf, value) -> buf.writeBoolean(value), buf -> buf.readBoolean()))
                    .build()
    );

    private ModDataComponents() {
    }
}
