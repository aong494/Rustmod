package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncArmorPacket {
    private final int slot;
    private final ItemStack stack;

    public SyncArmorPacket(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    public SyncArmorPacket(FriendlyByteBuf buf) {
        this.slot = buf.readInt();
        this.stack = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        buf.writeItem(stack);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // [클라이언트] 상자가 열려있어도 강제로 갑옷 데이터를 수정함
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.getInventory().armor.set(slot, stack);
            }
        });
        return true;
    }
}