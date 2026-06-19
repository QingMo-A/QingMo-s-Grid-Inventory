package com.dreamingfish.gridinventory.common.inventory;

import net.minecraft.world.entity.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record NestedContainerPath(List<Segment> segments) {
    public NestedContainerPath {
        segments = List.copyOf(segments);
    }

    public static NestedContainerPath root() {
        return new NestedContainerPath(List.of());
    }

    public NestedContainerPath gridEntry(UUID entryId) {
        return append(new GridEntrySegment(entryId));
    }

    public NestedContainerPath equipmentEntry(EquipmentSlot slot, String containerId, UUID entryId) {
        return append(new EquipmentEntrySegment(slot, containerId, entryId));
    }

    public NestedContainerPath containerEntry(String containerId, UUID entryId) {
        return append(new ContainerEntrySegment(containerId, entryId));
    }

    public NestedContainerPath playerSlot(int slot) {
        return append(new PlayerSlotSegment(slot));
    }

    public NestedContainerPath accessory(String identifier, int index) {
        return append(new AccessorySegment(identifier, index));
    }

    public int depth() {
        return segments.size();
    }

    public NestedContainerPath tail() {
        if (segments.isEmpty()) {
            return this;
        }
        return new NestedContainerPath(segments.subList(1, segments.size()));
    }

    private NestedContainerPath append(Segment segment) {
        List<Segment> next = new ArrayList<>(segments);
        next.add(segment);
        return new NestedContainerPath(next);
    }

    public sealed interface Segment permits GridEntrySegment, EquipmentEntrySegment, ContainerEntrySegment, PlayerSlotSegment, AccessorySegment {
    }

    public record GridEntrySegment(UUID entryId) implements Segment {
    }

    public record EquipmentEntrySegment(EquipmentSlot slot, String containerId, UUID entryId) implements Segment {
    }

    public record ContainerEntrySegment(String containerId, UUID entryId) implements Segment {
    }

    public record PlayerSlotSegment(int slot) implements Segment {
    }

    public record AccessorySegment(String identifier, int index) implements Segment {
    }
}
