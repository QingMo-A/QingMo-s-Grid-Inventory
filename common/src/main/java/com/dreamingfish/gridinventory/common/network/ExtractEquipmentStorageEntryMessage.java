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

public record ExtractEquipmentStorageEntryMessage(EquipmentSlot equipmentSlot, String containerId, UUID entryId, int playerSlot, int amount) implements GridMessage {

    public static void handle(ExtractEquipmentStorageEntryMessage packet, GridMessageContext context) {
        GridMoveMessageGuards.withGridMenu(context, "ExtractEquipmentStorageEntryMessage", (player, menu) -> {
            GridItemSource source = new GridItemSource.EquipmentStorageEntry(
                    packet.equipmentSlot(), packet.containerId(), packet.entryId());
            GridItemTarget target = new GridItemTarget.PlayerSlot(packet.playerSlot());
            GridMoveOptions options = new GridMoveOptions(packet.amount(), false, false);
            if (GridItemMoveService.move(menu, source, target, options)) {
                menu.broadcastChanges();
                ModNetworking.syncMenu(player, menu);
            }
        });
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.EXTRACT_EQUIPMENT_STORAGE_ENTRY;
    }
}
