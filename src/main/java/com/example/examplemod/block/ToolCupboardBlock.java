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
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ToolCupboardBlockEntity(pos, state) : null;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
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
            // 1. 기준 좌표 (무조건 본체 위치 찾기)
            BlockPos bottomPos = (state.getValue(HALF) == DoubleBlockHalf.LOWER) ? pos : pos.below();
            BlockPos chestPos = bottomPos.above();

            // 2. 상단 블록의 엔티티(인벤토리) 가져오기
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(chestPos);

            // [수정] 덫 상자인지 체크하는 if문 대신 바로 BlockEntity 확인
            if (be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
                player.openMenu(chest);
                return InteractionResult.SUCCESS;
            } else {
                // 실패 시 콘솔 대신 플레이어에게 상세 사유 출력
                String blockName = level.getBlockState(chestPos).getBlock().toString();
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§c[도구함] 상단 감지 실패! 위치: " + chestPos.toShortString() + " | 블록: " + blockName), true);
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
    @Override
    public void playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
            BlockState otherState = world.getBlockState(otherPos);

            // [수정] 위칸이 덫 상자일 때는 파괴하지 않도록 조건을 엄격하게 제한합니다.
            // 오직 같은 모드 블록의 '반쪽'일 때만 연쇄 파괴가 일어나게 합니다.
            if (otherState.is(this)) {
                world.destroyBlock(otherPos, false);
            }
        }
        // super 호출을 제거하거나 아래와 같이 조건부로 실행하여 기본 로직의 자폭을 방지합니다.
        // super.playerWillDestroy(world, pos, state, player);
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
        return Shapes.block(); // 위아래 모두 물리적 형태를 가짐
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
        // 1. 기준 좌표 (무조건 하단 본체 찾기)
        BlockPos bottomPos = (state.getValue(HALF) == DoubleBlockHalf.LOWER) ? pos : pos.below();
        BlockPos chestPos = bottomPos.above();

        // 2. 상단 상자의 BlockEntity 확인
        BlockEntity be = world.getBlockEntity(chestPos);
        if (be instanceof MenuProvider menuProvider) {
            return menuProvider;
        }

        // 3. 만약 상단에 없다면 원래 본체의 BlockEntity 확인 (백업)
        BlockEntity bottomBe = world.getBlockEntity(bottomPos);
        return bottomBe instanceof MenuProvider ? (MenuProvider) bottomBe : null;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        return state;
    }
    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return true;
    }
}