package com.example.examplemod;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModAttributeHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        // 최대 체력 속성 고정
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(100.0D);
        }

        // 저장된 체력 값 복구
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(cap -> {
            float healthToRestore = cap.getSavedHealth();
            // 만약 저장된 값이 0 이하(사망 상태 등)라면 60으로 초기화
            if (healthToRestore <= 0) healthToRestore = 60.0F;
            player.setHealth(healthToRestore);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player newPlayer = event.getEntity();
        AttributeInstance newMaxHealth = newPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (newMaxHealth != null) {
            newMaxHealth.setBaseValue(100.0D);
        }

        if (event.isWasDeath()) {
            newPlayer.setHealth(16.0F);
        } else {
            newPlayer.setHealth(event.getOriginal().getHealth());
        }
    }
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(cap -> {
            cap.setSavedHealth(player.getHealth());
        });
    }
}