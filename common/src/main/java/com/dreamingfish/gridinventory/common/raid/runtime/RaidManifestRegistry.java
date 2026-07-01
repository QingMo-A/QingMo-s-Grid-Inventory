package com.dreamingfish.gridinventory.common.raid.runtime;
import java.util.*;
public final class RaidManifestRegistry {
    private static final Map<Long, RaidManifest> MANIFESTS = new LinkedHashMap<>();
    private static long latest;
    private RaidManifestRegistry() {}
    public static void put(RaidManifest manifest) { MANIFESTS.put(manifest.raidId(), manifest); latest = manifest.raidId(); }
    public static Optional<RaidManifest> get(long id) { return Optional.ofNullable(MANIFESTS.get(id)); }
    public static Optional<RaidManifest> latest() { return get(latest); }
    public static void clear() { MANIFESTS.clear(); latest = 0L; }
    public static Collection<RaidManifest> all() { return List.copyOf(MANIFESTS.values()); }
    public static int size() { return MANIFESTS.size(); }
}
