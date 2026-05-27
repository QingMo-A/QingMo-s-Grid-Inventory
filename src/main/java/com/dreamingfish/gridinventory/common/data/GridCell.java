package com.dreamingfish.gridinventory.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record GridCell(int x, int y) {
    public static final Codec<GridCell> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(GridCell::x),
            Codec.INT.fieldOf("y").forGetter(GridCell::y)
    ).apply(instance, GridCell::new));

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(x);
        buf.writeVarInt(y);
    }

    public static GridCell decode(RegistryFriendlyByteBuf buf) {
        return new GridCell(buf.readVarInt(), buf.readVarInt());
    }
}
