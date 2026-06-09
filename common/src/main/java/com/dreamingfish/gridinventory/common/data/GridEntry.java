package com.dreamingfish.gridinventory.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public record GridEntry(UUID entryId, ItemStack stack, int x, int y, int width, int height, boolean rotated) {
    public static final Codec<GridEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("entry_id").forGetter(entry -> entry.entryId),
            ItemStack.CODEC.fieldOf("stack").forGetter(GridEntry::stack),
            Codec.INT.fieldOf("x").forGetter(GridEntry::x),
            Codec.INT.fieldOf("y").forGetter(GridEntry::y),
            Codec.INT.fieldOf("width").forGetter(GridEntry::width),
            Codec.INT.fieldOf("height").forGetter(GridEntry::height),
            Codec.BOOL.optionalFieldOf("rotated", false).forGetter(GridEntry::rotated)
    ).apply(instance, GridEntry::new));

    public boolean contains(int cellX, int cellY) {
        return cellX >= x && cellX < x + width && cellY >= y && cellY < y + height;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(entryId);
        buf.writeNbt(encodeStack(stack));
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeVarInt(width);
        buf.writeVarInt(height);
        buf.writeBoolean(rotated);
    }

    public static GridEntry decode(FriendlyByteBuf buf) {
        return new GridEntry(buf.readUUID(), decodeStack(buf.readNbt()), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    private static CompoundTag encodeStack(ItemStack stack) {
        DataResult<Tag> result = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack);
        return result.result()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .orElseGet(CompoundTag::new);
    }

    private static ItemStack decodeStack(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return ItemStack.EMPTY;
        }
        DynamicOps<Tag> ops = NbtOps.INSTANCE;
        return ItemStack.CODEC.parse(new Dynamic<>(ops, tag)).result().orElse(ItemStack.EMPTY);
    }
}
