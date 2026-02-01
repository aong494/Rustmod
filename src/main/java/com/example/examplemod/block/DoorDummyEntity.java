package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class DoorDummyEntity extends BlockEntity {
    private BlockPos masterPos = BlockPos.ZERO;

    public DoorDummyEntity(BlockPos pos, BlockState state) {
        // 나중에 ModBlockEntities.DOOR_DUMMY_ENTITY.get()으로 교체해야 함
        super(ModBlockEntities.DOOR_DUMMY_ENTITY.get(), pos, state);
    }

    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    // 1. 하드디스크에 저장 (서버용)
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (this.masterPos != null) {
            nbt.put("MasterPos", NbtUtils.writeBlockPos(this.masterPos));
        }
    }

    // 2. 하드디스크에서 로드 (서버용)
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("MasterPos")) {
            this.masterPos = NbtUtils.readBlockPos(nbt.getCompound("MasterPos"));
        }
    }

    // 3. 서버에서 클라이언트로 데이터 보낼 때의 NBT 설정
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        if (this.masterPos != null) {
            nbt.put("MasterPos", NbtUtils.writeBlockPos(this.masterPos));
        }
        return nbt;
    }

    // 4. 클라이언트로 패킷을 보내는 메서드
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}