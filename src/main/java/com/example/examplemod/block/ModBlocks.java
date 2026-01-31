package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExampleMod.MODID);

    public static final RegistryObject<Block> BIG_DOOR = BLOCKS.register("big_door",
            () -> new BigDoorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f).noOcclusion()));

    public static final RegistryObject<Block> DOOR_DUMMY = BLOCKS.register("door_dummy",
            () -> new DoorDummyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f).noOcclusion()));
    public static final RegistryObject<Block> ARMORED_DOOR = BLOCKS.register("armored_door",
            () -> new ArmoredDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(10.0f, 7.0f)
                    .noOcclusion()));
}