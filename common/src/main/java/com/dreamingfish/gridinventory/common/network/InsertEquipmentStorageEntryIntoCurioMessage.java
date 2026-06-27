package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record InsertEquipmentStorageEntryIntoCurioMessage(EquipmentSlot sourceSlot, String containerId, UUID entryId,
                                                         String identifier, int index) implements GridMessage {

    public static void handle(InsertEquipmentStorageEntryIntoCurioMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "InsertEquipmentStorageEntryIntoCurioMessage",
                (player, menu) -> {
                    GridItemSource source = new GridItemSource.EquipmentStorageEntry(packet.sourceSlot(),
                            packet.containerId(), packet.entryId());
                    GridItemTarget target = new GridItemTarget.AccessorySlot(packet.identifier(), packet.index());
                    if (GridItemMoveService.move(menu, source, target, GridMoveOptions.all(false, false))) {
                        menu.broadcastChanges();
                        ModNetworking.syncMenu(player, menu);
                    }
                });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.INSERT_EQUIPMENT_STORAGE_ENTRY_INTO_CURIO;
    }
}
