package com.dreamingfish.gridinventory.common.raid.config;
import net.minecraft.resources.ResourceLocation;
public record RaidItemRequirementConfig(ResourceLocation item, int count) {
    public RaidItemRequirementConfig { count = Math.max(1, count); }
    public boolean valid() { return item != null && count > 0; }
}
