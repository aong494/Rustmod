package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        AnimationController<BigDoorBlockEntity> controller = new AnimationController<>(this, "controller", 5, state -> {
            if (this.getBlockState().getValue(BigDoorBlock.OPEN)) {
                // thenHoldOn 대신 thenLoop 사용
                return state.setAndContinue(RawAnimation.begin().thenPlay("open").thenLoop("open"));
            } else {
                // closed 애니메이션도 마찬가지로 수정
                return state.setAndContinue(RawAnimation.begin().thenPlay("closed").thenLoop("closed"));
            }
        });

        // 사운드 핸들러 부분 (이전 답변과 동일)
        controller.setSoundKeyframeHandler(event -> {
            String soundName = event.getKeyframeData().getSound();
            if (this.level != null && this.level.isClientSide) {
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                        SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, soundName)),
                        SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
        });

        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    @Override
    public net.minecraft.world.phys.AABB getRenderBoundingBox() {
        // 본체 위치(worldPosition)를 기준으로 4x4x1 범위를 렌더링 영역으로 설정합니다.
        // 좌표는 본인의 모델 방향(x, y, z)에 맞춰 조정이 필요할 수 있습니다.
        return new net.minecraft.world.phys.AABB(worldPosition).inflate(4.0, 4.0, 1.0);
    }
}