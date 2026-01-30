package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BigDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public BigDoorBlockEntity(BlockPos pos, BlockState state) {
        // ModBlockEntities.BIG_DOOR 부분은 본인의 등록 클래스에 맞춰야 함
        super(ModBlockEntities.BIG_DOOR.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            // thenHoldLastState() 대신 thenPlay를 사용하고, 애니메이션 파일 자체에서 loop: false 설정을 하면 됩니다.
            if (this.getBlockState().getValue(BigDoorBlock.OPEN)) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("open"));
            }
            return state.setAndContinue(RawAnimation.begin().thenPlay("closed"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}