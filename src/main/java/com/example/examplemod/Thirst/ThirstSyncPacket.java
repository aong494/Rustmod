package com.example.examplemod.Thirst;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class ThirstSyncPacket {
    private final float thirst;

    public ThirstSyncPacket(float thirst) {
        this.thirst = thirst;
    }

    // 버퍼에서 읽기 (수신)
    public ThirstSyncPacket(FriendlyByteBuf buf) {
        this.thirst = buf.readFloat();
    }

    // 버퍼에 쓰기 (송신)
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(thirst);
    }

    // 클라이언트에서 패킷을 받았을 때 실행될 로직
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 클라이언트 사이드에서만 실행
            ClientThirstData.set(thirst);
        });
        return true;
    }
}