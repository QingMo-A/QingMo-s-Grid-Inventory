package com.dreamingfish.gridinventory.common.raid.runtime;

public record RaidTemplateApplyResult(boolean applied, boolean missing) {
    public static RaidTemplateApplyResult skipped() { return new RaidTemplateApplyResult(false, false); }
}
