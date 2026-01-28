package com.example.examplemod.Hunger;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public class HungerCapability {
    private float hunger = 500.0f;
    private final float maxHunger = 500.0f;
    private float savedHealth = 100.0f;

    public float getHunger() { return hunger; }

    public void setHunger(float value) {
        this.hunger = Math.max(0, Math.min(value, maxHunger));
    }

    public void addHunger(float value) {
        setHunger(this.hunger + value);
    }

    public void subHunger(float value) {
        setHunger(this.hunger - value);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putFloat("custom_hunger", hunger);
        nbt.putFloat("saved_health", savedHealth);
    }

    public void loadNBTData(CompoundTag nbt) {
        hunger = nbt.getFloat("custom_hunger");
        if(nbt.contains("saved_health")) { // 데이터가 있을 때만 불러오기
            savedHealth = nbt.getFloat("saved_health");
        }
    }
    public float getSavedHealth() {
        return savedHealth;
    }

    public void setSavedHealth(float value) {
        this.savedHealth = value;
    }
}