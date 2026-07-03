package com.dreamingfish.gridinventory.common.raid.runtime;

import net.minecraft.core.BlockPos;

import java.util.List;
import com.dreamingfish.gridinventory.common.raid.config.*;

public record RaidExtractionActivation(
        String id,
        String node,
        BlockPos localPos,
        double radius,
        String displayName,
        List<String> tags, RaidExtractionAvailabilityConfig availability,
        RaidExtractionTriggerConfig trigger, RaidExtractionTimerConfig timer,
        int useLimit, String consumeUseOn) {
    public RaidExtractionActivation {
        id = id == null ? "" : id;
        node = node == null ? "" : node;
        localPos = localPos == null ? BlockPos.ZERO : localPos;
        radius = radius > 0 ? radius : 3.0D;
        displayName = displayName == null ? "" : displayName;
        tags = tags == null ? List.of() : List.copyOf(tags);
        availability = availability == null ? RaidExtractionAvailabilityConfig.always() : availability;
        trigger = trigger == null ? RaidExtractionTriggerConfig.none() : trigger;
        timer = timer == null ? RaidExtractionTimerConfig.defaultPlayer() : timer;
        consumeUseOn = consumeUseOn == null || consumeUseOn.isBlank() ? "trigger" : consumeUseOn;
    }

    public RaidExtractionActivation(String id, String node, BlockPos localPos, double radius,
                                    String displayName, List<String> tags) {
        this(id, node, localPos, radius, displayName, tags,
                RaidExtractionAvailabilityConfig.always(), RaidExtractionTriggerConfig.none(),
                RaidExtractionTimerConfig.defaultPlayer(), -1, "trigger");
    }
}
