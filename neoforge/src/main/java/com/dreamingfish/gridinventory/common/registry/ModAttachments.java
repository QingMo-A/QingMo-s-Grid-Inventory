package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import com.dreamingfish.gridinventory.DFGridInventoryMod;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DFGridInventoryMod.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GridInventoryData>> PLAYER_GRID_INVENTORY = ATTACHMENT_TYPES.register(
            "player_grid_inventory",
            () -> AttachmentType.builder(() -> new GridInventoryData(GridInventoryServices.config().pocketColumns(), GridInventoryServices.config().pocketRows()))
                    .serialize(GridInventoryData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    private ModAttachments() {
    }
}
