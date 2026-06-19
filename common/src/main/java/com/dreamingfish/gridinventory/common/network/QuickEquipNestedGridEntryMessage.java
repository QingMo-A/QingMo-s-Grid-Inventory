package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import com.dreamingfish.gridinventory.common.menu.GridInventoryMenu;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageContext;
import com.dreamingfish.gridinventory.protocol.GridMessageType;

import java.util.UUID;

public record QuickEquipNestedGridEntryMessage(NestedContainerPath sourceOwnerPath, String sourceContainerId,
                                               UUID entryId) implements GridMessage {
    public static void handle(QuickEquipNestedGridEntryMessage message, GridMessageContext context) {
        if (context.player().containerMenu instanceof GridInventoryMenu menu) {
            menu.quickEquipNestedGridEntry(message.sourceOwnerPath(), message.sourceContainerId(), message.entryId());
            menu.broadcastChanges();
            ModNetworking.syncMenu(context.player(), menu);
        }
    }

    @Override
    public GridMessageType<?> type() {
        return GridMessages.QUICK_EQUIP_NESTED_GRID_ENTRY;
    }
}
