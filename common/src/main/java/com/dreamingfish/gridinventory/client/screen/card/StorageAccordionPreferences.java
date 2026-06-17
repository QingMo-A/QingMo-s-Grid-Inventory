package com.dreamingfish.gridinventory.client.screen.card;

import com.dreamingfish.gridinventory.DFGridInventory;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class StorageAccordionPreferences {
    private static final String FILE_NAME = "df_grid_inventory-client-ui.properties";
    private static final String PREFIX = "storage_card.";
    private static final Properties PROPERTIES = new Properties();
    private static boolean loaded;

    private StorageAccordionPreferences() {
    }

    public static boolean expanded(String key, boolean fallback) {
        load();
        String value = PROPERTIES.getProperty(PREFIX + key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    public static void setExpanded(String key, boolean expanded) {
        load();
        PROPERTIES.setProperty(PREFIX + key, Boolean.toString(expanded));
        save();
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path path = path();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try (InputStream input = Files.newInputStream(path)) {
            PROPERTIES.load(input);
        } catch (IOException exception) {
            DFGridInventory.LOGGER.warn("Failed to load grid inventory UI preferences from {}", path, exception);
        }
    }

    private static void save() {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream output = Files.newOutputStream(path)) {
                PROPERTIES.store(output, "DF Grid Inventory client UI preferences");
            }
        } catch (IOException exception) {
            DFGridInventory.LOGGER.warn("Failed to save grid inventory UI preferences to {}", path, exception);
        }
    }

    private static Path path() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE_NAME);
    }
}
