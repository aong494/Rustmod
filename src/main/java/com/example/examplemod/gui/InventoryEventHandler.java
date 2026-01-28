package com.example.examplemod.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.example.examplemod.capability.PlayerGearCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;

@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InventoryEventHandler {
    // 1. 플레이어가 생성될 때 Capability 저장소를 붙여주는 이벤트
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            ResourceLocation capLocation = ResourceLocation.tryParse("examplemod:extra_gear");

            if (capLocation != null) {
                event.addCapability(capLocation, new PlayerGearCapability.Provider());
            }
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;
            Inventory inv = player.getInventory();

            // 1. 숨겨진 핫바 슬롯 (6, 7, 8번) 감시
            for (int i = 6; i <= 8; i++) {
                ItemStack stackInHiddenHotbar = inv.getItem(i);

                if (!stackInHiddenHotbar.isEmpty()) {
                    // 2. 메인 인벤토리(9~32번)의 빈 공간 찾기
                    int emptySlot = -1;
                    for (int j = 9; j <= 32; j++) {
                        if (inv.getItem(j).isEmpty()) {
                            emptySlot = j;
                            break;
                        }
                    }

                    if (emptySlot != -1) {
                        // 빈칸이 있다면 거기로 이동
                        inv.setItem(emptySlot, stackInHiddenHotbar.copy());
                        inv.setItem(i, ItemStack.EMPTY);
                    } else {
                        // 인벤토리마저 꽉 찼다면 발밑으로 뱉기
                        player.drop(stackInHiddenHotbar.copy(), true, false);
                        inv.setItem(i, ItemStack.EMPTY);
                    }
                }
            }

            // 3. 숨겨진 메인 인벤토리 끝부분 (33~35번)은 항상 뱉기
            for (int i = 33; i <= 35; i++) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    player.drop(stack.copy(), true, false);
                    inv.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            Inventory inv = player.getInventory();
            // 사망 시에도 동일한 범위의 아이템 삭제 (드롭 방지)
            for (int i = 6; i <= 8; i++) inv.setItem(i, ItemStack.EMPTY);
            for (int i = 33; i <= 35; i++) inv.setItem(i, ItemStack.EMPTY);
        }
    }
}
// 2. [매우 중요] Capability 시스템 자체를 등록하는 클래스 (버스 타입이 MOD임)
@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.MOD)
class ModBusEvents {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        // 시스템에 PlayerGearCapability 클래스를 등록
        event.register(PlayerGearCapability.class);
    }
}