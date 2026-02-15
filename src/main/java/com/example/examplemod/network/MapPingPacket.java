package com.example.examplemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class MapPingPacket {
    private float x, z;
    private boolean isRemove;
    private int index;

    public MapPingPacket(float x, float z, boolean isRemove) {
        this.x = x; this.z = z; this.isRemove = isRemove;
    }

    public MapPingPacket(FriendlyByteBuf buffer) {
        this.x = buffer.readFloat();
        this.z = buffer.readFloat();
        this.isRemove = buffer.readBoolean();
        if (buffer.readableBytes() >= 4) { // 데이터가 있을 때만 읽기 (안전장치)
            this.index = buffer.readInt();
        }
    }
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.z);
        buffer.writeBoolean(this.isRemove);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ctx.get().getDirection().getReceptionSide().isServer()) {
                ClientPacketHandler.handlePing(this.x, this.z, this.isRemove, this.index);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}