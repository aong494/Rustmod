package com.example.examplemod;

import com.example.examplemod.capability.PlayerGearCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncGearPacket {
    private final CompoundTag inventoryTag;

    public SyncGearPacket(CompoundTag tag) {
        this.inventoryTag = tag;
    }

    public SyncGearPacket(FriendlyByteBuf buf) {
        this.inventoryTag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeNbt(inventoryTag);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft.getInstance().player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                cap.inventory.deserializeNBT(inventoryTag);
            });
        });
        return true;
    }
}