package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.world.entity.EquipmentSlot;

public record CreativeInsertIntoEquipmentStorageMessage(int tabIndex, int itemIndex, int count, EquipmentSlot equipmentSlot,
                                                        String containerId, int targetX, int targetY,
                                                        boolean rotated, boolean folded) implements GridMessage {
    public static void handle(CreativeInsertIntoEquipmentStorageMessage message, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "CreativeInsertIntoEquipmentStorageMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.CreativeItem(
                    message.tabIndex(), message.itemIndex(), message.count());
            GridItemTarget target = new GridItemTarget.EquipmentStoragePlacement(
                    message.equipmentSlot(), message.containerId(), message.targetX(), message.targetY(),
                    message.rotated(), message.folded());
            if (GridItemMoveService.move(menu, source, target,
                    GridMoveOptions.all(message.rotated(), message.folded()))) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.CREATIVE_INSERT_INTO_EQUIPMENT_STORAGE;
    }
}
