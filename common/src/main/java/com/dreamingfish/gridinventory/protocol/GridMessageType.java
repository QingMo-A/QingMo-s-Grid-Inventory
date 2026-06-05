package com.dreamingfish.gridinventory.protocol;

import net.minecraft.resources.ResourceLocation;

public final class GridMessageType<T extends GridMessage> {
    private final ResourceLocation id;
    private final GridMessageDirection direction;
    private final Class<T> messageClass;
    private final GridMessageHandler<T> handler;

    public GridMessageType(ResourceLocation id, GridMessageDirection direction, Class<T> messageClass, GridMessageHandler<T> handler) {
        this.id = id;
        this.direction = direction;
        this.messageClass = messageClass;
        this.handler = handler;
    }

    public ResourceLocation id() {
        return id;
    }

    public GridMessageDirection direction() {
        return direction;
    }

    public Class<T> messageClass() {
        return messageClass;
    }

    public void handle(T message, GridMessageContext context) {
        handler.handle(message, context);
    }
}
