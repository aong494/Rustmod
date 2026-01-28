package com.example.examplemod.Hunger;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HungerProvider implements ICapabilitySerializable<CompoundTag> {
    public static Capability<HungerCapability> PLAYER_HUNGER = CapabilityManager.get(new CapabilityToken<>() {});

    private HungerCapability hunger = null;
    private final LazyOptional<HungerCapability> optional = LazyOptional.of(this::createHunger);

    private HungerCapability createHunger() {
        if(this.hunger == null) {
            this.hunger = new HungerCapability();
        }
        return this.hunger;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == PLAYER_HUNGER) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createHunger().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createHunger().loadNBTData(nbt);
    }
}