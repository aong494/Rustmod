package com.example.examplemod;

import com.example.examplemod.capability.PlayerGearCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ArmorSwapPacket {
    private final int slotIdx; // 0~3: 갑옷, 4~5: 커스텀 슬롯

    public ArmorSwapPacket(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    public ArmorSwapPacket(FriendlyByteBuf buffer) {
        this.slotIdx = buffer.readInt();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(slotIdx);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack carried = player.containerMenu.getCarried();

            // --- [1] 아이템 교체 로직 ---
            if (slotIdx >= 0 && slotIdx <= 3) {
                // 갑옷 슬롯 처리
                ItemStack armor = player.getInventory().getArmor(slotIdx).copy();
                EquipmentSlot eSlot = getEquipmentSlot(slotIdx);

                if (carried.isEmpty() || carried.canEquip(eSlot, player)) {
                    player.getInventory().armor.set(slotIdx, carried.copy());
                    player.containerMenu.setCarried(armor);
                }
            } else if (slotIdx == 4 || slotIdx == 5) {
                // 커스텀 슬롯 처리 (4->0, 5->1)
                int gearIdx = slotIdx - 4;
                player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                    ItemStack gear = cap.inventory.getStackInSlot(gearIdx).copy();
                    cap.inventory.setStackInSlot(gearIdx, carried.copy());
                    player.containerMenu.setCarried(gear);

                    // 커스텀 슬롯 데이터 즉시 동기화
                    ModMessages.sendToPlayer(new SyncGearPacket(cap.inventory.serializeNBT()), player);
                });
            }

            // --- [2] 모든 메뉴 동기화 (실시간 반영 핵심) ---

            // 현재 화면(상자)과 인벤토리 메뉴 둘 다 갱신
            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();

            // 마우스 아이템 상태 강제 전송 (Container ID -1은 마우스 전용)
            // 1.20.1 버전의 생성자: (containerId, stateId, slotIdx, itemStack)
            player.connection.send(new ClientboundContainerSetSlotPacket(
                    -1,
                    player.containerMenu.getStateId(),
                    -1,
                    player.containerMenu.getCarried()
            ));
        });
        return true;
    }

    private EquipmentSlot getEquipmentSlot(int idx) {
        return switch (idx) {
            case 3 -> EquipmentSlot.HEAD;
            case 2 -> EquipmentSlot.CHEST;
            case 1 -> EquipmentSlot.LEGS;
            default -> EquipmentSlot.FEET;
        };
    }
}