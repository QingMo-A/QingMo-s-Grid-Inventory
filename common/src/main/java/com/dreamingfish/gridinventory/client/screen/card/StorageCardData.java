package com.dreamingfish.gridinventory.client.screen.card;

import com.dreamingfish.gridinventory.common.data.NamedGridInventoryData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record StorageCardData(
        StorageCardKey key,
        String stateKey,
        Component title,
        Component subtitle,
        ItemStack icon,
        boolean available,
        @Nullable EquipmentSlot slot,
        List<NamedGridInventoryData> containers
) {
    public int usedCells() {
        return containers.stream().mapToInt(container -> container.inventory().getEntries().stream()
                .mapToInt(entry -> entry.width() * entry.height()).sum()).sum();
    }

    public int totalCells() {
        return containers.stream().mapToInt(container -> {
            int total = 0;
            for (int y = 0; y < container.inventory().getRows(); y++) {
                for (int x = 0; x < container.inventory().getColumns(); x++) {
                    if (container.inventory().isEnabledCell(x, y)) {
                        total++;
                    }
                }
            }
            return total;
        }).sum();
    }
}
