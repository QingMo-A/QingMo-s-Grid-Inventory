package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, DFGridInventoryMod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GridInventoryData>> GRID_INVENTORY = DATA_COMPONENTS.register(
            "grid_inventory",
            () -> DataComponentType.<GridInventoryData>builder().persistent(GridInventoryData.CODEC).networkSynchronized(GridInventoryData.STREAM_CODEC).build()
    );

    private ModDataComponents() {
    }
}
