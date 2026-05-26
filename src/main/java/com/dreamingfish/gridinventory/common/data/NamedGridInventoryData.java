package com.dreamingfish.gridinventory.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;

public record NamedGridInventoryData(String id, String title, GridInventoryData inventory) {
    public static final Codec<NamedGridInventoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(NamedGridInventoryData::id),
            Codec.STRING.fieldOf("title").forGetter(NamedGridInventoryData::title),
            GridInventoryData.CODEC.fieldOf("inventory").forGetter(NamedGridInventoryData::inventory)
    ).apply(instance, NamedGridInventoryData::new));

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(id);
        buf.writeUtf(title);
        inventory.encode(buf);
    }

    public static NamedGridInventoryData decode(RegistryFriendlyByteBuf buf) {
        return new NamedGridInventoryData(buf.readUtf(), buf.readUtf(), GridInventoryData.decode(buf));
    }
}
