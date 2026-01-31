package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;

public class BigDoorBlock extends BaseEntityBlock {
    // 1. 속성을 명확하게 정의
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public BigDoorBlock(Properties properties) {
        super(properties);
        // 2. 생성자에서 기본값 등록 (매우 중요)
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(OPEN, false));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // 마스터 블록을 기준으로 4x4 범위를 더미 블록으로 채움
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (x == 0 && y == 0) continue; // 자기 자신 제외

                BlockPos dummyPos = pos.offset(x, y, 0); // 방향에 따라 offset 계산은 달라질 수 있음
                level.setBlock(dummyPos, ModBlocks.DOOR_DUMMY.get().defaultBlockState(), 3);

                // 더미 블록에게 마스터 블록의 위치를 알려줌
                if (level.getBlockEntity(dummyPos) instanceof DoorDummyEntity dummyEntity) {
                    dummyEntity.setMasterPos(pos);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean newState = !state.getValue(OPEN); // 바뀔 상태 (true 또는 false)
            level.setBlock(pos, state.setValue(OPEN, newState), 3);
            // 2. 주변 더미 블록들 상태도 한꺼번에 변경
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    if (x == 0 && y == 0) continue;
                    BlockPos dummyPos = pos.offset(x, y, 0);
                    BlockState dummyState = level.getBlockState(dummyPos);

                    // 해당 위치가 더미 블록이라면 OPEN 상태를 마스터와 똑같이 맞춤
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
            // 마스터 블록이 파괴될 때 4x4 범위의 더미 블록 제거
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 4; x++) {
                    if (x == 0 && y == 0) continue;
                    BlockPos targetPos = pos.offset(x, y, 0);
                    if (level.getBlockState(targetPos).is(ModBlocks.DOOR_DUMMY.get())) {
                        level.removeBlock(targetPos, false);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}