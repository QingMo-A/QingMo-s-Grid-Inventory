package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.compat.curios.CuriosIntegration;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.registry.ModDataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.dreamingfish.gridinventory.platform.network.GridPacketContext;

public record SyncEquipmentStoragePacket(EquipmentSlot slot, EquipmentStorageData storage) implements CustomPacketPayload {
    public static final Type<SyncEquipmentStoragePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(DFGridInventory.MODID, "sync_equipment_storage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEquipmentStoragePacket> STREAM_CODEC = StreamCodec.ofMember(SyncEquipmentStoragePacket::encode, SyncEquipmentStoragePacket::decode);

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(slot);
        EquipmentStorageData.STREAM_CODEC.encode(buf, storage);
    }

    private static SyncEquipmentStoragePacket decode(RegistryFriendlyByteBuf buf) {
        return new SyncEquipmentStoragePacket(buf.readEnum(EquipmentSlot.class), EquipmentStorageData.STREAM_CODEC.decode(buf));
    }

    public static void handle(SyncEquipmentStoragePacket packet, GridPacketContext context) {
        ItemStack stack = packet.slot() == EquipmentSlot.BODY
                ? CuriosIntegration.getCurioStack(context.player(), "back", 0).orElse(ItemStack.EMPTY)
                : context.player().getItemBySlot(packet.slot());
        if (stack.isEmpty()) {
            return;
        }
        stack.set(ModDataComponents.EQUIPMENT_STORAGE.get(), packet.storage());
        if (packet.slot() == EquipmentSlot.BODY) {
            CuriosIntegration.setCurioStack(context.player(), "back", 0, stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
