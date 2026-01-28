package com.example.examplemod.Thirst;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public class ThirstCapability {
    private float thirst = 250.0f;
    private final float maxThirst = 250.0f;

    public float getThirst() { return thirst; }

    // 값을 직접 설정 (0~250 사이로 제한)
    public void setThirst(float value) {
        this.thirst = Math.max(0, Math.min(value, maxThirst));
    }

    // [추가] 수분 수치를 더함 (최대치 250 제한)
    public void addThirst(float value) {
        setThirst(this.thirst + value);
    }

    // [추가] 수분 수치를 뺌 (최소치 0 제한)
    public void subThirst(float value) {
        setThirst(this.thirst - value);
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putFloat("thirst", thirst);
    }

    public void loadNBTData(CompoundTag nbt) {
        thirst = nbt.getFloat("thirst");
    }
}