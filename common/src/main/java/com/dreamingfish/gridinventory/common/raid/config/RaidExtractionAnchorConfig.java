package com.dreamingfish.gridinventory.common.raid.config;

import net.minecraft.core.BlockPos;

import java.util.List;

public record RaidExtractionAnchorConfig(
        String id,
        String node,
        int[] pos,
        double radius,
        int weight,
        boolean enabled,
        boolean alwaysActive,
        List<String> requiresTags,
        List<String> forbiddenTags,
        String displayName,
        List<String> tags,
        RaidExtractionAvailabilityConfig availability,
        RaidExtractionTriggerConfig trigger,
        RaidExtractionTimerConfig timer,
        int useLimit,
        String consumeUseOn) {
    public RaidExtractionAnchorConfig {
        id = id == null ? "" : id;
        node = node == null ? "" : node;
        pos = pos == null ? null : pos.clone();
        requiresTags = requiresTags == null ? List.of() : List.copyOf(requiresTags);
        forbiddenTags = forbiddenTags == null ? List.of() : List.copyOf(forbiddenTags);
        displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
        availability = availability == null ? RaidExtractionAvailabilityConfig.always() : availability;
        trigger = trigger == null ? RaidExtractionTriggerConfig.none() : trigger;
        timer = timer == null ? RaidExtractionTimerConfig.defaultPlayer() : timer;
        consumeUseOn = consumeUseOn == null || consumeUseOn.isBlank() ? "trigger" : consumeUseOn;
    }

    public BlockPos localBlockPos() {
        return pos != null && pos.length == 3 ? new BlockPos(pos[0], pos[1], pos[2]) : BlockPos.ZERO;
    }

    public double effectiveRadius() {
        return radius > 0 ? radius : 3.0D;
    }

    public int effectiveWeight() {
        return weight > 0 ? weight : 1;
    }
}
