package com.dreamingfish.gridinventory.common.block;

import com.dreamingfish.gridinventory.DFGridInventory;
import com.dreamingfish.gridinventory.common.blockentity.SearchableGridContainerBlockEntity;
import com.dreamingfish.gridinventory.common.loot.ContainerLootContext;
import com.dreamingfish.gridinventory.common.loot.ContainerLootServices;
import com.dreamingfish.gridinventory.platform.GridInventoryServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import com.mojang.serialization.MapCodec;

public abstract class AbstractSearchableGridContainerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPENED = BooleanProperty.create("opened");

    protected AbstractSearchableGridContainerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPENED, false));
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, OPENED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(OPENED, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SearchableGridContainerBlockEntity(pos, state);
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        return interact(state, level, pos, player);
    }

    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                            BlockHitResult hit) {
        return interact(state, level, pos, player);
    }

    private InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean opened = state.getValue(OPENED);
        if (player.isShiftKeyDown()) {
            if (!opened) {
                return InteractionResult.CONSUME;
            }
            level.setBlock(pos, state.setValue(OPENED, false), 3);
            level.playSound(null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.CONSUME;
        }
        if (!opened) {
            level.setBlock(pos, state.setValue(OPENED, true), 3);
            level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof SearchableGridContainerBlockEntity container) {
            maybeGenerateContainerLoot(serverPlayer, pos, container);
            GridInventoryServices.menus().openSearchableGridContainer(serverPlayer, pos, container.getGridData(),
                    "container.df_grid_inventory.searchable_grid_container");
        }
        return InteractionResult.CONSUME;
    }

    private void maybeGenerateContainerLoot(ServerPlayer player, BlockPos pos,
                                            SearchableGridContainerBlockEntity container) {
        if (container.isLootGenerated()) {
            return;
        }
        try {
            if (this instanceof BasicSearchableGridContainerBlock basic) {
                SearchableGridContainerSpec spec = basic.spec();
                long seed = container.getLootSeed() != 0L ? container.getLootSeed()
                        : player.serverLevel().getSeed() ^ pos.asLong();
                String mapId = container.getMapId().isEmpty() ? "standalone" : container.getMapId();
                double qualityMultiplier = container.getQualityMultiplier() > 0.0D
                        ? container.getQualityMultiplier() : spec.qualityMultiplier();
                ContainerLootContext context = new ContainerLootContext(
                        container.getRaidId(), seed, mapId, container.getZoneId(),
                        container.getAnchorId().isEmpty() ? pos.toShortString() : container.getAnchorId(),
                        container.getContainerType().isEmpty() ? spec.containerType() : container.getContainerType(),
                        // TODO Phase 44: derive zoneTier from raid/zone manifest.
                        1, container.getPointBudget(), qualityMultiplier, spec.fallbackLootTableId());
                ContainerLootServices.generateIntoGrid(player, player.serverLevel(), pos,
                        container.getGridData(), context);
            }
        } catch (RuntimeException exception) {
            SearchableGridContainerSpec spec = this instanceof BasicSearchableGridContainerBlock basic
                    ? basic.spec() : null;
            DFGridInventory.LOGGER.error(
                    "Failed to generate searchable container loot at {} table={} containerType={} anchorId={} zoneId={}",
                    pos, spec == null ? null : spec.fallbackLootTableId(),
                    container.getContainerType().isEmpty() && spec != null
                            ? spec.containerType() : container.getContainerType(),
                    container.getAnchorId(), container.getZoneId(), exception);
        } finally {
            container.setLootGenerated(true);
            container.setChanged();
        }
    }

    @Override
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (oldState.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SearchableGridContainerBlockEntity container) {
                container.getGridData().getEntries().forEach(entry ->
                        popResource(level, pos, entry.stack().copy()));
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(oldState, level, pos, newState, movedByPiston);
    }
}
