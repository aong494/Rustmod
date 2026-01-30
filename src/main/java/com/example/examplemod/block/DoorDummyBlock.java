package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
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
        // 더미를 클릭했을 때 마스터 블록(진짜 문)을 찾아 작동시킵니다.
        if (level.getBlockEntity(pos) instanceof DoorDummyEntity dummy) {
            BlockPos masterPos = dummy.getMasterPos();
            BlockState masterState = level.getBlockState(masterPos);

            if (masterState.getBlock() instanceof BigDoorBlock) {
                return masterState.use(level, player, hand, new BlockHitResult(hit.getLocation(), hit.getDirection(), masterPos, hit.isInside()));
            }
        }
        return InteractionResult.PASS;
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
        // BlockStateProperties.OPEN을 등록해줘야 에러가 나지 않습니다.
        builder.add(BlockStateProperties.OPEN);
    }
}