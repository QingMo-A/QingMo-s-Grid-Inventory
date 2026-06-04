package com.dreamingfish.gridinventory.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;

public record GridItemSize(int width, int height, boolean rotatable) {
    public static final GridItemSize DEFAULT = new GridItemSize(1, 1, false);

    public static final Codec<GridItemSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("width").forGetter(GridItemSize::width),
            Codec.INT.fieldOf("height").forGetter(GridItemSize::height),
            Codec.BOOL.optionalFieldOf("rotatable", false).forGetter(GridItemSize::rotatable)
    ).apply(instance, GridItemSize::new));

    public GridItemSize {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Grid item size must be at least 1x1");
        }
    }

    public int placedWidth(boolean rotated) {
        return rotated ? height : width;
    }

    public int placedHeight(boolean rotated) {
        return rotated ? width : height;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(width);
        buf.writeVarInt(height);
        buf.writeBoolean(rotatable);
    }

    public static GridItemSize decode(FriendlyByteBuf buf) {
        return new GridItemSize(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }
}
