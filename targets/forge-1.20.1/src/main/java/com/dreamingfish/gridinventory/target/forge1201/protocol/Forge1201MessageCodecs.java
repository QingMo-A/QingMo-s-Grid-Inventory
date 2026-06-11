package com.dreamingfish.gridinventory.target.forge1201.protocol;

import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.network.DropEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.DropGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToGridMessage;
import com.dreamingfish.gridinventory.common.network.ExtractCurioToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.ExtractEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.ExtractGridEntryToPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.ExtractToPlayerInventoryMessage;
import com.dreamingfish.gridinventory.common.network.GridMessages;
import com.dreamingfish.gridinventory.common.network.InsertEquipmentStorageEntryIntoCurioMessage;
import com.dreamingfish.gridinventory.common.network.InsertFromPlayerInventoryMessage;
import com.dreamingfish.gridinventory.common.network.InsertGridEntryIntoCurioMessage;
import com.dreamingfish.gridinventory.common.network.InsertIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.InsertPlayerSlotIntoCurioMessage;
import com.dreamingfish.gridinventory.common.network.ManualPickupItemMessage;
import com.dreamingfish.gridinventory.common.network.MoveEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.MoveGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.MovePlayerFreeSlotMessage;
import com.dreamingfish.gridinventory.common.network.OpenPlayerGridInventoryMessage;
import com.dreamingfish.gridinventory.common.network.PickupGroundItemIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.PickupGroundItemIntoGridMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipGridEntryMessage;
import com.dreamingfish.gridinventory.common.network.QuickEquipPlayerSlotMessage;
import com.dreamingfish.gridinventory.common.network.SyncBackpackFoldingRulesMessage;
import com.dreamingfish.gridinventory.common.network.SyncEquipmentStorageMessage;
import com.dreamingfish.gridinventory.common.network.SyncGridInventoryMessage;
import com.dreamingfish.gridinventory.common.network.SyncItemSizeRulesMessage;
import com.dreamingfish.gridinventory.common.network.ToggleEquipmentStorageEntryBackpackFoldMessage;
import com.dreamingfish.gridinventory.common.network.ToggleGridEntryBackpackFoldMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryIntoGridMessage;
import com.dreamingfish.gridinventory.common.network.TransferEquipmentStorageEntryMessage;
import com.dreamingfish.gridinventory.common.network.TransferGridEntryIntoEquipmentStorageMessage;
import com.dreamingfish.gridinventory.protocol.GridMessage;
import com.dreamingfish.gridinventory.protocol.GridMessageType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Forge1201MessageCodecs {
    private static final Map<GridMessageType<?>, Forge1201MessageCodec<?>> CODECS = new LinkedHashMap<>();
    private static final List<GridMessageType<?>> REGISTERED_TYPES = new ArrayList<>();

    static {
        registerAll();
    }

    private Forge1201MessageCodecs() {
    }

    public static List<GridMessageType<?>> registeredTypes() {
        return List.copyOf(REGISTERED_TYPES);
    }

    public static void encode(GridMessage message, FriendlyByteBuf buf) {
        encode(cast(message.type()), message, buf);
    }

    public static <T extends GridMessage> T decode(GridMessageType<T> type, FriendlyByteBuf buf) {
        return type.messageClass().cast(codec(type).decode(buf));
    }

    private static void registerAll() {
        register(GridMessages.OPEN_PLAYER_GRID_INVENTORY, codec(
                (message, buf) -> {
                },
                buf -> OpenPlayerGridInventoryMessage.INSTANCE
        ));
        register(GridMessages.MOVE_GRID_ENTRY, codec(
                (message, buf) -> {
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new MoveGridEntryMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.INSERT_FROM_PLAYER_INVENTORY, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.playerSlot());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.quick());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new InsertFromPlayerInventoryMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        buf.readBoolean(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.EXTRACT_TO_PLAYER_INVENTORY, codec(
                (message, buf) -> {
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.amount());
                },
                buf -> new ExtractToPlayerInventoryMessage(buf.readUUID(), buf.readVarInt())
        ));
        register(GridMessages.EXTRACT_GRID_ENTRY_TO_PLAYER_SLOT, codec(
                (message, buf) -> {
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.playerSlot());
                    buf.writeVarInt(message.amount());
                },
                buf -> new ExtractGridEntryToPlayerSlotMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt())
        ));
        register(GridMessages.SYNC_GRID_INVENTORY, codec(
                (message, buf) -> message.data().encode(buf),
                buf -> new SyncGridInventoryMessage(GridInventoryData.decode(buf))
        ));
        register(GridMessages.SYNC_EQUIPMENT_STORAGE, codec(
                (message, buf) -> {
                    buf.writeEnum(message.slot());
                    message.storage().encode(buf);
                },
                buf -> new SyncEquipmentStorageMessage(buf.readEnum(EquipmentSlot.class), EquipmentStorageData.decode(buf))
        ));
        register(GridMessages.SYNC_ITEM_SIZE_RULES, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.rules().size());
                    for (GridItemSizeRule rule : message.rules()) {
                        rule.encode(buf);
                    }
                },
                buf -> {
                    int count = buf.readVarInt();
                    List<GridItemSizeRule> rules = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        rules.add(GridItemSizeRule.decode(buf));
                    }
                    return new SyncItemSizeRulesMessage(rules);
                }
        ));
        register(GridMessages.SYNC_BACKPACK_FOLDING_RULES, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.rules().size());
                    for (BackpackFoldingDefinition rule : message.rules()) {
                        rule.encode(buf);
                    }
                },
                buf -> {
                    int count = buf.readVarInt();
                    List<BackpackFoldingDefinition> rules = new ArrayList<>(count);
                    for (int i = 0; i < count; i++) {
                        rules.add(BackpackFoldingDefinition.decode(buf));
                    }
                    return new SyncBackpackFoldingRulesMessage(rules);
                }
        ));
        register(GridMessages.INSERT_INTO_EQUIPMENT_STORAGE, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.playerSlot());
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new InsertIntoEquipmentStorageMessage(buf.readVarInt(), buf.readEnum(EquipmentSlot.class), buf.readUtf(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.MOVE_EQUIPMENT_STORAGE_ENTRY, codec(
                (message, buf) -> {
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new MoveEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.EXTRACT_EQUIPMENT_STORAGE_ENTRY, codec(
                (message, buf) -> {
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.playerSlot());
                    buf.writeVarInt(message.amount());
                },
                buf -> new ExtractEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                        buf.readVarInt(), buf.readVarInt())
        ));
        register(GridMessages.TRANSFER_GRID_ENTRY_INTO_EQUIPMENT_STORAGE, codec(
                (message, buf) -> {
                    buf.writeUUID(message.entryId());
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new TransferGridEntryIntoEquipmentStorageMessage(buf.readUUID(), buf.readEnum(EquipmentSlot.class), buf.readUtf(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY_INTO_GRID, codec(
                (message, buf) -> {
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new TransferEquipmentStorageEntryIntoGridMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY, codec(
                (message, buf) -> {
                    buf.writeEnum(message.sourceSlot());
                    buf.writeUtf(message.sourceContainerId());
                    buf.writeUUID(message.entryId());
                    buf.writeEnum(message.targetSlot());
                    buf.writeUtf(message.targetContainerId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                },
                buf -> new TransferEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                        buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
        ));
        register(GridMessages.TOGGLE_EQUIPMENT_STORAGE_ENTRY_BACKPACK_FOLD, codec(
                (message, buf) -> {
                    buf.writeEnum(message.slot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                },
                buf -> new ToggleEquipmentStorageEntryBackpackFoldMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID(),
                        buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
        ));
        register(GridMessages.QUICK_EQUIP_EQUIPMENT_STORAGE_ENTRY, codec(
                (message, buf) -> {
                    buf.writeEnum(message.sourceSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                },
                buf -> new QuickEquipEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID())
        ));
        register(GridMessages.DROP_EQUIPMENT_STORAGE_ENTRY, codec(
                (message, buf) -> {
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                },
                buf -> new DropEquipmentStorageEntryMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID())
        ));
        register(GridMessages.DROP_GRID_ENTRY, codec(
                (message, buf) -> buf.writeUUID(message.entryId()),
                buf -> new DropGridEntryMessage(buf.readUUID())
        ));
        register(GridMessages.QUICK_EQUIP_GRID_ENTRY, codec(
                (message, buf) -> buf.writeUUID(message.entryId()),
                buf -> new QuickEquipGridEntryMessage(buf.readUUID())
        ));
        register(GridMessages.QUICK_EQUIP_PLAYER_SLOT, codec(
                (message, buf) -> buf.writeVarInt(message.playerSlot()),
                buf -> new QuickEquipPlayerSlotMessage(buf.readVarInt())
        ));
        register(GridMessages.TOGGLE_GRID_ENTRY_BACKPACK_FOLD, codec(
                (message, buf) -> {
                    buf.writeUUID(message.entryId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                },
                buf -> new ToggleGridEntryBackpackFoldMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
        ));
        register(GridMessages.MOVE_PLAYER_FREE_SLOT, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.sourcePlayerSlot());
                    buf.writeVarInt(message.targetPlayerSlot());
                },
                buf -> new MovePlayerFreeSlotMessage(buf.readVarInt(), buf.readVarInt())
        ));
        register(GridMessages.MANUAL_PICKUP_ITEM, codec(
                (message, buf) -> buf.writeInt(message.entityId()),
                buf -> new ManualPickupItemMessage(buf.readInt())
        ));
        register(GridMessages.PICKUP_GROUND_ITEM_INTO_GRID, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.entityId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                },
                buf -> new PickupGroundItemIntoGridMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
        ));
        register(GridMessages.PICKUP_GROUND_ITEM_INTO_EQUIPMENT_STORAGE, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.entityId());
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                },
                buf -> new PickupGroundItemIntoEquipmentStorageMessage(buf.readVarInt(), buf.readEnum(EquipmentSlot.class),
                        buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_EQUIPMENT_STORAGE, codec(
                (message, buf) -> {
                    buf.writeUtf(message.identifier());
                    buf.writeVarInt(message.index());
                    buf.writeEnum(message.equipmentSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new ExtractCurioToEquipmentStorageMessage(buf.readUtf(), buf.readVarInt(), buf.readEnum(EquipmentSlot.class),
                        buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_GRID, codec(
                (message, buf) -> {
                    buf.writeUtf(message.identifier());
                    buf.writeVarInt(message.index());
                    buf.writeVarInt(message.targetX());
                    buf.writeVarInt(message.targetY());
                    buf.writeBoolean(message.rotated());
                    buf.writeBoolean(message.targetFolded());
                },
                buf -> new ExtractCurioToGridMessage(buf.readUtf(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        buf.readBoolean(), buf.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_PLAYER_SLOT, codec(
                (message, buf) -> {
                    buf.writeUtf(message.identifier());
                    buf.writeVarInt(message.index());
                    buf.writeVarInt(message.targetPlayerSlot());
                },
                buf -> new ExtractCurioToPlayerSlotMessage(buf.readUtf(), buf.readVarInt(), buf.readVarInt())
        ));
        register(GridMessages.INSERT_EQUIPMENT_STORAGE_ENTRY_INTO_CURIO, codec(
                (message, buf) -> {
                    buf.writeEnum(message.sourceSlot());
                    buf.writeUtf(message.containerId());
                    buf.writeUUID(message.entryId());
                    buf.writeUtf(message.identifier());
                    buf.writeVarInt(message.index());
                },
                buf -> new InsertEquipmentStorageEntryIntoCurioMessage(buf.readEnum(EquipmentSlot.class), buf.readUtf(),
                        buf.readUUID(), buf.readUtf(), buf.readVarInt())
        ));
        register(GridMessages.INSERT_GRID_ENTRY_INTO_CURIO, codec(
                (message, buf) -> {
                    buf.writeUUID(message.entryId());
                    buf.writeUtf(message.identifier());
                    buf.writeVarInt(message.index());
                },
                buf -> new InsertGridEntryIntoCurioMessage(buf.readUUID(), buf.readUtf(), buf.readVarInt())
        ));
        register(GridMessages.INSERT_PLAYER_SLOT_INTO_CURIO, codec(
                (message, buf) -> {
                    buf.writeVarInt(message.sourcePlayerSlot());
                    buf.writeUtf(message.identifier());
                    buf.writeVarInt(message.index());
                },
                buf -> new InsertPlayerSlotIntoCurioMessage(buf.readVarInt(), buf.readUtf(), buf.readVarInt())
        ));
    }

    private static <T extends GridMessage> void register(GridMessageType<T> type, Forge1201MessageCodec<T> codec) {
        CODECS.put(type, codec);
        REGISTERED_TYPES.add(type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends GridMessage> Forge1201MessageCodec<T> codec(GridMessageType<T> type) {
        Forge1201MessageCodec<?> codec = CODECS.get(type);
        if (codec == null) {
            throw new IllegalStateException("Missing Forge 1.20.1 codec for " + type.id());
        }
        return (Forge1201MessageCodec<T>) codec;
    }

    private static <T extends GridMessage> void encode(GridMessageType<T> type, GridMessage message, FriendlyByteBuf buf) {
        codec(type).encode(type.messageClass().cast(message), buf);
    }

    private static <T extends GridMessage> Forge1201MessageCodec<T> codec(Encoder<T> encoder, Decoder<T> decoder) {
        return new Forge1201MessageCodec<>() {
            @Override
            public void encode(T message, FriendlyByteBuf buf) {
                encoder.encode(message, buf);
            }

            @Override
            public T decode(FriendlyByteBuf buf) {
                return decoder.decode(buf);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T extends GridMessage> GridMessageType<T> cast(GridMessageType<?> type) {
        return (GridMessageType<T>) type;
    }

    @FunctionalInterface
    private interface Encoder<T extends GridMessage> {
        void encode(T message, FriendlyByteBuf buf);
    }

    @FunctionalInterface
    private interface Decoder<T extends GridMessage> {
        T decode(FriendlyByteBuf buf);
    }
}
