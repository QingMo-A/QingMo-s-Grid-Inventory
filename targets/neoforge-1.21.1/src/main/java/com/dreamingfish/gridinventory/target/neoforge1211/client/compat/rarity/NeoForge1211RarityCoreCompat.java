package com.dreamingfish.gridinventory.target.neoforge1211.client.compat.rarity;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.client.compat.rarity.GridItemRarityCompat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class NeoForge1211RarityCoreCompat extends GridItemRarityCompat {
    private static final String MODID = "raritycore";

    private final boolean loaded;
    private final Method getRarity;
    private final Method getRarityColor;
    private final Method isBorderRenderingEnabled;
    private final Method isLevelRendererEnabled;
    private final Method hasConfiguredRarity;
    private final Method rarityClientConfigGetInstance;
    private final Method rarityClientConfigGetColor;
    private final Method rarityClientConfigIsRendererEnabled;
    private final Method isSkipUnconfiguredItems;
    private final Method setRenderingTooltipItem;
    private boolean disabled;
    private boolean warned;

    public NeoForge1211RarityCoreCompat() {
        if (!ModList.get().isLoaded(MODID)) {
            this.loaded = false;
            this.getRarity = null;
            this.getRarityColor = null;
            this.isBorderRenderingEnabled = null;
            this.isLevelRendererEnabled = null;
            this.hasConfiguredRarity = null;
            this.rarityClientConfigGetInstance = null;
            this.rarityClientConfigGetColor = null;
            this.rarityClientConfigIsRendererEnabled = null;
            this.isSkipUnconfiguredItems = null;
            this.setRenderingTooltipItem = null;
            return;
        }

        Method getRarityMethod = null;
        Method getRarityColorMethod = null;
        Method isBorderRenderingEnabledMethod = null;
        Method isLevelRendererEnabledMethod = null;
        Method hasConfiguredRarityMethod = null;
        Method rarityClientConfigGetInstanceMethod = null;
        Method rarityClientConfigGetColorMethod = null;
        Method rarityClientConfigIsRendererEnabledMethod = null;
        Method isSkipUnconfiguredItemsMethod = null;
        Method setRenderingTooltipItemMethod = null;
        boolean available = false;
        try {
            Class<?> api = Class.forName("org.yanbwe.raritycore.api.RarityCoreAPI");
            getRarityMethod = api.getMethod("getRarity", ItemStack.class);
            try {
                getRarityColorMethod = api.getMethod("getRarityColor", int.class);
            } catch (ReflectiveOperationException ignored) {
                getRarityColorMethod = null;
            }
            try {
                isBorderRenderingEnabledMethod = api.getMethod("isBorderRenderingEnabled");
            } catch (ReflectiveOperationException ignored) {
                isBorderRenderingEnabledMethod = null;
            }
            try {
                isLevelRendererEnabledMethod = api.getMethod("isLevelRendererEnabled", int.class);
            } catch (ReflectiveOperationException ignored) {
                isLevelRendererEnabledMethod = null;
            }
            try {
                hasConfiguredRarityMethod = api.getMethod("hasConfiguredRarity", Item.class, ItemStack.class);
            } catch (ReflectiveOperationException ignored) {
                hasConfiguredRarityMethod = null;
            }

            try {
                Class<?> clientConfig = Class.forName("org.yanbwe.raritycore.config.ClientConfigManager");
                isSkipUnconfiguredItemsMethod = clientConfig.getMethod("isSkipUnconfiguredItems");
                if (isBorderRenderingEnabledMethod == null) {
                    isBorderRenderingEnabledMethod = clientConfig.getMethod("isEnableItemBorderRendering");
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                isSkipUnconfiguredItemsMethod = null;
            }

            if (getRarityColorMethod == null || isLevelRendererEnabledMethod == null) {
                Class<?> rarityClientConfig = Class.forName("org.yanbwe.raritycore.config.RarityClientConfig");
                rarityClientConfigGetInstanceMethod = rarityClientConfig.getMethod("getInstance");
                rarityClientConfigGetColorMethod = rarityClientConfig.getMethod("getColor", int.class);
                rarityClientConfigIsRendererEnabledMethod = rarityClientConfig.getMethod("isRendererEnabled", int.class);
            }

            try {
                Class<?> exclusions = Class.forName("org.yanbwe.raritycore.client.RarityExclusionManager");
                setRenderingTooltipItemMethod = exclusions.getMethod("setRenderingTooltipItem", boolean.class);
            } catch (ReflectiveOperationException | LinkageError ignored) {
                setRenderingTooltipItemMethod = null;
            }
            available = true;
        } catch (ReflectiveOperationException | LinkageError exception) {
            DFGridInventory.LOGGER.warn("RarityCore is loaded, but its client API could not be linked for NeoForge 1.21.1 compatibility", exception);
        }
        this.loaded = available;
        this.getRarity = getRarityMethod;
        this.getRarityColor = getRarityColorMethod;
        this.isBorderRenderingEnabled = isBorderRenderingEnabledMethod;
        this.isLevelRendererEnabled = isLevelRendererEnabledMethod;
        this.hasConfiguredRarity = hasConfiguredRarityMethod;
        this.rarityClientConfigGetInstance = rarityClientConfigGetInstanceMethod;
        this.rarityClientConfigGetColor = rarityClientConfigGetColorMethod;
        this.rarityClientConfigIsRendererEnabled = rarityClientConfigIsRendererEnabledMethod;
        this.isSkipUnconfiguredItems = isSkipUnconfiguredItemsMethod;
        this.setRenderingTooltipItem = setRenderingTooltipItemMethod;
    }

    @Override
    protected @Nullable RarityVisual queryVisual(ItemStack stack) {
        if (!loaded || disabled || stack.isEmpty()) {
            return null;
        }
        try {
            if (isBorderRenderingEnabled != null && !invokeBoolean(isBorderRenderingEnabled)) {
                return null;
            }
            int rarity = invokeInt(getRarity, stack);
            if (!isRendererEnabled(rarity)) {
                return null;
            }
            if (isSkipUnconfiguredItems != null && invokeBoolean(isSkipUnconfiguredItems)
                    && hasConfiguredRarity != null && !invokeBoolean(hasConfiguredRarity, stack.getItem(), stack)) {
                return null;
            }
            return new RarityVisual(rarity, color(rarity));
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

    private boolean isRendererEnabled(int rarity) throws ReflectiveOperationException {
        if (isLevelRendererEnabled != null) {
            return invokeBoolean(isLevelRendererEnabled, rarity);
        }
        Object config = invoke(rarityClientConfigGetInstance);
        return (Boolean) invoke(rarityClientConfigIsRendererEnabled, config, rarity);
    }

    private int color(int rarity) throws ReflectiveOperationException {
        if (getRarityColor != null) {
            return invokeInt(getRarityColor, rarity);
        }
        Object config = invoke(rarityClientConfigGetInstance);
        return (Integer) invoke(rarityClientConfigGetColor, config, rarity);
    }

    private static Object invoke(Method method, Object... arguments) throws ReflectiveOperationException {
        try {
            Object target = null;
            Object[] actualArguments = arguments;
            if (arguments.length > 0 && method.getParameterCount() == arguments.length - 1
                    && method.getDeclaringClass().isInstance(arguments[0])) {
                target = arguments[0];
                actualArguments = new Object[arguments.length - 1];
                System.arraycopy(arguments, 1, actualArguments, 0, actualArguments.length);
            }
            return method.invoke(target, actualArguments);
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
            DFGridInventory.LOGGER.warn("Disabling RarityCore NeoForge 1.21.1 compatibility after a reflective API failure", exception);
        }
    }
}
