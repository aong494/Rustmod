package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RustFurnaceBlock extends FurnaceBlock {
    public RustFurnaceBlock(Properties properties) {
        super(properties);
    }

    // [핵심] 이 블록이 주변 면을 가리는지 여부를 결정합니다.
    // 기본값은 1.0f(완전히 가림)인데, 이를 0.0f로 낮추거나 false를 반환하게 합니다.
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return 1.0F; // 블록 내부가 너무 어두워지는 것 방지
    }

    // 주변 블록의 면을 지우지 않도록 명시적으로 빈 모양을 반환
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return Shapes.empty();
    }
}