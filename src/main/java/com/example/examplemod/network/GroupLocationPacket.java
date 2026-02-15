package com.example.examplemod.network;

import com.example.examplemod.gui.MapScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class GroupLocationPacket {
    private final UUID memberUuid;
    private final String name;
    private final double x;
    private final double z;
    private final int index;

    public GroupLocationPacket(UUID memberUuid, String name, double x, double z, int index) {
        this.memberUuid = memberUuid;
        this.name = name;
        this.x = x;
        this.z = z;
        this.index = index;
    }
    public GroupLocationPacket(FriendlyByteBuf buffer) {
        long most = buffer.readLong();
        long least = buffer.readLong();
        this.memberUuid = new UUID(most, least);
        this.name = buffer.readUtf();
        this.x = buffer.readDouble();
        this.z = buffer.readDouble();
        this.index = (buffer.readableBytes() >= 4) ? buffer.readInt() : 1;
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeLong(this.memberUuid.getMostSignificantBits());
        buffer.writeLong(this.memberUuid.getLeastSignificantBits());
        buffer.writeUtf(this.name);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.z);
        buffer.writeInt(this.index);
    }

    public static void handle(GroupLocationPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            // 이제 MapScreen의 5개 인자 메서드와 완벽히 호환됩니다.
            MapScreen.updateGroupMember(
                    packet.memberUuid,
                    packet.name,
                    packet.x,
                    packet.z,
                    packet.index
            );
        });
        contextSupplier.get().setPacketHandled(true);
    }
}