package com.dreamingfish.gridinventory.common.block;

import net.minecraft.network.chat.Component;

public final class BasicSearchableGridContainerBlock extends AbstractSearchableGridContainerBlock {
    private final int columns;
    private final int rows;
    private final Component title;

    public BasicSearchableGridContainerBlock(Properties properties, int columns, int rows, Component title) {
        super(properties);
        if (columns <= 0 || rows <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        this.columns = columns;
        this.rows = rows;
        this.title = title;
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public Component title() {
        return title;
    }
}
