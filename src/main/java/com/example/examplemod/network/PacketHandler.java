package com.example.examplemod.network;

import com.example.examplemod.gui.SyncExtraSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryParse("examplemod:main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        // 패킷 등록: SyncExtraSlotPacket
        INSTANCE.registerMessage(packetId++, SyncExtraSlotPacket.class,
                SyncExtraSlotPacket::toBytes,   // 데이터를 버퍼에 쓰기
                SyncExtraSlotPacket::new,       // 버퍼에서 데이터를 읽기
                SyncExtraSlotPacket::handle     // 서버에서 처리
        );
    }
}