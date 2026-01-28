package com.example.examplemod.gui;

import com.example.examplemod.capability.PlayerGearCapability; // 아까 만든 클래스 import
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncExtraSlotPacket {
    private final int slotId;
    private final ItemStack stack;

    public SyncExtraSlotPacket(int slotId, ItemStack stack) {
        this.slotId = slotId;
        this.stack = stack;
    }

    // 패킷 데이터를 쓰고 읽는 메서드 (패킷 등록 시 필요)
    public SyncExtraSlotPacket(FriendlyByteBuf buffer) {
        this.slotId = buffer.readInt();
        this.stack = buffer.readItem();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.slotId);
        buffer.writeItem(this.stack);
    }

    public static void handle(SyncExtraSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                // 이제 PlayerGearCapability.GEAR_CAPABILITY 를 통해 접근 가능합니다.
                player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                    cap.inventory.setStackInSlot(msg.slotId, msg.stack);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}