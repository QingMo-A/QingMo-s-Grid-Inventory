package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import net.minecraft.world.entity.EquipmentSlot;

public record InsertIntoEquipmentStorageMessage(int playerSlot, EquipmentSlot equipmentSlot, String containerId, int targetX, int targetY, boolean rotated, boolean targetFolded) implements GridMessage {

    public static void handle(InsertIntoEquipmentStorageMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "InsertIntoEquipmentStorageMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.PlayerSlot(packet.playerSlot());
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
        return GridMessages.INSERT_INTO_EQUIPMENT_STORAGE;
    }
}
