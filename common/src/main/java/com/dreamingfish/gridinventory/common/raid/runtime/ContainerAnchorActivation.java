package com.dreamingfish.gridinventory.common.raid.runtime;
import net.minecraft.core.BlockPos;
public record ContainerAnchorActivation(String anchorId, String zoneId, String groupId, BlockPos localPos,
                                        String containerType, int pointBudget, double qualityMultiplier, long lootSeed) {}
