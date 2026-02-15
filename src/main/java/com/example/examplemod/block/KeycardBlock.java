package com.example.examplemod.block;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class KeycardBlock extends Block {
    // 블록벤치에서 가져온 좌표값 그대로 적용
    protected static final VoxelShape SHAPE = Block.box(2.25D, 0.0D, 5.0D, 12.0D, 0.25D, 12.0D);

    public KeycardBlock(Properties properties) {
        // noOcclusion()은 필수입니다. 이걸 안 하면 얇은 카드 주변이 검게 보이거나 투명해질 수 있습니다.
        super(properties.noOcclusion());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}