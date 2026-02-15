package com.example.examplemod.network;

import com.example.examplemod.gui.RustStyleChestScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class MaintenancePacket {
    private final boolean isDecaying;
    private final String costStr;

    public MaintenancePacket(FriendlyByteBuf buf) {
        // 서버의 sendMaintenancePacket 구조와 일치해야 함
        this.isDecaying = buf.readBoolean();
        // 서버에서 writeVarInt + Bytes로 보낸 문자열을 읽음
        this.costStr = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(isDecaying);
        buf.writeUtf(costStr);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // [핵심] 받은 데이터를 GUI의 static 변수에 업데이트
            RustStyleChestScreen.isDecaying = this.isDecaying;
            RustStyleChestScreen.maintenanceCost = this.costStr;
        });
        ctx.get().setPacketHandled(true);
    }
}