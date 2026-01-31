package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DoorDummyBlock extends Block implements EntityBlock {
    public DoorDummyBlock(Properties properties) {
        super(properties);
        // 1. 여기서 기본 상태를 지정해줘야 합니다! (중요)
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.OPEN, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof DoorDummyEntity dummy) {
            BlockPos masterPos = dummy.getMasterPos();
            BlockState masterState = level.getBlockState(masterPos);

            if (masterState.getBlock() instanceof BigDoorBlock masterBlock) {
                return masterBlock.use(masterState, level, masterPos, player, hand, hit);
            }
        }
        return InteractionResult.PASS;
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof DoorDummyEntity dummy) {
                BlockPos masterPos = dummy.getMasterPos();
                if (level.getBlockState(masterPos).is(ModBlocks.BIG_DOOR.get())) {
                    level.destroyBlock(masterPos, true); // 마스터를 부수면 마스터의 onRemove가 실행되어 나머지 더미도 다 부서짐
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DoorDummyEntity(pos, state);
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(BlockStateProperties.OPEN) ? Shapes.empty() : Shapes.block();
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.OPEN);
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 선택 박스(검은 테두리)도 안 보이게 하려면 empty() 사용
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        // 빛이 통과하게 함
        return 1.0F;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        // 하늘 빛이 통과하게 함
        return true;
    }
}