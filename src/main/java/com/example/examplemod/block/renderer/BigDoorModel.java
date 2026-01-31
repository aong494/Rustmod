package com.example.examplemod.block.renderer;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.ArmoredDoorBlock;
import com.example.examplemod.block.BigDoorBlockEntity;
import com.example.examplemod.block.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class BigDoorModel extends GeoModel<BigDoorBlockEntity> {
    // 1. 모델 파일 위치 (.geo.json)
    @Override
    public ResourceLocation getModelResource(BigDoorBlockEntity animatable) {
        return new ResourceLocation(ExampleMod.MODID, "geo/big_door.geo.json");
    }

    // 2. 텍스처 파일 위치 (.png)
    @Override
    public ResourceLocation getTextureResource(BigDoorBlockEntity animatable) {
        return new ResourceLocation(ExampleMod.MODID, "textures/block/big_door.png");
    }

    // 3. 애니메이션 파일 위치 (.animation.json)
    @Override
    public ResourceLocation getAnimationResource(BigDoorBlockEntity animatable) {
        return new ResourceLocation(ExampleMod.MODID, "animations/big_door.animation.json");
    }
}