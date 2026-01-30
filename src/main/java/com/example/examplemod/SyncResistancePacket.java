package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class SyncResistancePacket {
    private final int insulation;
    private final int radiation; // 방사능 로직도 비슷하게 합산한다고 가정

    public SyncResistancePacket(int insulation, int radiation) {
        this.insulation = insulation;
        this.radiation = radiation;
    }

    public SyncResistancePacket(FriendlyByteBuf buf) {
        this.insulation = buf.readInt();
        this.radiation = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(insulation);
        buf.writeInt(radiation);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientStats.insulation = this.insulation;
            ClientStats.radiation = this.radiation;
        });
        return true;
    }
}