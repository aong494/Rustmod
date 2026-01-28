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
        Player oldPlayer = event.getOriginal();

        // 1. 새 플레이어의 최대 체력 먼저 설정
        AttributeInstance newMaxHealth = newPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (newMaxHealth != null) {
            newMaxHealth.setBaseValue(100.0D);
        }

        // 2. 부활(Death)인지, 차원 이동(Dimension Change)인지 구분
        if (event.isWasDeath()) {
            // 죽어서 부활한 경우 60으로 설정
            // 주의: 여기서 setHealth를 해도 마인크래프트가 나중에 100으로 채울 수 있음
            newPlayer.setHealth(60.0F);
        } else {
            // 차원 이동 등은 기존 체력 유지
            newPlayer.setHealth(oldPlayer.getHealth());
        }
    }

    // 3. [핵심 추가] 부활 직후에 다시 한번 체력을 60으로 고정
    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;

        // 1. 최대 체력 100 확인 및 체력 60으로 설정
        net.minecraft.world.entity.ai.attributes.AttributeInstance maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(100.0D);
        }
        player.setHealth(60.0F);

        // 2. 커스텀 배고픔(Hunger)을 100으로 설정 및 클라이언트 동기화
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(hunger -> {
            hunger.setHunger(100.0F); // 값을 100으로 고정
            com.example.examplemod.ModMessages.sendToPlayer(
                    new com.example.examplemod.Hunger.HungerSyncPacket(hunger.getHunger()), player);
        });

        // 3. 커스텀 목마름(Thirst)을 100으로 설정 및 클라이언트 동기화
        player.getCapability(com.example.examplemod.Thirst.ThirstProvider.PLAYER_THIRST).ifPresent(thirst -> {
            thirst.setThirst(100.0F); // 값을 100으로 고정
            com.example.examplemod.ModMessages.sendToPlayer(
                    new com.example.examplemod.Thirst.ThirstSyncPacket(thirst.getThirst()), player);
        });
    }
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(cap -> {
            cap.setSavedHealth(player.getHealth());
        });
    }
}