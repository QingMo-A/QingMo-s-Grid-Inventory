package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.network.*;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NeoForge1211MessageCodecs {
    private static final Map<GridMessageType<?>, NeoForge1211MessageCodec<?>> CODECS = new LinkedHashMap<>();

    static {
        registerAll();
    }

    private NeoForge1211MessageCodecs() {
    }

    public static boolean hasCodec(GridMessageType<?> type) {
        return CODECS.containsKey(type);
    }

    public static void encode(GridMessage message, RegistryFriendlyByteBuf buf) {
        codec(message.type()).encodeUnchecked(message, buf);
    }

    public static <T extends GridMessage> T decode(GridMessageType<T> type, RegistryFriendlyByteBuf buf) {
        return type.messageClass().cast(codec(type).decode(buf));
    }

    private static void registerAll() {
        register(GridMessages.DROP_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); },
                b -> new DropEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID())
        ));
        register(GridMessages.DROP_GRID_ENTRY, codec(
                (m, b) -> b.writeUUID(m.entryId()),
                b -> new DropGridEntryMessage(b.readUUID())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new ExtractCurioToEquipmentStorageMessage(b.readUtf(), b.readVarInt(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_GRID, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new ExtractCurioToGridMessage(b.readUtf(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_PLAYER_SLOT, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); b.writeVarInt(m.targetPlayerSlot()); },
                b -> new ExtractCurioToPlayerSlotMessage(b.readUtf(), b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.EXTRACT_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.playerSlot()); b.writeVarInt(m.amount()); },
                b -> new ExtractEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.EXTRACT_GRID_ENTRY_TO_PLAYER_SLOT, uuidSlotAmount(ExtractGridEntryToPlayerSlotMessage::new));
        register(GridMessages.EXTRACT_TO_PLAYER_INVENTORY, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeVarInt(m.amount()); },
                b -> new ExtractToPlayerInventoryMessage(b.readUUID(), b.readVarInt())
        ));
        register(GridMessages.INSERT_EQUIPMENT_STORAGE_ENTRY_INTO_CURIO, codec(
                (m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeUtf(m.identifier()); b.writeVarInt(m.index()); },
                b -> new InsertEquipmentStorageEntryIntoCurioMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readUtf(), b.readVarInt())
        ));
        register(GridMessages.INSERT_FROM_PLAYER_INVENTORY, codec(
                (m, b) -> { b.writeVarInt(m.playerSlot()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.quick()); b.writeBoolean(m.targetFolded()); },
                b -> new InsertFromPlayerInventoryMessage(b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.INSERT_GRID_ENTRY_INTO_CURIO, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeUtf(m.identifier()); b.writeVarInt(m.index()); },
                b -> new InsertGridEntryIntoCurioMessage(b.readUUID(), b.readUtf(), b.readVarInt())
        ));
        register(GridMessages.INSERT_INTO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeVarInt(m.playerSlot()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new InsertIntoEquipmentStorageMessage(b.readVarInt(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.INSERT_PLAYER_SLOT_INTO_CURIO, codec(
                (m, b) -> { b.writeVarInt(m.sourcePlayerSlot()); b.writeUtf(m.identifier()); b.writeVarInt(m.index()); },
                b -> new InsertPlayerSlotIntoCurioMessage(b.readVarInt(), b.readUtf(), b.readVarInt())
        ));
        register(GridMessages.MANUAL_PICKUP_ITEM, codec(
                (m, b) -> b.writeInt(m.entityId()),
                b -> new ManualPickupItemMessage(b.readInt())
        ));
        register(GridMessages.MOVE_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new MoveEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.MOVE_GRID_ENTRY, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new MoveGridEntryMessage(b.readUUID(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.MOVE_PLAYER_FREE_SLOT, codec(
                (m, b) -> { b.writeVarInt(m.sourcePlayerSlot()); b.writeVarInt(m.targetPlayerSlot()); },
                b -> new MovePlayerFreeSlotMessage(b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.OPEN_PLAYER_GRID_INVENTORY, unit(OpenPlayerGridInventoryMessage.INSTANCE));
        register(GridMessages.PICKUP_GROUND_ITEM_INTO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeVarInt(m.entityId()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); },
                b -> new PickupGroundItemIntoEquipmentStorageMessage(b.readVarInt(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean())
        ));
        register(GridMessages.PICKUP_GROUND_ITEM_INTO_GRID, codec(
                (m, b) -> { b.writeVarInt(m.entityId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); },
                b -> new PickupGroundItemIntoGridMessage(b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readBoolean())
        ));
        register(GridMessages.QUICK_EQUIP_EQUIPMENT_STORAGE_ENTRY, equipmentUuid(QuickEquipEquipmentStorageEntryMessage::new));
        register(GridMessages.QUICK_EQUIP_GRID_ENTRY, codec(
                (m, b) -> b.writeUUID(m.entryId()),
                b -> new QuickEquipGridEntryMessage(b.readUUID())
        ));
        register(GridMessages.QUICK_EQUIP_PLAYER_SLOT, codec(
                (m, b) -> b.writeVarInt(m.playerSlot()),
                b -> new QuickEquipPlayerSlotMessage(b.readVarInt())
        ));
        register(GridMessages.SYNC_BACKPACK_FOLDING_RULES, codec(
                (m, b) -> { b.writeVarInt(m.rules().size()); for (BackpackFoldingDefinition rule : m.rules()) rule.encode(b); },
                b -> { int count = b.readVarInt(); List<BackpackFoldingDefinition> rules = new ArrayList<>(count); for (int i = 0; i < count; i++) rules.add(BackpackFoldingDefinition.decode(b)); return new SyncBackpackFoldingRulesMessage(rules); }
        ));
        register(GridMessages.SYNC_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeEnum(m.slot()); EquipmentStorageData.STREAM_CODEC.encode(b, m.storage()); },
                b -> new SyncEquipmentStorageMessage(b.readEnum(EquipmentSlot.class), EquipmentStorageData.STREAM_CODEC.decode(b))
        ));
        register(GridMessages.SYNC_GRID_INVENTORY, codec(
                (m, b) -> m.data().encode(b),
                b -> new SyncGridInventoryMessage(GridInventoryData.decode(b))
        ));
        register(GridMessages.SYNC_ITEM_SIZE_RULES, codec(
                (m, b) -> { b.writeVarInt(m.rules().size()); for (GridItemSizeRule rule : m.rules()) rule.encode(b); },
                b -> { int count = b.readVarInt(); List<GridItemSizeRule> rules = new ArrayList<>(count); for (int i = 0; i < count; i++) rules.add(GridItemSizeRule.decode(b)); return new SyncItemSizeRulesMessage(rules); }
        ));
        register(GridMessages.TOGGLE_EQUIPMENT_STORAGE_ENTRY_BACKPACK_FOLD, codec(
                (m, b) -> { b.writeEnum(m.slot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); },
                b -> new ToggleEquipmentStorageEntryBackpackFoldMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt(), b.readBoolean())
        ));
        register(GridMessages.TOGGLE_GRID_ENTRY_BACKPACK_FOLD, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); },
                b -> new ToggleGridEntryBackpackFoldMessage(b.readUUID(), b.readVarInt(), b.readVarInt(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY_INTO_GRID, codec(
                (m, b) -> { b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferEquipmentStorageEntryIntoGridMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); b.writeEnum(m.targetSlot()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); },
                b -> new TransferEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_GRID_ENTRY_INTO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferGridEntryIntoEquipmentStorageMessage(b.readUUID(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
    }

    private static <T extends GridMessage> void register(GridMessageType<T> type, NeoForge1211MessageCodec<T> codec) {
        CODECS.put(type, codec);
    }

    @SuppressWarnings("unchecked")
    private static <T extends GridMessage> CheckedCodec<T> codec(GridMessageType<?> type) {
        NeoForge1211MessageCodec<?> codec = CODECS.get(type);
        if (codec == null) {
            throw new IllegalStateException("Missing NeoForge 1.21.1 codec for " + type.id());
        }
        return new CheckedCodec<>((NeoForge1211MessageCodec<T>) codec);
    }

    private static <T extends GridMessage> NeoForge1211MessageCodec<T> unit(T instance) {
        return codec((message, buf) -> { }, buf -> instance);
    }

    private static <T extends GridMessage> NeoForge1211MessageCodec<T> codec(Encoder<T> encoder, Decoder<T> decoder) {
        return new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(T message, RegistryFriendlyByteBuf buf) {
                encoder.encode(message, buf);
            }

            @Override
            public T decode(RegistryFriendlyByteBuf buf) {
                return decoder.decode(buf);
            }
        };
    }

    private static NeoForge1211MessageCodec<ExtractGridEntryToPlayerSlotMessage> uuidSlotAmount(UuidSlotAmountFactory<ExtractGridEntryToPlayerSlotMessage> factory) {
        return codec((m, b) -> { b.writeUUID(m.entryId()); b.writeVarInt(m.playerSlot()); b.writeVarInt(m.amount()); },
                b -> factory.create(b.readUUID(), b.readVarInt(), b.readVarInt()));
    }

    private static NeoForge1211MessageCodec<QuickEquipEquipmentStorageEntryMessage> equipmentUuid(EquipmentUuidFactory<QuickEquipEquipmentStorageEntryMessage> factory) {
        return codec((m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); },
                b -> factory.create(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID()));
    }

    @FunctionalInterface
    private interface Encoder<T extends GridMessage> {
        void encode(T message, RegistryFriendlyByteBuf buf);
    }

    @FunctionalInterface
    private interface Decoder<T extends GridMessage> {
        T decode(RegistryFriendlyByteBuf buf);
    }

    @FunctionalInterface
    private interface UuidSlotAmountFactory<T extends GridMessage> {
        T create(UUID entryId, int playerSlot, int amount);
    }

    @FunctionalInterface
    private interface EquipmentUuidFactory<T extends GridMessage> {
        T create(EquipmentSlot slot, String containerId, UUID entryId);
    }

    private record CheckedCodec<T extends GridMessage>(NeoForge1211MessageCodec<T> delegate) {
        @SuppressWarnings("unchecked")
        void encodeUnchecked(GridMessage message, RegistryFriendlyByteBuf buf) {
            delegate.encode((T) message, buf);
        }

        T decode(RegistryFriendlyByteBuf buf) {
            return delegate.decode(buf);
        }
    }
}
