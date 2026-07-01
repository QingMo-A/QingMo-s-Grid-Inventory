package com.dreamingfish.gridinventory.common.raid.runtime;

import java.util.List;

public record RaidVariantSelection(
        String groupId, String variantId, List<String> tags, List<RaidBlockPatch> patches) {
    public RaidVariantSelection {
        groupId = groupId == null ? "" : groupId;
        variantId = variantId == null ? "" : variantId;
        tags = tags == null ? List.of() : List.copyOf(tags);
        patches = patches == null ? List.of() : List.copyOf(patches);
    }
}
