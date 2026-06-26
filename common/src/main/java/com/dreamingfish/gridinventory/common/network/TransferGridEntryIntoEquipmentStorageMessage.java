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

public record TransferGridEntryIntoEquipmentStorageMessage(UUID entryId, EquipmentSlot equipmentSlot, String containerId,
                                                           int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(TransferGridEntryIntoEquipmentStorageMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "TransferGridEntryIntoEquipmentStorageMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.MenuGridEntry(packet.entryId());
            GridItemTarget target = new GridItemTarget.EquipmentStoragePlacement(packet.equipmentSlot(),
                    packet.containerId(), packet.targetX(), packet.targetY(), packet.rotated(), packet.targetFolded());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(packet.rotated(), packet.targetFolded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_GRID_ENTRY_INTO_EQUIPMENT_STORAGE;
    }
}
