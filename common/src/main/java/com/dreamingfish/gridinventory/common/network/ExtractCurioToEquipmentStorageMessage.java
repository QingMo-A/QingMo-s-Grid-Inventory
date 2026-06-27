package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import net.minecraft.world.entity.EquipmentSlot;

public record ExtractCurioToEquipmentStorageMessage(String identifier, int index, EquipmentSlot equipmentSlot,
                                                   String containerId, int targetX, int targetY,
                                                   boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(ExtractCurioToEquipmentStorageMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "ExtractCurioToEquipmentStorageMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.AccessorySlot(packet.identifier(), packet.index());
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
        return GridMessages.EXTRACT_CURIO_TO_EQUIPMENT_STORAGE;
    }
}
