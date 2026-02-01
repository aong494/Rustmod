package com.example.examplemod.gui;

import com.example.examplemod.ModMessages;
import com.example.examplemod.SyncResistancePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
        // 서버 사이드에서만 동작, Phase는 START가 안정적입니다.
        if (event.phase == TickEvent.Phase.START && !event.player.level().isClientSide) {
            Player player = event.player;

            // [중요 1] 인벤토리나 창(GUI)을 열고 있을 때는 로직 중단!
            // 서버가 아이템을 계속 만지면 클라이언트에서 툴팁(Lore) 렌더링이 취소됩니다.
            if (player.containerMenu != player.inventoryMenu) return;

            // [중요 2] 매 틱(0.05초)마다 실행하면 패킷 과부하가 걸립니다. 10틱(0.5초) 주기로 완화.
            if (player.tickCount % 10 != 0) return;

            Inventory inv = player.getInventory();
            boolean changed = false;

            // 6, 7, 8번 슬롯 및 33, 34, 35번 슬롯 검사
            int[] restrictedSlots = {6, 7, 8, 33, 34, 35};
            for (int i : restrictedSlots) {
                ItemStack stack = inv.getItem(i);
                if (!stack.isEmpty()) {
                    int emptySlot = findEmptyMainSlot(inv);
                    if (emptySlot != -1) {
                        inv.setItem(emptySlot, stack.copy());
                        inv.setItem(i, ItemStack.EMPTY);
                        changed = true;
                    } else {
                        player.drop(stack.copy(), true, false);
                        inv.setItem(i, ItemStack.EMPTY);
                        changed = true;
                    }
                }
            }

            // 아이템이 이동되었다면 클라이언트에 변경 사항 전송
            if (changed && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }
    }

    // 메인 인벤토리(9~32번)에서 빈칸을 찾는 헬퍼 메서드
    private static int findEmptyMainSlot(Inventory inv) {
        for (int i = 9; i <= 32; i++) {
            if (inv.getItem(i).isEmpty()) return i;
        }
        return -1;
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
    @SubscribeEvent
    public static void onInventoryUpdate(TickEvent.PlayerTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            if (event.player instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.tickCount % 20 == 0) {
                    int insul = calculateInsulation(serverPlayer);
                    int rad = calculateRadiation(serverPlayer);
                    ModMessages.sendToPlayer(new SyncResistancePacket(insul, rad), serverPlayer);
                }
            }
        }
    }
    private static int calculateInsulation(Player player) {
        int totalLevel = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.isEmpty() || !armor.hasTag()) continue;

            if (armor.getTag().contains("PublicBukkitValues")) {
                net.minecraft.nbt.CompoundTag bukkitTag = armor.getTag().getCompound("PublicBukkitValues");

                if (bukkitTag.contains("rust:insulation")) {
                    totalLevel += (int) bukkitTag.getDouble("rust:insulation");
                }
            }
        }
        return totalLevel;
    }

    private static int calculateRadiation(Player player) {
        int totalLevel = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.isEmpty() || !armor.hasTag()) continue;

            if (armor.getTag().contains("PublicBukkitValues")) {
                net.minecraft.nbt.CompoundTag bukkitTag = armor.getTag().getCompound("PublicBukkitValues");

                // 플러그인에서 정의한 키 이름: "rust:radiation_protection"
                if (bukkitTag.contains("rust:radiation_protection")) {
                    // 데이터 타입이 Double일 가능성이 높으므로 getDouble 후 캐스팅
                    double value = bukkitTag.getDouble("rust:radiation_protection");

                    // 만약 getDouble로 0이 나온다면 정수형(Int)일 수 있으므로 다시 체크
                    if (value == 0 && bukkitTag.contains("rust:radiation_protection", 3)) { // 3은 Int 타입 번호
                        totalLevel += bukkitTag.getInt("rust:radiation_protection");
                    } else {
                        totalLevel += (int) value;
                    }
                }
            }
        }
        return totalLevel;
    }
    @SubscribeEvent
    public static void onTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty() && stack.hasTag()) {
            // NBT가 있는지 디버깅용 텍스트 추가
            if (stack.getTag().contains("PublicBukkitValues")) {
                event.getToolTip().add(net.minecraft.network.chat.Component.literal("§7[보호 수치 포함됨]"));
            }
        }
    }
}
// 2. [매우 중요] Capability 시스템 자체를 등록하는 클래스 (버스 타입이 MOD임)
@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.MOD)
class ModBusEvents {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerGearCapability.class);
    }
}