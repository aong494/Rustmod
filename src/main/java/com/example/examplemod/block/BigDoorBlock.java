package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.context.BlockPlaceContext;

public class BigDoorBlock extends BaseEntityBlock {
    // 1. 속성을 명확하게 정의
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public BigDoorBlock(Properties properties) {
        super(properties);
        // 2. 기본 상태에도 등록
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos startPos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction right = facing.getClockWise();

        // 4x4 공간이 모두 비어있는지 확인
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                BlockPos targetPos = startPos.relative(right, x).above(y);
                BlockState targetState = level.getBlockState(targetPos);
                // 시작 지점(본인)은 제외하고 체크
                if (x == 0 && y == 0) continue;
                // 공기나 교체 가능한 블록(물, 풀 등)이 아니면 설치 불가
                if (!targetState.canBeReplaced(context)) {
                    return null; // null을 반환하면 설치가 취소됩니다.
                }
            }
        }

        return this.defaultBlockState().setValue(FACING, facing).setValue(OPEN, false);
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        Direction facing = state.getValue(FACING);
        Direction right = facing.getClockWise();

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (x == 0 && y == 0) continue;

                BlockPos dummyPos = pos.relative(right, x).above(y);

                // 공기 블록 상태에서 바로 setValue를 호출하면 튕깁니다.
                // 대신, 기본 상태(defaultBlockState)를 먼저 가져와서 값을 설정해야 합니다.
                BlockState dummyState = ModBlocks.DOOR_DUMMY.get().defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(OPEN, false);

                level.setBlock(dummyPos, dummyState, 3);

                if (level.getBlockEntity(dummyPos) instanceof DoorDummyEntity dummyEntity) {
                    dummyEntity.setMasterPos(pos);
                }
            }
        }
        level.sendBlockUpdated(pos, state, state, 3);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean newState = !state.getValue(OPEN);
            Direction facing = state.getValue(FACING);
            Direction right = facing.getClockWise();
            level.setBlock(pos, state.setValue(OPEN, newState), 3);
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    if (x == 0 && y == 0) continue;
                    BlockPos dummyPos = pos.relative(right, x).above(y);
                    BlockState dummyState = level.getBlockState(dummyPos);

                    if (dummyState.is(ModBlocks.DOOR_DUMMY.get())) {
                        level.setBlock(dummyPos, dummyState.setValue(OPEN, newState), 3);
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigDoorBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // 문이 열려있으면(OPEN == true) 충돌 박스를 비워서 통과 가능하게 만듭니다.
        return state.getValue(BigDoorBlock.OPEN) ? Shapes.empty() : Shapes.block();
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            Direction right = facing.getClockWise();

            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    if (x == 0 && y == 0) continue;
                    BlockPos targetPos = pos.relative(right, x).above(y);
                    if (level.getBlockState(targetPos).is(ModBlocks.DOOR_DUMMY.get())) {
                        level.removeBlock(targetPos, false);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            level.playSound(null, pos,
                    SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, "custom_door_break")),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}