package com.example.examplemod;

import com.example.examplemod.gui.CraftingRustScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CCraftAmountPacket {
    // 1. 변수 선언 (이 부분이 없으면 'amount'를 찾을 수 없습니다)
    private final int amount;

    // 2. 생성자
    public S2CCraftAmountPacket(int amount) {
        this.amount = amount;
    }

    // 3. 버퍼 읽기용 생성자
    public S2CCraftAmountPacket(FriendlyByteBuf buf) {
        this.amount = buf.readInt();
    }

    // 4. 버퍼 쓰기 메서드
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.amount);
    }

    // 5. 핸들러 (clientAmount 업데이트)
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // CraftingRustScreen에 static으로 선언된 clientAmount에 값 대입
            CraftingRustScreen.clientAmount = this.amount;
        });
        return true;
    }
}