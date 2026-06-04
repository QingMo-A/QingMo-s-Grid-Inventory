package com.dreamingfish.gridinventory.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record GridEntry(UUID entryId, ItemStack stack, int x, int y, int width, int height, boolean rotated) {
    public static final Codec<GridEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("entry_id").forGetter(entry -> entry.entryId),
            ItemStack.OPTIONAL_CODEC.fieldOf("stack").forGetter(GridEntry::stack),
            Codec.INT.fieldOf("x").forGetter(GridEntry::x),
            Codec.INT.fieldOf("y").forGetter(GridEntry::y),
            Codec.INT.fieldOf("width").forGetter(GridEntry::width),
            Codec.INT.fieldOf("height").forGetter(GridEntry::height),
            Codec.BOOL.optionalFieldOf("rotated", false).forGetter(GridEntry::rotated)
    ).apply(instance, GridEntry::new));

    public boolean contains(int cellX, int cellY) {
        return cellX >= x && cellX < x + width && cellY >= y && cellY < y + height;
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeVarInt(width);
        buf.writeVarInt(height);
        buf.writeBoolean(rotated);
    }

    public static GridEntry decode(RegistryFriendlyByteBuf buf) {
        return new GridEntry(buf.readUUID(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }
}
