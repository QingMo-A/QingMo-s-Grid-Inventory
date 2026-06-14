package com.dreamingfish.gridinventory.target.forge1201.client.compat.rarity;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.client.compat.rarity.GridItemRarityCompat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Forge1201RarityCoreCompat extends GridItemRarityCompat {
    private static final String MODID = "raritycore";

    private final boolean loaded;
    private final Method getRarity;
    private final Method getRarityColor;
    private final Method isBorderRenderingEnabled;
    private final Method isLevelRendererEnabled;
    private final Method hasConfiguredRarity;
    private final Method isSkipUnconfiguredItems;
    private final Method setRenderingTooltipItem;
    private boolean disabled;
    private boolean warned;

    public Forge1201RarityCoreCompat() {
        if (!ModList.get().isLoaded(MODID)) {
            this.loaded = false;
            this.getRarity = null;
            this.getRarityColor = null;
            this.isBorderRenderingEnabled = null;
            this.isLevelRendererEnabled = null;
            this.hasConfiguredRarity = null;
            this.isSkipUnconfiguredItems = null;
            this.setRenderingTooltipItem = null;
            return;
        }

        Method getRarityMethod = null;
        Method getRarityColorMethod = null;
        Method isBorderRenderingEnabledMethod = null;
        Method isLevelRendererEnabledMethod = null;
        Method hasConfiguredRarityMethod = null;
        Method isSkipUnconfiguredItemsMethod = null;
        Method setRenderingTooltipItemMethod = null;
        boolean available = false;
        try {
            Class<?> api = Class.forName("org.yanbwe.raritycore.api.RarityCoreAPI");
            getRarityMethod = api.getMethod("getRarity", ItemStack.class);
            getRarityColorMethod = api.getMethod("getRarityColor", int.class);
            isBorderRenderingEnabledMethod = api.getMethod("isBorderRenderingEnabled");
            isLevelRendererEnabledMethod = api.getMethod("isLevelRendererEnabled", int.class);
            hasConfiguredRarityMethod = api.getMethod("hasConfiguredRarity", Item.class, ItemStack.class);

            try {
                Class<?> clientConfig = Class.forName("org.yanbwe.raritycore.config.ClientConfigManager");
                isSkipUnconfiguredItemsMethod = clientConfig.getMethod("isSkipUnconfiguredItems");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                isSkipUnconfiguredItemsMethod = null;
            }

            try {
                Class<?> exclusions = Class.forName("org.yanbwe.raritycore.client.RarityExclusionManager");
                setRenderingTooltipItemMethod = exclusions.getMethod("setRenderingTooltipItem", boolean.class);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                setRenderingTooltipItemMethod = null;
            }
            available = true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            DFGridInventory.LOGGER.warn("RarityCore is loaded, but its client API could not be linked for Forge 1.20.1 compatibility", exception);
        }
        this.loaded = available;
        this.getRarity = getRarityMethod;
        this.getRarityColor = getRarityColorMethod;
        this.isBorderRenderingEnabled = isBorderRenderingEnabledMethod;
        this.isLevelRendererEnabled = isLevelRendererEnabledMethod;
        this.hasConfiguredRarity = hasConfiguredRarityMethod;
        this.isSkipUnconfiguredItems = isSkipUnconfiguredItemsMethod;
        this.setRenderingTooltipItem = setRenderingTooltipItemMethod;
    }

    @Override
    protected @Nullable RarityVisual queryVisual(ItemStack stack) {
        if (!loaded || disabled || stack.isEmpty()) {
            return null;
        }
        try {
            if (!invokeBoolean(isBorderRenderingEnabled)) {
                return null;
            }
            int rarity = invokeInt(getRarity, stack);
            if (!invokeBoolean(isLevelRendererEnabled, rarity)) {
                return null;
            }
            if (isSkipUnconfiguredItems != null && invokeBoolean(isSkipUnconfiguredItems)
                    && !invokeBoolean(hasConfiguredRarity, stack.getItem(), stack)) {
                return null;
            }
            return new RarityVisual(rarity, invokeInt(getRarityColor, rarity));
        } catch (ReflectiveOperationException | LinkageError exception) {
            warnAndDisable(exception);
            return null;
        }
    }

    @Override
    protected void setAutomaticBorderSuppressed(boolean suppressed) {
        if (!loaded || disabled || setRenderingTooltipItem == null) {
            return;
        }
        try {
            setRenderingTooltipItem.invoke(null, suppressed);
        } catch (ReflectiveOperationException | LinkageError exception) {
            warnAndDisable(exception);
        }
    }

    private static boolean invokeBoolean(Method method, Object... arguments) throws ReflectiveOperationException {
        return (Boolean) invoke(method, arguments);
    }

    private static int invokeInt(Method method, Object... arguments) throws ReflectiveOperationException {
        return (Integer) invoke(method, arguments);
    }

    private static Object invoke(Method method, Object... arguments) throws ReflectiveOperationException {
        try {
            return method.invoke(null, arguments);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw exception;
        }
    }

    private void warnAndDisable(Throwable exception) {
        disabled = true;
        if (!warned) {
            warned = true;
            DFGridInventory.LOGGER.warn("Disabling RarityCore Forge 1.20.1 compatibility after a reflective API failure", exception);
        }
    }
}
