package com.dreamingfish.gridinventory.fabric.mixin;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.data.GridInventoryData;
import com.dreamingfish.gridinventory.fabric.platform.player.FabricGridInventoryDataHolder;
import com.dreamingfish.gridinventory.fabric.platform.player.FabricGridInventoryPlayerDataBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin implements FabricGridInventoryDataHolder {
    @Unique
    private static final String DF_GRID_INVENTORY_PLAYER_GRID_KEY = "df_grid_inventory:player_grid_inventory";

    @Unique
    private GridInventoryData df_grid_inventory$gridInventoryData;

    @Override
    public GridInventoryData df_grid_inventory$getGridInventoryData() {
        if (df_grid_inventory$gridInventoryData == null) {
            df_grid_inventory$gridInventoryData = FabricGridInventoryPlayerDataBridge.createDefault();
        }
        return df_grid_inventory$gridInventoryData;
    }

    @Override
    public void df_grid_inventory$setGridInventoryData(GridInventoryData data) {
        df_grid_inventory$gridInventoryData = data.copy();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void df_grid_inventory$addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        GridInventoryData.CODEC.encodeStart(NbtOps.INSTANCE, df_grid_inventory$getGridInventoryData())
                .resultOrPartial(DFGridInventory.LOGGER::error)
                .ifPresent(tag -> compound.put(DF_GRID_INVENTORY_PLAYER_GRID_KEY, tag));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void df_grid_inventory$readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        if (!compound.contains(DF_GRID_INVENTORY_PLAYER_GRID_KEY)) {
            return;
        }
        GridInventoryData.CODEC.parse(NbtOps.INSTANCE, compound.get(DF_GRID_INVENTORY_PLAYER_GRID_KEY))
                .resultOrPartial(DFGridInventory.LOGGER::error)
                .ifPresent(this::df_grid_inventory$setGridInventoryData);
    }
}
