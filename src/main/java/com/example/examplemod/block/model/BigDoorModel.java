package com.example.examplemod.block.model;

import com.example.examplemod.block.BigDoorBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BigDoorModel extends GeoModel<BigDoorBlockEntity> {
    @Override
    public ResourceLocation getModelResource(BigDoorBlockEntity animatable) {
        // [여기에 JSON 경로를 적습니다]
        return new ResourceLocation("examplemod", "geo/big_door.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BigDoorBlockEntity animatable) {
        // 텍스처 경로 (PNG 파일 위치)
        return new ResourceLocation("examplemod", "textures/block/big_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BigDoorBlockEntity animatable) {
        // 애니메이션 경로 (있다면)
        return new ResourceLocation("examplemod", "animations/big_door.animation.json");
    }
}