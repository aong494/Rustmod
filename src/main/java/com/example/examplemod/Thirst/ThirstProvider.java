package com.example.examplemod.Thirst;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThirstProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<ThirstCapability> PLAYER_THIRST = CapabilityManager.get(new CapabilityToken<>() {});

    private ThirstCapability thirst = null;
    private final LazyOptional<ThirstCapability> optional = LazyOptional.of(this::createThirst);

    private ThirstCapability createThirst() {
        if (this.thirst == null) this.thirst = new ThirstCapability();
        return this.thirst;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_THIRST) return optional.cast();
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createThirst().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createThirst().loadNBTData(nbt);
    }
}