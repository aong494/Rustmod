// PlayerGearCapability.java
package com.example.examplemod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerGearCapability {
    // 1. 에러가 났던 그 심볼(열쇠)을 여기서 정의합니다.
    public static final Capability<PlayerGearCapability> GEAR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    // 2. 에러가 났던 'inventory' 심볼입니다.
    public final ItemStackHandler inventory = new ItemStackHandler(2);

    // 데이터를 NBT 형식으로 변환 (저장용)
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    // NBT에서 데이터를 읽어옴 (불러오기용)
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }

    // 플레이어에게 이 저장소를 붙여주는 'Provider' 클래스
    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerGearCapability gear = new PlayerGearCapability();
        private final LazyOptional<PlayerGearCapability> optional = LazyOptional.of(() -> gear);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return GEAR_CAPABILITY.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() { return gear.serializeNBT(); }

        @Override
        public void deserializeNBT(CompoundTag nbt) { gear.deserializeNBT(nbt); }
    }
}