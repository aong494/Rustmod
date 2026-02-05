package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ToolCupboardBlock extends Block implements EntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public ToolCupboardBlock(Properties properties) {
        super(properties.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return new ToolCupboardBlockEntity(pos, state);
        }
        return null;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return RenderShape.INVISIBLE;
        }
        return RenderShape.MODEL;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // FACING과 HALF를 모두 넣어줘야 합니다.
        builder.add(FACING, HALF);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockPos chestPos = (state.getValue(HALF) == DoubleBlockHalf.UPPER) ? pos.below() : pos;
            BlockEntity be = level.getBlockEntity(chestPos);
            if (be instanceof net.minecraft.world.level.block.entity.TrappedChestBlockEntity) {
                player.openMenu((net.minecraft.world.level.block.entity.TrappedChestBlockEntity)be);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    // 설치 시 방향 설정 로직 (이게 있어야 facing 속성 에러도 안 납니다)
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (pos.getY() < context.getLevel().getMaxBuildHeight() - 1 && context.getLevel().getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER);
        }
        return null;
    }
    // 블록 배치 시 위쪽 칸도 함께 설치되도록 설정
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    // 한쪽이 부서지면 나머지 한쪽도 부서지게 설정
    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.is(this) || (half == DoubleBlockHalf.UPPER && otherState.is(Blocks.TRAPPED_CHEST))) {
                world.destroyBlock(otherPos, false);
            }
        }
        super.playerWillDestroy(world, pos, state, player);
    }
    // 덫 상자처럼 레드스톤 신호 세기 출력 (열린 인원수만큼 신호 발생)
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos targetPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        return Mth.clamp(ChestBlockEntity.getOpenCount(level, targetPos), 0, 15);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return Shapes.empty();
        }
        return Shapes.block(); // 아래칸은 꽉 찬 블록
    }
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // 하단 블록일 때만 아이템 드랍 실행 (상단은 BlockEntity가 없으므로)
            if (state.hasProperty(HALF) && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity blockentity = world.getBlockEntity(pos);
                if (blockentity instanceof Container container) {
                    Containers.dropContents(world, pos, container);
                }
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
    @Nullable
    @Override
    public MenuProvider getMenuProvider(BlockState state, Level world, BlockPos pos) {
        BlockPos targetPos = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        BlockEntity blockEntity = world.getBlockEntity(targetPos);
        return blockEntity instanceof ToolCupboardBlockEntity ? (MenuProvider) blockEntity : null;
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER == (direction == Direction.UP))) {
            if (neighborState.is(this) || neighborState.is(Blocks.TRAPPED_CHEST)) {
                return state;
            }
            return Blocks.AIR.defaultBlockState();
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
}