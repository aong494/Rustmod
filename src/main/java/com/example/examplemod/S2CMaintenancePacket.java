package com.example.examplemod;

import com.example.examplemod.gui.RustStyleChestScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class S2CMaintenancePacket {
    private final boolean isDecaying;
    private final String costString;

    // 서버에서 데이터를 보낼 때 사용
    public S2CMaintenancePacket(boolean isDecaying, String costString) {
        this.isDecaying = isDecaying;
        this.costString = costString;
    }

    // 데이터를 읽을 때 사용
    public S2CMaintenancePacket(FriendlyByteBuf buffer) {
        this.isDecaying = buffer.readBoolean();
        this.costString = buffer.readUtf();
    }

    // 데이터를 쓸 때 사용
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isDecaying);
        buffer.writeUtf(this.costString);
    }

    // 클라이언트가 패킷을 받았을 때 실행할 로직
    public static void handle(S2CMaintenancePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // RustStyleChestScreen에 만들어둔 static 변수에 서버 데이터를 꽂아넣음
            RustStyleChestScreen.isDecaying = msg.isDecaying;
            RustStyleChestScreen.maintenanceCost = msg.costString;
        });
        ctx.get().setPacketHandled(true);
    }
}