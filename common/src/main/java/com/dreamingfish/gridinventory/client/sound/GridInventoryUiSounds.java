package com.dreamingfish.gridinventory.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class GridInventoryUiSounds {
    private GridInventoryUiSounds() {
    }

    public static void dragStart() {
        play(SoundEvents.UI_BUTTON_CLICK, 0.35F, 1.45F);
    }

    public static void dragRelease() {
        play(SoundEvents.UI_BUTTON_CLICK, 0.32F, 1.15F);
    }

    public static void equip() {
        play(SoundEvents.ARMOR_EQUIP_GENERIC, 0.45F, 1.0F);
    }

    public static void unequip() {
        play(SoundEvents.ITEM_PICKUP, 0.32F, 0.85F);
    }

    public static void foldBackpack() {
        play(SoundEvents.UI_BUTTON_CLICK, 0.38F, 0.75F);
    }

    private static void play(SoundEvent sound, float volume, float pitch) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
        }
    }

    private static void play(Holder<SoundEvent> sound, float volume, float pitch) {
        play(sound.value(), volume, pitch);
    }
}
