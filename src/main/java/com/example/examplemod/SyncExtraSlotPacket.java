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

    // 데이터를 바이트 단위로 읽기/쓰기
    public SyncExtraSlotPacket(FriendlyByteBuf buffer) {
        this.index = buffer.readInt();
        this.stack = buffer.readItem();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
        buffer.writeItem(stack);
    }

    // 서버에서 실행될 로직
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // [수정] 이제 이 코드는 클라이언트(내 컴퓨터 화면)에서 실행됩니다.
            net.minecraft.client.Minecraft.getInstance().player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                cap.inventory.setStackInSlot(index, stack);
            });
        });
        return true;
    }
}