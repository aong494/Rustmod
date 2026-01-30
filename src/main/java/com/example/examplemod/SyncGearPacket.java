package com.example.examplemod;

import com.example.examplemod.capability.PlayerGearCapability;
import com.example.examplemod.gui.RustStyleInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncGearPacket {
    private final ItemStack slot0;
    private final ItemStack slot1;

    // [해결책] 이 생성자가 없어서 에러가 났던 것입니다. 추가해 주세요!
    public SyncGearPacket(ItemStack slot0, ItemStack slot1) {
        this.slot0 = slot0;
        this.slot1 = slot1;
    }

    // 버퍼에서 읽어올 때 (S -> C)
    public SyncGearPacket(FriendlyByteBuf buf) {
        this.slot0 = buf.readItem();
        this.slot1 = buf.readItem();
    }

    // 버퍼에 쓸 때
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(slot0);
        buf.writeItem(slot1);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                net.minecraft.client.Minecraft.getInstance().player.getCapability(
                        com.example.examplemod.capability.PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                    cap.inventory.setStackInSlot(0, slot0);
                    cap.inventory.setStackInSlot(1, slot1);
                });
                if (net.minecraft.client.Minecraft.getInstance().screen instanceof RustStyleInventoryScreen gui) {
                    gui.updateExtraSlots(slot0, slot1);
                }
            }
        });
        return true;
    }
}