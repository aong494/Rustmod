package com.example.examplemod;

import com.example.examplemod.capability.PlayerGearCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncExtraSlotPacket {
    private final int index;
    private final ItemStack stack;

    public SyncExtraSlotPacket(int index, ItemStack stack) {
        this.index = index;
        this.stack = stack;
    }

    public SyncExtraSlotPacket(FriendlyByteBuf buffer) {
        this.index = buffer.readInt();
        this.stack = buffer.readItem();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeItem(stack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                    // 1. 서버 Capability에 아이템 저장
                    cap.inventory.setStackInSlot(index, stack);
                    // 2. 중요: 서버에서 들고 있는 아이템 상태 강제 동기화
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                    player.containerMenu.setRemoteCarried(ItemStack.EMPTY);
                    ModMessages.sendToPlayer(new SyncGearPacket(
                            cap.inventory.getStackInSlot(0),
                            cap.inventory.getStackInSlot(1)
                    ), player);

                    player.containerMenu.broadcastChanges();
                });
            }
        });
        return true;
    }
}