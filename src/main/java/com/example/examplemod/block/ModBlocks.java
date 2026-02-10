package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.tags.BlockTags;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExampleMod.MODID);
    // ModBlocks 클래스 상단에 전용 사운드 타입 정의
    // 2. Big Door (거대 문) - 철 블록보다 약간 더 단단함
    public static final RegistryObject<Block> BIG_DOOR = BLOCKS.register("big_door",
            () -> new BigDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 600.0f) // 철 블록(5.0)보다 조금 더 단단함
                    .noOcclusion()));
    public static final RegistryObject<Block> DOOR_DUMMY = BLOCKS.register("door_dummy",
            () -> new DoorDummyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(5.0f, 600.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Block> ARMORED_DOOR = BLOCKS.register("armored_door",
            () -> new ArmoredDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(5.0f, 600.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Block> TOOL_CUPBOARD = BLOCKS.register("tool_cupboard",
            () -> new ToolCupboardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .noOcclusion()
                    .dynamicShape()));
    public static final RegistryObject<Block> RUST_FURNACE = BLOCKS.register("rust_furnace",
            () -> new FurnaceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
                    .noOcclusion() // 1. Properties 레벨에서 투명 설정
            ) {
                // [해결책 1] 주변 바닐라 블록이 면을 숨기는 것을 방지하는 핵심 메서드
                @Override
                public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
                    return false;
                }

                // [해결책 2] 엔진이 이 블록을 '꽉 찬 블록'으로 판단하지 않게 함
                @Override
                public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
                    return Shapes.empty();
                }

                // [해결책 3] 시각적/빛 계산 시 빈 공간으로 인식하게 함
                @Override
                public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
                    return Shapes.empty();
                }

                // [해결책 4] 빛 투과 허용
                @Override
                public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
                    return true;
                }
            });
}