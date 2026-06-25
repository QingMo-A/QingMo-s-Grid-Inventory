package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public record TransferNestedGridEntryIntoEquipmentStorageMessage(NestedContainerPath sourceOwnerPath,
                                                                 String sourceContainerId,
                                                                 UUID entryId,
                                                                 EquipmentSlot equipmentSlot,
                                                                 String targetContainerId,
                                                                 int targetX,
                                                                 int targetY,
                                                                 boolean rotated,
                                                                 boolean targetFolded) implements GridMessage {
    public static void handle(TransferNestedGridEntryIntoEquipmentStorageMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            GridItemSource source = message.sourceContainerId().isEmpty()
                    ? new GridItemSource.NestedGridEntry(message.sourceOwnerPath(), message.entryId())
                    : new GridItemSource.NestedEquipmentStorageEntry(message.sourceOwnerPath(),
                    message.sourceContainerId(), message.entryId());
            GridItemTarget target = new GridItemTarget.EquipmentStoragePlacement(message.equipmentSlot(),
                    message.targetContainerId(), message.targetX(), message.targetY(), message.rotated(),
                    message.targetFolded());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(message.rotated(), message.targetFolded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(context.player(), menu);
            }
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.TRANSFER_NESTED_GRID_ENTRY_INTO_EQUIPMENT_STORAGE;
    }
}
