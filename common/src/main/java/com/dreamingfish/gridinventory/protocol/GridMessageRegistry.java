package com.dreamingfish.gridinventory.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GridMessageRegistry {
    private final List<GridMessageType<?>> messages = new ArrayList<>();

    public <T extends GridMessage> GridMessageType<T> register(GridMessageType<T> type) {
        messages.add(type);
        return type;
    }

    public List<GridMessageType<?>> messages() {
        return Collections.unmodifiableList(messages);
    }
}
