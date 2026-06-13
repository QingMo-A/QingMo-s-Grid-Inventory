package com.dreamingfish.gridinventory.target.forge1201.mixin.client;

import com.dreamingfish.gridinventory.client.access.SlotPositionAccessor;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor extends SlotPositionAccessor {
    @Mutable
    @Accessor("x")
    @Override
    void df_grid_inventory$setX(int x);

    @Mutable
    @Accessor("y")
    @Override
    void df_grid_inventory$setY(int y);
}
