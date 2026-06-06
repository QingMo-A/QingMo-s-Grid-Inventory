package com.dreamingfish.gridinventory.common.inventory;

import com.dreamingfish.gridinventory.api.EquipmentStorageContainerDefinition;
import com.dreamingfish.gridinventory.common.data.GridEntry;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.data.GridSection;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;

import java.util.List;
import java.util.Objects;

public final class PlayerPocketDefinitionManager {
    private static EquipmentStorageContainerDefinition definition;

    private PlayerPocketDefinitionManager() {
    }

    public static void replaceDefinition(EquipmentStorageContainerDefinition loadedDefinition) {
        definition = loadedDefinition;
    }

    public static GridInventoryData createInventory() {
        EquipmentStorageContainerDefinition current = currentDefinition();
        return GridInventoryData.withSections(current.resolvedColumns(), current.resolvedRows(), current.resolvedSections());
    }

    public static GridInventoryData refreshShape(GridInventoryData current) {
        EquipmentStorageContainerDefinition resolved = currentDefinition();
        int columns = resolved.resolvedColumns();
        int rows = resolved.resolvedRows();
        List<GridSection> sections = resolved.resolvedSections();
        if (current.getColumns() == columns && current.getRows() == rows && Objects.equals(current.getSections(), sections)) {
            return current;
        }
        if (!allEntriesFit(current.getEntries(), columns, rows, sections)) {
            return current;
        }
        return new GridInventoryData(columns, rows, current.getEntries(), sections);
    }

    private static EquipmentStorageContainerDefinition currentDefinition() {
        return definition != null ? definition : defaultDefinition();
    }

    private static EquipmentStorageContainerDefinition defaultDefinition() {
        return new EquipmentStorageContainerDefinition(
                "pocket",
                "screen.df_grid_inventory.pocket",
                GridInventoryServices.config().pocketColumns(),
                GridInventoryServices.config().pocketRows(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private static boolean allEntriesFit(List<GridEntry> entries, int columns, int rows, List<GridSection> sections) {
        GridInventoryData shaped = new GridInventoryData(columns, rows, List.of(), sections);
        for (GridEntry entry : entries) {
            for (int y = entry.y(); y < entry.y() + entry.height(); y++) {
                for (int x = entry.x(); x < entry.x() + entry.width(); x++) {
                    if (!shaped.isEnabledCell(x, y)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
