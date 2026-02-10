package com.example.examplemod.Thirst;

import com.example.examplemod.Hunger.HungerCapability;
import com.example.examplemod.Hunger.HungerProvider;
import com.example.examplemod.Hunger.HungerSyncPacket;
import com.example.examplemod.ModMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

@Mod.EventBusSubscriber(modid = "examplemod")
public class ThirstEvents {
    // ThirstEvents 클래스 맨 윗부분에 추가하세요
    private static final java.util.UUID THIRST_SLOW_ID = java.util.UUID.fromString("d8955132-706a-4952-8703-9b9220935392");
    private static final net.minecraft.world.entity.ai.attributes.AttributeModifier THIRST_SLOW_MODIFIER =
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(THIRST_SLOW_ID, "Thirst Slowdown", -0.3D, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL);

    // 1. 플레이어 생성 시 Capability 부착
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(ThirstProvider.PLAYER_THIRST).isPresent()) {
                event.addCapability(ResourceLocation.tryParse("examplemod:textures/thirst"), new ThirstProvider());
            }
            if (!event.getObject().getCapability(HungerProvider.PLAYER_HUNGER).isPresent()) {
                event.addCapability(ResourceLocation.tryParse("examplemod:custom_hunger"), new HungerProvider());
            }
        }
    }

    // 2. 수분 감소 및 디버프 로직
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            ServerPlayer player = (ServerPlayer) event.player;
            player.getCapability(HungerProvider.PLAYER_HUNGER).ifPresent((HungerCapability hunger) -> {

                // [중요] 배고픔 감소 로직
                // 20틱(1초)마다 0.1씩 감소 (취향에 따라 수치 조절)
                if (player.tickCount % 20 == 0) {
                    hunger.subHunger(0.1f); // 실제로 서버 데이터를 깎음

                    // [중요] 감소된 데이터를 클라이언트로 즉시 전송 (동기화)
                    ModMessages.sendToPlayer(new HungerSyncPacket(hunger.getHunger()), player);
                }

                if (hunger.getHunger() < 500.0f) {
                    player.getFoodData().setFoodLevel(19); // 19면 시스템이 "배가 고프다"고 판단함
                } else {
                    player.getFoodData().setFoodLevel(20); // 500이면 20으로 고정하여 더 못 먹게 함
                }

                // 2. 배고픔 0일 때 대미지 (기존 유지)
                if (hunger.getHunger() <= 15.0f && player.tickCount % 40 == 0) {
                    player.hurt(player.damageSources().starve(), 1.0f);
                }
            });

            player.getCapability(ThirstProvider.PLAYER_THIRST).ifPresent(thirst -> {
                float current = thirst.getThirst();

                // 초당 약 0.05씩 자연 감소 (20틱 = 1초)
                if (player.tickCount % 20 == 0) {
                    thirst.setThirst(current - 0.1f);
                    if (player.tickCount % 10 == 0) { // 너무 자주 보내면 렉이 걸리니 0.5초마다 동기화
                        ModMessages.sendToPlayer(new ThirstSyncPacket(thirst.getThirst()), player);
                    }
                }

                // 조건 2: 수분 0 도달 - 대미지 및 달리기 불가
                    AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);

                    if (current <= 0) {
                        // 1. 수분이 0이면 달리기 즉시 해제
                        if (player.isSprinting()) {
                            player.setSprinting(false);
                        }

                        // 2. 이동 속도 디버프 부여 (이게 있어야 달리기가 원천 봉쇄됩니다)
                        if (speed != null && !speed.hasModifier(THIRST_SLOW_MODIFIER)) {
                            speed.addTransientModifier(THIRST_SLOW_MODIFIER);
                        }

                        // 3. 아사 대미지
                        if (player.tickCount % 40 == 0) {
                            player.hurt(player.damageSources().starve(), 1.0f);
                        }
                    } else {
                        // 수분이 회복되면 속도 디버프 제거
                        if (speed != null && speed.hasModifier(THIRST_SLOW_MODIFIER)) {
                            speed.removeModifier(THIRST_SLOW_ID);
                        }
                    }
            });
        }
    }
    // 3. 음식 섭취 시 회복 로직
    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getItem().isEdible()) {
            player.getCapability(HungerProvider.PLAYER_HUNGER).ifPresent((HungerCapability hunger) -> {
                // 음식의 기본 영양값의 10배만큼 커스텀 배고픔 회복 (예: 하트 3개(6) -> 60 회복)
                float foodValue = event.getItem().getFoodProperties(player).getNutrition();
                hunger.addHunger(foodValue * 5.0f);
            });
        }
    }
    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 수분 40 미만 체크 (기존 로직)
            player.getCapability(ThirstProvider.PLAYER_THIRST).ifPresent((ThirstCapability thirst) -> {
                if (thirst.getThirst() < 40.0f) {
                    event.setCanceled(true);
                }
            });
        // [추가] 배고픔 50 이하 체크 및 150 회복 전까지 회복 차단
           player.getCapability(HungerProvider.PLAYER_HUNGER).ifPresent((HungerCapability hunger) -> {
                float currentHunger = hunger.getHunger();

              // 50 이하로 떨어지면 자연 회복 중단
              if (currentHunger <= 50.0f) {
                  event.setCanceled(true);
              }

             // 배고픔 상태 치료 조건: 만약 150 미만이라면 회복 차단 유지
              // (즉, 한 번 50 이하로 떨어지면 150을 넘길 때까지 회복이 안 됨)
              if (currentHunger < 150.0f) {
                  event.setCanceled(true);
               }
           });
        }
    }
    @SubscribeEvent
    public static void onSprint(TickEvent.PlayerTickEvent event) {
        // START 페이즈에서 처리해야 달리기 판정 전에 차단 가능
        if (event.phase == TickEvent.Phase.START) {
            Player player = event.player;

            // 서버와 클라이언트 양쪽에서 모두 체크
            player.getCapability(ThirstProvider.PLAYER_THIRST).ifPresent((ThirstCapability data) -> {
                if (data.getThirst() <= 0) {
                    if (player.isSprinting()) {
                        player.setSprinting(false);

                        // 클라이언트일 경우, 달리기 키 입력을 무력화하기 위해 FOV나 물리 로직에 개입
                        if (event.side.isClient()) {
                            player.setSprinting(false);
                            // 마인크래프트 클라이언트의 보행 속도 패킷 전송을 억제
                            Minecraft.getInstance().options.keySprint.setDown(false);
                        }
                    }
                }
            });
        }
    }
    @SubscribeEvent
    public static void onDrinkWater(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ThirstProvider.PLAYER_THIRST).ifPresent(thirst -> {
                boolean changed = false;

                // 물병 체크
                if (event.getItem().is(Items.POTION) && PotionUtils.getPotion(event.getItem()) == Potions.WATER) {
                    thirst.addThirst(50.0f);
                    changed = true;
                }
                // 수박 체크 (독립적)
                else if (event.getItem().is(Items.MELON_SLICE)) {
                    thirst.addThirst(10.0f);
                    changed = true;
                }
                // 사과 체크 (독립적)
                else if (event.getItem().is(Items.APPLE)) {
                    thirst.addThirst(5.0f);
                    changed = true;
                }

                if (changed) {
                    ModMessages.sendToPlayer(new ThirstSyncPacket(thirst.getThirst()), player);
                }
            });
        }
    }
    @SubscribeEvent
    public static void onRightClickWater(PlayerInteractEvent event) {
        // RightClickItem(아이템/공중 우클릭) 또는 RightClickBlock(블록 우클릭) 대응
        if (!(event instanceof PlayerInteractEvent.RightClickItem || event instanceof PlayerInteractEvent.RightClickBlock)) return;

        Player player = event.getEntity();
        Level level = event.getLevel();

        // 클라이언트라면 무시 (서버 로직에서 처리)
        if (level.isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) return;

        // 1. 물 속 체크 (발 또는 눈 위치)
        boolean isInWater = player.isInWater() || level.getFluidState(player.blockPosition()).is(Fluids.WATER);

        // 2. 레이트레이싱 체크 (물 표면 클릭 대비)
        BlockHitResult ray = (BlockHitResult) player.pick(4.0D, 0.0F, true);
        boolean isLookingAtWater = ray.getType() == HitResult.Type.BLOCK && level.getFluidState(ray.getBlockPos()).is(Fluids.WATER);

        if (isInWater || isLookingAtWater) {
            player.getCapability(ThirstProvider.PLAYER_THIRST).ifPresent((ThirstCapability data) -> {
                ItemStack heldItem = player.getItemInHand(event.getHand());

                // 유리병, 양동이 등 물을 퍼내는 아이템은 제외
                if (!heldItem.isEmpty() && (heldItem.is(Items.GLASS_BOTTLE) || heldItem.is(Items.BUCKET))) return;

                if (data.getThirst() < 250.0f) {
                    // 3. 갈증 회복 및 동기화
                    data.addThirst(20.0f);
                    ModMessages.sendToPlayer(new ThirstSyncPacket(data.getThirst()), (ServerPlayer) player);

                    // 효과 재생
                    player.playSound(SoundEvents.GENERIC_DRINK, 0.5f, 1.0f);
                    player.swing(event.getHand(), true);

                    // 이벤트 성공 처리 및 취소
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    if (event.isCancelable()) event.setCanceled(true);
                }
            });
        }
    }
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide) {
            // 1. 최대 체력을 100으로 설정 (기본값 20)
            AttributeInstance maxHealthAttr = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttr != null) {
                maxHealthAttr.setBaseValue(100.0D);
                // 현재 체력이 바뀐 최대치에 맞게 꽉 차게 하려면 아래 줄 추가
                player.setHealth(100.0f);
            }

            // 주의: 배고픔(Hunger)은 마인크래프트 엔진상 '속성(Attribute)'이 아니라
            // FoodData 클래스 내부에 20이라는 숫자가 하드코딩되어 있습니다.
            // 배고픔 최대치를 500으로 확장하려면 별도의 Accessor나 틱 기반의 관리가 필요합니다.
        }
    }
    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();

        // 플레이어가 음식을 들고 있다면
        if (itemStack.isEdible()) {
            // 커스텀 배고픔이 최대치(500)보다 낮으면 강제로 먹기 시작
            player.getCapability(HungerProvider.PLAYER_HUNGER).ifPresent(hunger -> {
                if (hunger.getHunger() < 500.0f) {
                    // 바닐라의 "배부름 체크"를 무시하고 아이템 사용을 허용
                    // (아이템 사용 애니메이션과 소리가 정상적으로 나게 됨)
                }
            });
        }
    }
    // 아이템 사용 조건 완화
    @SubscribeEvent
    public static void onStartUsingItem(LivingEntityUseItemEvent.Start event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack itemStack = event.getItem();

            // 음식 아이템인 경우
            if (itemStack.isEdible()) {
                player.getCapability(HungerProvider.PLAYER_HUNGER).ifPresent(hunger -> {
                    // 커스텀 배고픔이 부족하다면 먹는 시간을 정상적으로 설정
                    if (hunger.getHunger() < 500.0f) {
                        // 이 이벤트는 단순히 체크용이며, 취소하지 않으면 진행됩니다.
                    }
                });
            }
        }
    }
}