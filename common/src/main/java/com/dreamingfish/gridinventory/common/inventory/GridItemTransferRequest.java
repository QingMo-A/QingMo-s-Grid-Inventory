package com.dreamingfish.gridinventory.common.inventory;

public record GridItemTransferRequest(
        GridItemSource source,
        GridItemTarget target,
        int amount
) {
    public GridItemTransferRequest {
        amount = Math.max(1, amount);
    }
}
