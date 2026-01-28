package com.example.examplemod.Hunger;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class HungerSyncPacket {
    // 1. 데이터를 담을 변수 선언
    private final float hunger;

    // 2. 서버에서 보낼 때 사용할 생성자
    public HungerSyncPacket(float hunger) {
        this.hunger = hunger;
    }

    // 3. 네트워크를 통해 데이터를 읽어올 때 사용하는 생성자 (중요)
    public HungerSyncPacket(FriendlyByteBuf buf) {
        this.hunger = buf.readFloat();
    }

    // 4. 데이터를 네트워크로 내보낼 때 사용하는 메서드 (중요)
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(hunger);
    }

    // 5. 클라이언트에서 데이터를 받았을 때 실행되는 로직
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ClientHungerData.set(this.hunger);
        });
        return true;
    }
}