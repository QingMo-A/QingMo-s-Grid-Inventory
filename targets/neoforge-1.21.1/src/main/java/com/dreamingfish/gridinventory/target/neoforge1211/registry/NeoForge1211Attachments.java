package com.dreamingfish.gridinventory.target.neoforge1211.registry;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.PlayerPocketDefinitionManager;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class NeoForge1211Attachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DFGridInventory.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GridInventoryData>> PLAYER_GRID_INVENTORY = ATTACHMENT_TYPES.register(
            "player_grid_inventory",
            () -> AttachmentType.builder(PlayerPocketDefinitionManager::createInventory)
                    .serialize(GridInventoryData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    private NeoForge1211Attachments() {
    }
}
