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

// ArmorSwapPacket.java

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;

            ItemStack carried = player.containerMenu.getCarried().copy(); // 복사본 필수

            // 1. 갑옷 슬롯 처리
            if (slotIdx >= 0 && slotIdx <= 3) {
                ItemStack oldArmor = player.getInventory().armor.get(slotIdx).copy();
                EquipmentSlot eSlot = getEquipmentSlot(slotIdx);

                if (carried.isEmpty() || carried.canEquip(eSlot, player)) {
                    // 서버 데이터 변경
                    player.getInventory().armor.set(slotIdx, carried);
                    player.containerMenu.setCarried(oldArmor);

                    // [추가] 커스텀 패킷으로 클라이언트 갑옷 칸을 즉시 갱신 (잔상 방지 핵심)
                    ModMessages.sendToPlayer(new SyncArmorPacket(slotIdx, carried), player);
                }
            }
            // 2. 커스텀 슬롯 처리
            else if (slotIdx == 4 || slotIdx == 5) {
                int gearIdx = slotIdx - 4;
                player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                    ItemStack oldGear = cap.inventory.getStackInSlot(gearIdx).copy();
                    cap.inventory.setStackInSlot(gearIdx, carried);
                    player.containerMenu.setCarried(oldGear);

                    // 커스텀 슬롯 데이터 동기화
                    ModMessages.sendToPlayer(new SyncGearPacket(cap.inventory.serializeNBT()), player);
                });
            }

            // 3. 마우스에 든 아이템 강제 동기화 (이게 없으면 아이템 복사 현상 발생)
            player.containerMenu.broadcastChanges();
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                    -1, // 커서 아이템
                    player.containerMenu.incrementStateId(),
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