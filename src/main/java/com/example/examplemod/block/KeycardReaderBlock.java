package com.example.examplemod.block;

import com.example.examplemod.item.KeycardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class KeycardReaderBlock extends HorizontalDirectionalBlock {
    // 모델링 데이터를 기반으로 한 히트박스 (벽면에 붙는 얇은 상자 형태)
    // 북쪽(NORTH)을 기준으로 잡고 나머지 방향은 getShape에서 계산하거나 직접 정의합니다.
    protected static final VoxelShape NORTH_SHAPE = Block.box(2.0D, 3.0D, 14.0D, 14.0D, 14.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(2.0D, 3.0D, 0.0D, 14.0D, 14.0D, 2.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(14.0D, 3.0D, 2.0D, 16.0D, 14.0D, 14.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 3.0D, 2.0D, 2.0D, 14.0D, 14.0D);

    public KeycardReaderBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case SOUTH: return SOUTH_SHAPE;
            case EAST: return EAST_SHAPE;
            case WEST: return WEST_SHAPE;
            default: return NORTH_SHAPE;
        }
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // 플레이어가 바라보는 방향의 반대(벽면)를 바라보도록 설정
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}