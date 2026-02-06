package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ToolCupboardBlockEntity extends BlockEntity {

    public ToolCupboardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOOL_CUPBOARD_BE.get(), pos, state);
    }
}