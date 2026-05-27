package com.dreamingfish.gridinventory.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.List;

public record GridSection(String id, List<GridCell> cells) {
    public static final Codec<GridSection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(GridSection::id),
            GridCell.CODEC.listOf().fieldOf("cells").forGetter(GridSection::cells)
    ).apply(instance, GridSection::new));

    public boolean contains(int x, int y) {
        return cells.stream().anyMatch(cell -> cell.x() == x && cell.y() == y);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeVarInt(cells.size());
        for (GridCell cell : cells) {
            cell.encode(buf);
        }
    }

    public static GridSection decode(RegistryFriendlyByteBuf buf) {
        String id = buf.readUtf();
        int size = buf.readVarInt();
        java.util.ArrayList<GridCell> cells = new java.util.ArrayList<>();
        for (int index = 0; index < size; index++) {
            cells.add(GridCell.decode(buf));
        }
        return new GridSection(id, List.copyOf(cells));
    }
}
