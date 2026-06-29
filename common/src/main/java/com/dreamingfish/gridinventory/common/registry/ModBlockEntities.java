package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static Supplier<BlockEntityType<SearchableGridContainerBlockEntity>> SEARCHABLE_GRID_CONTAINER;

    private static boolean registered;

    private ModBlockEntities() {
    }

    public static void register(GridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;
        SEARCHABLE_GRID_CONTAINER = registry.registerBlockEntity(
                GridContentIds.SEARCHABLE_GRID_CONTAINER_BLOCK_ENTITY,
                SearchableGridContainerBlockEntity::new,
                ModBlocks.SEARCHABLE_GRID_CONTAINER);
    }
}
