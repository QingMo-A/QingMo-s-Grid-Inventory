package com.dreamingfish.gridinventory.mixin.client;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("x")
    void df_grid_inventory$setX(int x);

    @Mutable
    @Accessor("y")
    void df_grid_inventory$setY(int y);
}
