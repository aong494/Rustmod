package com.example.examplemod;

import com.example.examplemod.effect.ModEffects;
import com.example.examplemod.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
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
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) return;

        // 1. [기존 로직] 최대 체력 속성 고정
        AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(20.0D);
        }

        // 2. [기존 로직] 저장된 체력 값 복구 및 배고픔 패킷 전송
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(cap -> {
            float healthToRestore = cap.getSavedHealth();
            if (healthToRestore <= 0) healthToRestore = 15.0F;
            player.setHealth(healthToRestore);

            // [추가] 배고픔 데이터 패킷 전송
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new com.example.examplemod.Hunger.HungerSyncPacket(cap.getHunger())
            );
        });

        // 3. [추가] 갈증 데이터 로드 및 패킷 전송
        player.getCapability(com.example.examplemod.Thirst.ThirstProvider.PLAYER_THIRST).ifPresent(cap -> {
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new com.example.examplemod.Thirst.ThirstSyncPacket(cap.getThirst())
            );
        });

        // 4. [기존 로직] 장비 동기화
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
    public static void syncPlayerStats(ServerPlayer player) {
        // 배고픔 동기화
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(hunger -> {
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new com.example.examplemod.Hunger.HungerSyncPacket(hunger.getHunger())
            );
        });

        // 갈증 동기화
        player.getCapability(com.example.examplemod.Thirst.ThirstProvider.PLAYER_THIRST).ifPresent(thirst -> {
            PacketHandler.INSTANCE.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                    new com.example.examplemod.Thirst.ThirstSyncPacket(thirst.getThirst())
            );
        });
    }
    // 클래스 내부에 추가
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        // 서버 측에서만 실행하며, 틱의 마지막 단계에서 처리
        if (event.side.isServer() && event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            ServerPlayer player = (ServerPlayer) event.player;
            if (player.hasEffect(com.example.examplemod.effect.ModEffects.DOWNED.get())) {
                player.setPose(net.minecraft.world.entity.Pose.SLEEPING);
            }
            // 성능을 위해 매 틱(0.05초)마다 보내지 않고, 10틱(0.5초)마다 패킷을 보냅니다.
            // 숫자를 20으로 바꾸면 1초마다 보냅니다.
            if (player.level().getGameTime() % 10 == 0) {
                syncPlayerStats(player);
            }
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

        // 2. 커스텀 배고픔(Hunger)을 100으로 설정
        player.getCapability(com.example.examplemod.Hunger.HungerProvider.PLAYER_HUNGER).ifPresent(hunger -> {
            hunger.setHunger(100.0F);
        });

        // 3. 커스텀 목마름(Thirst)을 100으로 설정
        player.getCapability(com.example.examplemod.Thirst.ThirstProvider.PLAYER_THIRST).ifPresent(thirst -> {
            thirst.setThirst(100.0F);
        });

        // [중요] 설정한 값들을 패킷으로 즉시 동기화
        syncPlayerStats(player);
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