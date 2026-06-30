package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.List;
import java.util.Map;
public record ZoneRaidState(String zoneId, int activeContainerCount, int lootBudget,
                            List<String> activeAnchorIds, Map<String, Integer> anchorBudgets) {}
