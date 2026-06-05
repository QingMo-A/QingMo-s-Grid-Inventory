package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.network.GridMessages;
import com.dreamingfish.gridinventory.common.network.OpenPlayerGridInventoryMessage;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Forge1201SimpleChannelBridge {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DFGridInventory.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int discriminator;

    private Forge1201SimpleChannelBridge() {
    }

    public static void register() {
        registerClientToServer(GridMessages.OPEN_PLAYER_GRID_INVENTORY);
    }

    public static void sendToServer(GridMessage message) {
        if (message instanceof OpenPlayerGridInventoryMessage) {
            CHANNEL.sendToServer(message);
            return;
        }
        throw new UnsupportedOperationException("Forge 1.20.1 protocol stub has not implemented " + message.type().id());
    }

    private static <T extends GridMessage> void registerClientToServer(GridMessageType<T> type) {
        CHANNEL.messageBuilder(type.messageClass(), discriminator++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(Forge1201MessageCodecs::encode)
                .decoder(buf -> Forge1201MessageCodecs.decode(type, buf))
                .consumerMainThread((message, context) -> type.handle(message, new Forge1201MessageContext(context)))
                .add();
    }
}
