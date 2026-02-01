package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;

public class BigDoorBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    public BigDoorBlockEntity(BlockPos pos, BlockState state) {
        // ModBlockEntities.BIG_DOOR 부분은 본인의 등록 클래스에 맞춰야 함
        super(ModBlockEntities.BIG_DOOR.get(), pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // 1. 컨트롤러 내부 로직을 먼저 정의하고 변수에 담습니다.
        AnimationController<BigDoorBlockEntity> doorController = new AnimationController<>(this, "controller", 5, state -> {
            if (this.isRemoved()) return PlayState.STOP;

            BlockState blockState = this.getBlockState();
            if (!(blockState.getBlock() instanceof BigDoorBlock)) return PlayState.STOP;

            boolean isOpen = blockState.getValue(BigDoorBlock.OPEN);

            if (isOpen) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("open"));
            } else {
                String currentAnim = state.getController().getCurrentAnimation() != null
                        ? state.getController().getCurrentAnimation().animation().name()
                        : "";
                if (currentAnim.equals("open") || currentAnim.equals("closed")) {
                    return state.setAndContinue(RawAnimation.begin().thenPlay("closed").thenLoop("idle"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
            }
        });

        doorController.setSoundKeyframeHandler(event -> {
            String soundName = event.getKeyframeData().getSound();

            if (this.level != null && this.level.isClientSide) {
                // 2. 1.20.x 버전 이상 권장 방식
                ResourceLocation soundLocation = ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, soundName);

                level.playLocalSound(
                        worldPosition.getX(),
                        worldPosition.getY(),
                        worldPosition.getZ(),
                        SoundEvent.createVariableRangeEvent(soundLocation),
                        SoundSource.BLOCKS,
                        1.0F, 1.0F,
                        false
                );
            }
        });

        // 3. 마지막에 등록
        controllers.add(doorController);
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
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
    private float health = 1000.0f; // 초기 체력

    public boolean handleDamage(float amount) {
        this.health -= amount;
        this.setChanged(); // 데이터 변경 알림

        if (this.health <= 0) {
            // 체력이 0이면 전체 문 파괴 로직 실행
            this.destroyEntireDoor();
            return true; // 파괴됨
        }

        // 플레이어에게 알림 등 추가 가능
        return false; // 아직 생존
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putFloat("DoorHealth", this.health);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.health = nbt.getFloat("DoorHealth");
    }
    public void destroyEntireDoor() {
        if (this.level == null) return;

        BlockState state = this.getBlockState();
        Direction facing = state.getValue(BigDoorBlock.FACING);
        Direction right = facing.getClockWise();

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                BlockPos targetPos = this.worldPosition.relative(right, x).above(y);
                // 마스터(0,0)만 아이템을 드롭하고 나머지는 그냥 제거
                this.level.destroyBlock(targetPos, x == 0 && y == 0);
            }
        }
    }
}