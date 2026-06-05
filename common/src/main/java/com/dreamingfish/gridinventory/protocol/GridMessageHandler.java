package com.dreamingfish.gridinventory.protocol;

@FunctionalInterface
public interface GridMessageHandler<T extends GridMessage> {
    void handle(T message, GridMessageContext context);
}
