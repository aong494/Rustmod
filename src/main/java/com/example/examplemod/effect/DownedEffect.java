package com.example.examplemod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class DownedEffect extends MobEffect {
    public DownedEffect() {
        // 카테고리(HARMFUL)와 입자 색상(빨강: 0xFF0000) 설정
        super(MobEffectCategory.HARMFUL, 0xFF0000);
    }
}