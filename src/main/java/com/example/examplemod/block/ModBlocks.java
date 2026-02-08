package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
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
}