package com.dreamingfish.gridinventory.common.registry;

import com.dreamingfish.gridinventory.platform.GridInventoryRegistryBridge;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Supplier;

public final class ModSpecialItems {
    public static Supplier<Item> SECRET_DOCUMENT;
    public static Supplier<Item> TARGET_LOCATOR_MODULE;
    public static Supplier<Item> GOLDEN_COILED_SNAKE_STATUE;
    public static Supplier<Item> ORNATE_VASE;
    public static Supplier<Item> FEAST_STATUE;
    public static Supplier<Item> HIGH_ENERGY_FUEL;
    public static Supplier<Item> ANTIQUE_TEAPOT;
    public static Supplier<Item> ARRAY_LENS;

    private static boolean registered;

    private ModSpecialItems() {
    }

    public static void register(GridInventoryRegistryBridge registry) {
        if (registered) {
            return;
        }
        registered = true;
        SECRET_DOCUMENT = register(registry, "secret_document");
        TARGET_LOCATOR_MODULE = register(registry, "target_locator_module");
        GOLDEN_COILED_SNAKE_STATUE = register(registry, "golden_coiled_snake_statue");
        ORNATE_VASE = register(registry, "ornate_vase");
        FEAST_STATUE = register(registry, "feast_statue");
        HIGH_ENERGY_FUEL = register(registry, "high_energy_fuel");
        ANTIQUE_TEAPOT = register(registry, "antique_teapot");
        ARRAY_LENS = register(registry, "array_lens");
    }

    public static List<Supplier<Item>> creativeTabItems() {
        return List.of(SECRET_DOCUMENT, TARGET_LOCATOR_MODULE, GOLDEN_COILED_SNAKE_STATUE,
                ORNATE_VASE, FEAST_STATUE, HIGH_ENERGY_FUEL, ANTIQUE_TEAPOT, ARRAY_LENS);
    }

    private static Supplier<Item> register(GridInventoryRegistryBridge registry, String name) {
        return registry.registerItem(name, () -> new Item(new Item.Properties().stacksTo(1)));
    }
}
