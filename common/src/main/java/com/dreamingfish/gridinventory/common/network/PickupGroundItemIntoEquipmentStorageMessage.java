package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemMoveService;
import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import net.minecraft.world.entity.EquipmentSlot;

public record PickupGroundItemIntoEquipmentStorageMessage(int entityId, EquipmentSlot equipmentSlot, String containerId,
                                                         int targetX, int targetY, boolean rotated) implements GridMessage {

    public static void handle(PickupGroundItemIntoEquipmentStorageMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "PickupGroundItemIntoEquipmentStorageMessage",
                (player, menu) -> {
                    GridItemSource source = new GridItemSource.GroundItem(packet.entityId());
                    GridItemTarget target = new GridItemTarget.EquipmentStoragePlacement(packet.equipmentSlot(),
                            packet.containerId(), packet.targetX(), packet.targetY(), packet.rotated(), false);
                    if (GridItemMoveService.move(menu, source, target,
                            GridMoveOptions.all(packet.rotated(), false))) {
                        menu.broadcastChanges();
                        ModNetworking.syncMenu(player, menu);
                    }
                });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.PICKUP_GROUND_ITEM_INTO_EQUIPMENT_STORAGE;
    }
}
