package com.dreamingfish.gridinventory.common.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record EquipmentStorageData(List<NamedGridInventoryData> containers) {
    public static final EquipmentStorageData EMPTY = new EquipmentStorageData(List.of());
    public static final Codec<EquipmentStorageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NamedGridInventoryData.CODEC.listOf().optionalFieldOf("containers", List.of()).forGetter(EquipmentStorageData::containers)
    ).apply(instance, EquipmentStorageData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentStorageData> STREAM_CODEC = StreamCodec.ofMember(EquipmentStorageData::encode, EquipmentStorageData::decode);

    public boolean isEmpty() {
        return containers.stream().allMatch(container -> container.inventory().getEntries().isEmpty());
    }

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(containers.size());
        for (NamedGridInventoryData container : containers) {
            container.encode(buf);
        }
    }

    private static EquipmentStorageData decode(RegistryFriendlyByteBuf buf) {
        return new EquipmentStorageData(java.util.stream.IntStream.range(0, buf.readVarInt())
                .mapToObj(ignored -> NamedGridInventoryData.decode(buf))
                .toList());
    }
}
