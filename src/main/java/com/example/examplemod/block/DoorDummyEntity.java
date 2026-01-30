package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DoorDummyEntity extends BlockEntity {
    private BlockPos masterPos = BlockPos.ZERO;

    public DoorDummyEntity(BlockPos pos, BlockState state) {
        // 나중에 ModBlockEntities.DOOR_DUMMY_ENTITY.get()으로 교체해야 함
        super(ModBlockEntities.DOOR_DUMMY_ENTITY.get(), pos, state);
    }

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        setChanged(); // 데이터 변경 알림
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    // 데이터 저장 (서버 껐다 켜도 유지되게)
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("MasterPos", NbtUtils.writeBlockPos(this.masterPos));
    }

    // 데이터 로드
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("MasterPos")) {
            this.masterPos = NbtUtils.readBlockPos(tag.getCompound("MasterPos"));
        }
    }
}