package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "examplemod");

    // 4x4 문 마스터 엔티티 등록
    public static final RegistryObject<BlockEntityType<BigDoorBlockEntity>> BIG_DOOR =
            BLOCK_ENTITIES.register("big_door", () ->
                    BlockEntityType.Builder.of(BigDoorBlockEntity::new, ModBlocks.BIG_DOOR.get()).build(null));

    // 더미 블록 엔티티 등록
    public static final RegistryObject<BlockEntityType<DoorDummyEntity>> DOOR_DUMMY_ENTITY =
            BLOCK_ENTITIES.register("door_dummy", () ->
                    BlockEntityType.Builder.of(DoorDummyEntity::new, ModBlocks.DOOR_DUMMY.get()).build(null));
}