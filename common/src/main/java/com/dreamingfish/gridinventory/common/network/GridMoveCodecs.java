package com.dreamingfish.gridinventory.common.network;

import com.dreamingfish.gridinventory.common.inventory.GridItemSource;
import com.dreamingfish.gridinventory.common.inventory.GridItemTarget;
import com.dreamingfish.gridinventory.common.inventory.GridMoveOptions;
import com.dreamingfish.gridinventory.common.inventory.NestedContainerPath;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.UUID;

public final class GridMoveCodecs {
    private static final int MAX_PATH_SEGMENTS = 16;
    private static final int MAX_STRING_LENGTH = 128;

    private GridMoveCodecs() {
    }

    public static void writeOptions(FriendlyByteBuf buf, GridMoveOptions options) {
        buf.writeVarInt(options.count());
        buf.writeBoolean(options.rotated());
        buf.writeBoolean(options.targetFolded());
    }

    public static GridMoveOptions readOptions(FriendlyByteBuf buf) {
        return new GridMoveOptions(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void writeSource(FriendlyByteBuf buf, GridItemSource source) {
        if (source instanceof GridItemSource.PlayerSlot value) {
            buf.writeVarInt(0);
            buf.writeVarInt(value.slot());
        } else if (source instanceof GridItemSource.MenuGridEntry value) {
            buf.writeVarInt(1);
            buf.writeUUID(value.entryId());
        } else if (source instanceof GridItemSource.EquipmentStorageEntry value) {
            buf.writeVarInt(2);
            buf.writeEnum(value.slot());
            buf.writeUtf(value.containerId(), MAX_STRING_LENGTH);
            buf.writeUUID(value.entryId());
        } else if (source instanceof GridItemSource.NestedGridEntry value) {
            buf.writeVarInt(3);
            writeNestedPath(buf, value.ownerPath());
            buf.writeUUID(value.entryId());
        } else if (source instanceof GridItemSource.NestedEquipmentStorageEntry value) {
            buf.writeVarInt(4);
            writeNestedPath(buf, value.ownerPath());
            buf.writeUtf(value.containerId(), MAX_STRING_LENGTH);
            buf.writeUUID(value.entryId());
        } else if (source instanceof GridItemSource.AccessorySlot value) {
            buf.writeVarInt(5);
            buf.writeUtf(value.identifier(), MAX_STRING_LENGTH);
            buf.writeVarInt(value.index());
        } else if (source instanceof GridItemSource.GroundItem value) {
            buf.writeVarInt(6);
            buf.writeVarInt(value.entityId());
        } else {
            throw new DecoderException("Unsupported grid item source: " + source);
        }
    }

    public static GridItemSource readSource(FriendlyByteBuf buf) {
        int type = buf.readVarInt();
        return switch (type) {
            case 0 -> new GridItemSource.PlayerSlot(buf.readVarInt());
            case 1 -> new GridItemSource.MenuGridEntry(buf.readUUID());
            case 2 -> new GridItemSource.EquipmentStorageEntry(buf.readEnum(EquipmentSlot.class),
                    buf.readUtf(MAX_STRING_LENGTH), buf.readUUID());
            case 3 -> new GridItemSource.NestedGridEntry(readNestedPath(buf), buf.readUUID());
            case 4 -> new GridItemSource.NestedEquipmentStorageEntry(readNestedPath(buf),
                    buf.readUtf(MAX_STRING_LENGTH), buf.readUUID());
            case 5 -> new GridItemSource.AccessorySlot(buf.readUtf(MAX_STRING_LENGTH), buf.readVarInt());
            case 6 -> new GridItemSource.GroundItem(buf.readVarInt());
            default -> throw new DecoderException("Invalid grid item source type: " + type);
        };
    }

    public static void writeTarget(FriendlyByteBuf buf, GridItemTarget target) {
        if (target instanceof GridItemTarget.PlayerSlot value) {
            buf.writeVarInt(0);
            buf.writeVarInt(value.slot());
        } else if (target instanceof GridItemTarget.MenuGridPlacement value) {
            buf.writeVarInt(1);
            writePlacement(buf, value.x(), value.y(), value.rotated(), value.folded());
        } else if (target instanceof GridItemTarget.EquipmentStoragePlacement value) {
            buf.writeVarInt(2);
            buf.writeEnum(value.slot());
            buf.writeUtf(value.containerId(), MAX_STRING_LENGTH);
            writePlacement(buf, value.x(), value.y(), value.rotated(), value.folded());
        } else if (target instanceof GridItemTarget.NestedGridPlacement value) {
            buf.writeVarInt(3);
            writeNestedPath(buf, value.ownerPath());
            writePlacement(buf, value.x(), value.y(), value.rotated(), value.folded());
        } else if (target instanceof GridItemTarget.NestedEquipmentStoragePlacement value) {
            buf.writeVarInt(4);
            writeNestedPath(buf, value.ownerPath());
            buf.writeUtf(value.containerId(), MAX_STRING_LENGTH);
            writePlacement(buf, value.x(), value.y(), value.rotated(), value.folded());
        } else if (target instanceof GridItemTarget.AccessorySlot value) {
            buf.writeVarInt(5);
            buf.writeUtf(value.identifier(), MAX_STRING_LENGTH);
            buf.writeVarInt(value.index());
        } else {
            throw new DecoderException("Unsupported grid item target: " + target);
        }
    }

    public static GridItemTarget readTarget(FriendlyByteBuf buf) {
        int type = buf.readVarInt();
        return switch (type) {
            case 0 -> new GridItemTarget.PlayerSlot(buf.readVarInt());
            case 1 -> new GridItemTarget.MenuGridPlacement(buf.readVarInt(), buf.readVarInt(),
                    buf.readBoolean(), buf.readBoolean());
            case 2 -> new GridItemTarget.EquipmentStoragePlacement(buf.readEnum(EquipmentSlot.class),
                    buf.readUtf(MAX_STRING_LENGTH), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(),
                    buf.readBoolean());
            case 3 -> new GridItemTarget.NestedGridPlacement(readNestedPath(buf), buf.readVarInt(),
                    buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
            case 4 -> new GridItemTarget.NestedEquipmentStoragePlacement(readNestedPath(buf),
                    buf.readUtf(MAX_STRING_LENGTH), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(),
                    buf.readBoolean());
            case 5 -> new GridItemTarget.AccessorySlot(buf.readUtf(MAX_STRING_LENGTH), buf.readVarInt());
            default -> throw new DecoderException("Invalid grid item target type: " + type);
        };
    }

    public static void writeNestedPath(FriendlyByteBuf buf, NestedContainerPath path) {
        if (path.segments().size() > MAX_PATH_SEGMENTS) {
            throw new DecoderException("Nested path is too deep: " + path.segments().size());
        }
        buf.writeVarInt(path.segments().size());
        for (NestedContainerPath.Segment segment : path.segments()) {
            if (segment instanceof NestedContainerPath.GridEntrySegment value) {
                buf.writeVarInt(0);
                buf.writeUUID(value.entryId());
            } else if (segment instanceof NestedContainerPath.EquipmentEntrySegment value) {
                buf.writeVarInt(1);
                buf.writeEnum(value.slot());
                buf.writeUtf(value.containerId(), MAX_STRING_LENGTH);
                buf.writeUUID(value.entryId());
            } else if (segment instanceof NestedContainerPath.ContainerEntrySegment value) {
                buf.writeVarInt(2);
                buf.writeUtf(value.containerId(), MAX_STRING_LENGTH);
                buf.writeUUID(value.entryId());
            } else if (segment instanceof NestedContainerPath.PlayerSlotSegment value) {
                buf.writeVarInt(3);
                buf.writeVarInt(value.slot());
            } else if (segment instanceof NestedContainerPath.AccessorySegment value) {
                buf.writeVarInt(4);
                buf.writeUtf(value.identifier(), MAX_STRING_LENGTH);
                buf.writeVarInt(value.index());
            } else {
                throw new DecoderException("Unsupported nested path segment: " + segment);
            }
        }
    }

    public static NestedContainerPath readNestedPath(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_PATH_SEGMENTS) {
            throw new DecoderException("Invalid nested path size: " + size);
        }
        NestedContainerPath path = NestedContainerPath.root();
        for (int i = 0; i < size; i++) {
            int type = buf.readVarInt();
            path = switch (type) {
                case 0 -> path.gridEntry(buf.readUUID());
                case 1 -> path.equipmentEntry(buf.readEnum(EquipmentSlot.class), buf.readUtf(MAX_STRING_LENGTH),
                        buf.readUUID());
                case 2 -> path.containerEntry(buf.readUtf(MAX_STRING_LENGTH), buf.readUUID());
                case 3 -> path.playerSlot(buf.readVarInt());
                case 4 -> path.accessory(buf.readUtf(MAX_STRING_LENGTH), buf.readVarInt());
                default -> throw new DecoderException("Invalid nested path segment type: " + type);
            };
        }
        return path;
    }

    private static void writePlacement(FriendlyByteBuf buf, int x, int y, boolean rotated, boolean folded) {
        buf.writeVarInt(x);
        buf.writeVarInt(y);
        buf.writeBoolean(rotated);
        buf.writeBoolean(folded);
    }
}
