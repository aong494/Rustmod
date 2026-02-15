package com.example.examplemod.network;

import com.example.examplemod.gui.SyncExtraSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    // static final 초기화를 제거하고 선언만 합니다.
    public static SimpleChannel INSTANCE;
    private static int packetId = 0;

    public static void register() {
        // register() 메서드가 실행될 때 채널을 생성합니다.
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("examplemod", "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // 0: 기존 슬롯 패킷
        INSTANCE.registerMessage(packetId++, SyncExtraSlotPacket.class,
                SyncExtraSlotPacket::toBytes, SyncExtraSlotPacket::new, SyncExtraSlotPacket::handle);

        // 1: 그룹원 위치 정보 (서버 -> 클라이언트)
        INSTANCE.registerMessage(packetId++, GroupLocationPacket.class,
                GroupLocationPacket::toBytes, GroupLocationPacket::new, GroupLocationPacket::handle);

        // 2: 핑 공유 패킷 (클라이언트 <-> 서버 양방향)
        INSTANCE.registerMessage(packetId++, MapPingPacket.class,
                MapPingPacket::toBytes, MapPingPacket::new, MapPingPacket::handle);
        // 배고픔 동기화 패킷 등록
        INSTANCE.registerMessage(packetId++, com.example.examplemod.Hunger.HungerSyncPacket.class,
                com.example.examplemod.Hunger.HungerSyncPacket::toBytes, // encode 대신 toBytes 사용
                com.example.examplemod.Hunger.HungerSyncPacket::new,
                com.example.examplemod.Hunger.HungerSyncPacket::handle);

        // 갈증 동기화 패킷 등록
        INSTANCE.registerMessage(packetId++, com.example.examplemod.Thirst.ThirstSyncPacket.class,
                com.example.examplemod.Thirst.ThirstSyncPacket::toBytes, // encode 대신 toBytes 사용
                com.example.examplemod.Thirst.ThirstSyncPacket::new,
                com.example.examplemod.Thirst.ThirstSyncPacket::handle);
        // 기존 6번 패킷 (ID 충돌 방지를 위해 packetId++ 구조로 유지하는 것이 좋습니다)
        INSTANCE.registerMessage(packetId++, MaintenancePacket.class,
                MaintenancePacket::toBytes, MaintenancePacket::new, MaintenancePacket::handle);
    }
}