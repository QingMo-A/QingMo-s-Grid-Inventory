package com.dreamingfish.gridinventory.target.neoforge1211.protocol;

import com.dreamingfish.gridinventory.api.BackpackFoldingDefinition;
import com.dreamingfish.gridinventory.api.GridItemSizeRule;
import com.dreamingfish.gridinventory.common.data.EquipmentStorageData;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
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

    public static void encode(GridMessage message, RegistryFriendlyByteBuf buf) {
        encode(cast(message.type()), message, buf);
    }

    public static <T extends GridMessage> T decode(GridMessageType<T> type, RegistryFriendlyByteBuf buf) {
        return type.messageClass().cast(codec(type).decode(buf));
    }

    public static void registerAll() {
        register(GridMessages.DROP_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); },
                b -> new DropEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID())
        ));
        register(GridMessages.DROP_GRID_ENTRY, codec(
                (m, b) -> b.writeUUID(m.entryId()),
                b -> new DropGridEntryMessage(b.readUUID())
        ));
        register(GridMessages.DROP_NESTED_GRID_ENTRY, codec(
                (m, b) -> { writePath(b, m.sourceOwnerPath()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); },
                b -> new DropNestedGridEntryMessage(readPath(b), b.readUtf(), b.readUUID())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new ExtractCurioToEquipmentStorageMessage(b.readUtf(), b.readVarInt(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_GRID, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new ExtractCurioToGridMessage(b.readUtf(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_NESTED_GRID, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); writePath(b, m.targetOwnerPath()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new ExtractCurioToNestedGridMessage(b.readUtf(), b.readVarInt(), readPath(b), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.EXTRACT_CURIO_TO_PLAYER_SLOT, codec(
                (m, b) -> { b.writeUtf(m.identifier()); b.writeVarInt(m.index()); b.writeVarInt(m.targetPlayerSlot()); },
                b -> new ExtractCurioToPlayerSlotMessage(b.readUtf(), b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.EXTRACT_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.playerSlot()); b.writeVarInt(m.amount()); },
                b -> new ExtractEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.EXTRACT_GRID_ENTRY_TO_PLAYER_SLOT, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(ExtractGridEntryToPlayerSlotMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeUUID(message.entryId());
                buf.writeVarInt(message.playerSlot());
                buf.writeVarInt(message.amount());
            }

            @Override
            public ExtractGridEntryToPlayerSlotMessage decode(RegistryFriendlyByteBuf buf) {
                return new ExtractGridEntryToPlayerSlotMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt());
            }
        });
        register(GridMessages.EXTRACT_NESTED_GRID_ENTRY_TO_PLAYER_SLOT, codec(
                (m, b) -> { writePath(b, m.sourceOwnerPath()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.playerSlot()); b.writeVarInt(m.amount()); },
                b -> new ExtractNestedGridEntryToPlayerSlotMessage(readPath(b), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.EXTRACT_TO_PLAYER_INVENTORY, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(ExtractToPlayerInventoryMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeUUID(message.entryId());
                buf.writeVarInt(message.amount());
            }

            @Override
            public ExtractToPlayerInventoryMessage decode(RegistryFriendlyByteBuf buf) {
                return new ExtractToPlayerInventoryMessage(buf.readUUID(), buf.readVarInt());
            }
        });
        register(GridMessages.INSERT_EQUIPMENT_STORAGE_ENTRY_INTO_CURIO, codec(
                (m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); b.writeUtf(m.identifier()); b.writeVarInt(m.index()); },
                b -> new InsertEquipmentStorageEntryIntoCurioMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readUtf(), b.readVarInt())
        ));
        register(GridMessages.INSERT_FROM_PLAYER_INVENTORY, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(InsertFromPlayerInventoryMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeVarInt(message.playerSlot());
                buf.writeVarInt(message.targetX());
                buf.writeVarInt(message.targetY());
                buf.writeBoolean(message.rotated());
                buf.writeBoolean(message.quick());
                buf.writeBoolean(message.targetFolded());
            }

            @Override
            public InsertFromPlayerInventoryMessage decode(RegistryFriendlyByteBuf buf) {
                return new InsertFromPlayerInventoryMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                        buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
            }
        });
        register(GridMessages.INSERT_GRID_ENTRY_INTO_CURIO, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeUtf(m.identifier()); b.writeVarInt(m.index()); },
                b -> new InsertGridEntryIntoCurioMessage(b.readUUID(), b.readUtf(), b.readVarInt())
        ));
        register(GridMessages.INSERT_INTO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeVarInt(m.playerSlot()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new InsertIntoEquipmentStorageMessage(b.readVarInt(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.INSERT_PLAYER_SLOT_INTO_NESTED_GRID, codec(
                (m, b) -> { b.writeVarInt(m.playerSlot()); writePath(b, m.targetOwnerPath()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new InsertPlayerSlotIntoNestedGridMessage(b.readVarInt(), readPath(b), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
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
        register(GridMessages.MOVE_GRID_ENTRY, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(MoveGridEntryMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeUUID(message.entryId());
                buf.writeVarInt(message.targetX());
                buf.writeVarInt(message.targetY());
                buf.writeBoolean(message.rotated());
                buf.writeBoolean(message.targetFolded());
            }

            @Override
            public MoveGridEntryMessage decode(RegistryFriendlyByteBuf buf) {
                return new MoveGridEntryMessage(buf.readUUID(), buf.readVarInt(), buf.readVarInt(),
                        buf.readBoolean(), buf.readBoolean());
            }
        });
        register(GridMessages.MOVE_PLAYER_FREE_SLOT, codec(
                (m, b) -> { b.writeVarInt(m.sourcePlayerSlot()); b.writeVarInt(m.targetPlayerSlot()); },
                b -> new MovePlayerFreeSlotMessage(b.readVarInt(), b.readVarInt())
        ));
        register(GridMessages.OPEN_PLAYER_GRID_INVENTORY, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(OpenPlayerGridInventoryMessage message, RegistryFriendlyByteBuf buf) {
            }

            @Override
            public OpenPlayerGridInventoryMessage decode(RegistryFriendlyByteBuf buf) {
                return OpenPlayerGridInventoryMessage.INSTANCE;
            }
        });
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
        register(GridMessages.QUICK_EQUIP_NESTED_GRID_ENTRY, codec(
                (m, b) -> { writePath(b, m.sourceOwnerPath()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); },
                b -> new QuickEquipNestedGridEntryMessage(readPath(b), b.readUtf(), b.readUUID())
        ));
        register(GridMessages.QUICK_EQUIP_PLAYER_SLOT, codec(
                (m, b) -> b.writeVarInt(m.playerSlot()),
                b -> new QuickEquipPlayerSlotMessage(b.readVarInt())
        ));
        register(GridMessages.SYNC_BACKPACK_FOLDING_RULES, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(SyncBackpackFoldingRulesMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeVarInt(message.rules().size());
                for (BackpackFoldingDefinition rule : message.rules()) {
                    rule.encode(buf);
                }
            }

            @Override
            public SyncBackpackFoldingRulesMessage decode(RegistryFriendlyByteBuf buf) {
                int count = buf.readVarInt();
                List<BackpackFoldingDefinition> rules = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    rules.add(BackpackFoldingDefinition.decode(buf));
                }
                return new SyncBackpackFoldingRulesMessage(rules);
            }
        });
        register(GridMessages.SYNC_EQUIPMENT_STORAGE, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(SyncEquipmentStorageMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeEnum(message.slot());
                message.storage().encode(buf);
            }

            @Override
            public SyncEquipmentStorageMessage decode(RegistryFriendlyByteBuf buf) {
                return new SyncEquipmentStorageMessage(buf.readEnum(EquipmentSlot.class), EquipmentStorageData.decode(buf));
            }
        });
        register(GridMessages.SYNC_GRID_INVENTORY, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(SyncGridInventoryMessage message, RegistryFriendlyByteBuf buf) {
                message.data().encode(buf);
            }

            @Override
            public SyncGridInventoryMessage decode(RegistryFriendlyByteBuf buf) {
                return new SyncGridInventoryMessage(GridInventoryData.decode(buf));
            }
        });
        register(GridMessages.SYNC_ITEM_SIZE_RULES, new NeoForge1211MessageCodec<>() {
            @Override
            public void encode(SyncItemSizeRulesMessage message, RegistryFriendlyByteBuf buf) {
                buf.writeVarInt(message.rules().size());
                for (GridItemSizeRule rule : message.rules()) {
                    rule.encode(buf);
                }
            }

            @Override
            public SyncItemSizeRulesMessage decode(RegistryFriendlyByteBuf buf) {
                int count = buf.readVarInt();
                List<GridItemSizeRule> rules = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    rules.add(GridItemSizeRule.decode(buf));
                }
                return new SyncItemSizeRulesMessage(rules);
            }
        });
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
        register(GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY_INTO_NESTED_GRID, codec(
                (m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); writePath(b, m.targetOwnerPath()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferEquipmentStorageEntryIntoNestedGridMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), readPath(b), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_EQUIPMENT_STORAGE_ENTRY, codec(
                (m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); b.writeEnum(m.targetSlot()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferEquipmentStorageEntryMessage(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_GRID_ENTRY_INTO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { b.writeUUID(m.entryId()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.containerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferGridEntryIntoEquipmentStorageMessage(b.readUUID(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_GRID_ENTRY_INTO_NESTED_GRID, codec(
                (m, b) -> { b.writeUUID(m.entryId()); writePath(b, m.targetOwnerPath()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferGridEntryIntoNestedGridMessage(b.readUUID(), readPath(b), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_NESTED_GRID_ENTRY_INTO_GRID, codec(
                (m, b) -> { writePath(b, m.sourceOwnerPath()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferNestedGridEntryIntoGridMessage(readPath(b), b.readUtf(), b.readUUID(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_NESTED_GRID_ENTRY_INTO_EQUIPMENT_STORAGE, codec(
                (m, b) -> { writePath(b, m.sourceOwnerPath()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); b.writeEnum(m.equipmentSlot()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferNestedGridEntryIntoEquipmentStorageMessage(readPath(b), b.readUtf(), b.readUUID(), b.readEnum(EquipmentSlot.class), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
        register(GridMessages.TRANSFER_NESTED_GRID_ENTRY_INTO_NESTED_GRID, codec(
                (m, b) -> { writePath(b, m.sourceOwnerPath()); b.writeUtf(m.sourceContainerId()); b.writeUUID(m.entryId()); writePath(b, m.targetOwnerPath()); b.writeUtf(m.targetContainerId()); b.writeVarInt(m.targetX()); b.writeVarInt(m.targetY()); b.writeBoolean(m.rotated()); b.writeBoolean(m.targetFolded()); },
                b -> new TransferNestedGridEntryIntoNestedGridMessage(readPath(b), b.readUtf(), b.readUUID(), readPath(b), b.readUtf(), b.readVarInt(), b.readVarInt(), b.readBoolean(), b.readBoolean())
        ));
    }

    private static void writePath(RegistryFriendlyByteBuf buf, NestedContainerPath path) {
        buf.writeVarInt(path.segments().size());
        for (NestedContainerPath.Segment segment : path.segments()) {
            if (segment instanceof NestedContainerPath.GridEntrySegment gridEntry) {
                buf.writeVarInt(0);
                buf.writeUUID(gridEntry.entryId());
            } else if (segment instanceof NestedContainerPath.EquipmentEntrySegment equipmentEntry) {
                buf.writeVarInt(1);
                buf.writeEnum(equipmentEntry.slot());
                buf.writeUtf(equipmentEntry.containerId());
                buf.writeUUID(equipmentEntry.entryId());
            } else if (segment instanceof NestedContainerPath.ContainerEntrySegment containerEntry) {
                buf.writeVarInt(2);
                buf.writeUtf(containerEntry.containerId());
                buf.writeUUID(containerEntry.entryId());
            } else if (segment instanceof NestedContainerPath.PlayerSlotSegment playerSlot) {
                buf.writeVarInt(3);
                buf.writeVarInt(playerSlot.slot());
            } else if (segment instanceof NestedContainerPath.AccessorySegment accessory) {
                buf.writeVarInt(4);
                buf.writeUtf(accessory.identifier());
                buf.writeVarInt(accessory.index());
            }
        }
    }

    private static NestedContainerPath readPath(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        NestedContainerPath path = NestedContainerPath.root();
        for (int index = 0; index < count; index++) {
            int type = buf.readVarInt();
            if (type == 0) {
                path = path.gridEntry(buf.readUUID());
            } else if (type == 1) {
                path = path.equipmentEntry(buf.readEnum(EquipmentSlot.class), buf.readUtf(), buf.readUUID());
            } else if (type == 2) {
                path = path.containerEntry(buf.readUtf(), buf.readUUID());
            } else if (type == 3) {
                path = path.playerSlot(buf.readVarInt());
            } else if (type == 4) {
                path = path.accessory(buf.readUtf(), buf.readVarInt());
            } else {
                throw new IllegalStateException("Unknown nested container path segment type " + type);
            }
        }
        return path;
    }

    private static <T extends GridMessage> void register(GridMessageType<T> type, NeoForge1211MessageCodec<T> codec) {
        CODECS.put(type, codec);
    }

    @SuppressWarnings("unchecked")
    public static <T extends GridMessage> NeoForge1211MessageCodec<T> codec(GridMessageType<T> type) {
        NeoForge1211MessageCodec<?> codec = CODECS.get(type);
        if (codec == null) {
            throw new IllegalStateException("Missing NeoForge 1.21.1 codec for " + type.id());
        }
        return (NeoForge1211MessageCodec<T>) codec;
    }

    private static <T extends GridMessage> void encode(GridMessageType<T> type, GridMessage message, RegistryFriendlyByteBuf buf) {
        codec(type).encode(type.messageClass().cast(message), buf);
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

    private static NeoForge1211MessageCodec<QuickEquipEquipmentStorageEntryMessage> equipmentUuid(EquipmentUuidFactory<QuickEquipEquipmentStorageEntryMessage> factory) {
        return codec((m, b) -> { b.writeEnum(m.sourceSlot()); b.writeUtf(m.containerId()); b.writeUUID(m.entryId()); },
                b -> factory.create(b.readEnum(EquipmentSlot.class), b.readUtf(), b.readUUID()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends GridMessage> GridMessageType<T> cast(GridMessageType<?> type) {
        return (GridMessageType<T>) type;
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
    private interface EquipmentUuidFactory<T extends GridMessage> {
        T create(EquipmentSlot slot, String containerId, UUID entryId);
    }
}
