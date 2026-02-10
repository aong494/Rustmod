package com.example.examplemod;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModAttributeHandler {
    private static void syncGear(Player player) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.getCapability(com.example.examplemod.capability.PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                com.example.examplemod.ModMessages.sendToPlayer(
                        new com.example.examplemod.SyncGearPacket(
                                cap.inventory.getStackInSlot(0),
                                cap.inventory.getStackInSlot(1)
                        ), serverPlayer);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        // 최대 체력 속성 고정
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(20.0D);
        }

        // 저장된 체력 값 복구
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(cap -> {
            float healthToRestore = cap.getSavedHealth();
            // 만약 저장된 값이 0 이하(사망 상태 등)라면 60으로 초기화
            if (healthToRestore <= 0) healthToRestore = 15.0F;
            player.setHealth(healthToRestore);
        });
        syncGear(player);
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player newPlayer = event.getEntity();
        Player oldPlayer = event.getOriginal();

        // 1. 새 플레이어의 최대 체력 먼저 설정
        AttributeInstance newMaxHealth = newPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (newMaxHealth != null) {
            newMaxHealth.setBaseValue(20.0D);
        }
        if (!event.isWasDeath()) {
            oldPlayer.getCapability(com.example.examplemod.capability.PlayerGearCapability.GEAR_CAPABILITY).ifPresent(oldCap -> {
                newPlayer.getCapability(com.example.examplemod.capability.PlayerGearCapability.GEAR_CAPABILITY).ifPresent(newCap -> {
                    newCap.inventory.setStackInSlot(0, oldCap.inventory.getStackInSlot(0));
                    newCap.inventory.setStackInSlot(1, oldCap.inventory.getStackInSlot(1));
                });
            });
        }

        // 2. 부활(Death)인지, 차원 이동(Dimension Change)인지 구분
        if (event.isWasDeath()) {
            // 죽어서 부활한 경우 60으로 설정
            // 주의: 여기서 setHealth를 해도 마인크래프트가 나중에 100으로 채울 수 있음
            newPlayer.setHealth(15.0F);
        } else {
            // 차원 이동 등은 기존 체력 유지
            newPlayer.setHealth(oldPlayer.getHealth());
        }
    }
    @SubscribeEvent
    public static void onPlayerDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        // 플레이어가 죽었을 때만 실행
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player && !player.level().isClientSide) {
            player.getCapability(com.example.examplemod.capability.PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                for (int i = 0; i < cap.inventory.getSlots(); i++) {
                    ItemStack stack = cap.inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        // 1. 땅에 아이템 엔티티 생성
                        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                                player.level(), player.getX(), player.getY(), player.getZ(), stack.copy());

                        // 2. 드랍 목록에 추가 (이렇게 해야 다른 모드들과 호환됩니다)
                        event.getDrops().add(itemEntity);

                        // 3. 기존 슬롯은 비우기 (복사 방지)
                        cap.inventory.setStackInSlot(i, ItemStack.EMPTY);
                    }
                }
            });
        }
    }

    // 3. [핵심 추가] 부활 직후에 다시 한번 체력을 60으로 고정
    @SubscribeEvent
    public static void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;

        // 1. 최대 체력 100 확인 및 체력 60으로 설정
        net.minecraft.world.entity.ai.attributes.AttributeInstance maxHealth = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(20.0D);
        }
        player.setHealth(15.0F);

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
        syncGear(player);
    }
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(cap -> {
            cap.setSavedHealth(player.getHealth());
        });
    }
}