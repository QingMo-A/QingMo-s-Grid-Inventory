package com.dreamingfish.gridinventory.common.block;

public final class BasicSearchableGridContainerBlock extends AbstractSearchableGridContainerBlock {
    private final SearchableGridContainerSpec spec;

    public BasicSearchableGridContainerBlock(Properties properties, SearchableGridContainerSpec spec) {
        super(properties);
        this.spec = spec;
    }

    public int columns() {
        return spec.columns();
    }

    public int rows() {
        return spec.rows();
    }

    public SearchableGridContainerSpec spec() {
        return spec;
    }
}
