package com.example.examplemod;

import com.example.examplemod.Hunger.HungerSyncPacket;
import com.example.examplemod.Thirst.ThirstSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() { return packetId++; }

    public static void register() {
        // [핵심 추가] 이미 등록되어 있다면 다시 실행하지 않음
        if (INSTANCE != null) return;

        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation("examplemod", "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // 1. 목마름 동기화 (S -> C)
        net.messageBuilder(ThirstSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ThirstSyncPacket::new)
                .encoder(ThirstSyncPacket::toBytes)
                .consumerMainThread(ThirstSyncPacket::handle)
                .add();

        // 2. 허기 동기화 (S -> C)
        net.messageBuilder(HungerSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HungerSyncPacket::new)
                .encoder(HungerSyncPacket::toBytes)
                .consumerMainThread(HungerSyncPacket::handle)
                .add();

        // 3. 통합 장비/커스텀 슬롯 교체 패킷 (C -> S)
        // 이제 이 패킷 하나로 갑옷 4칸 + 커스텀 2칸을 모두 처리합니다.
        net.messageBuilder(ArmorSwapPacket.class, id())
                .encoder(ArmorSwapPacket::toBytes)
                .decoder(ArmorSwapPacket::new)
                .consumerMainThread(ArmorSwapPacket::handle)
                .add();
        net.messageBuilder(SyncGearPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncGearPacket::new)
                .encoder(SyncGearPacket::toBytes)
                .consumerMainThread(SyncGearPacket::handle)
                .add();
        net.messageBuilder(SyncArmorPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncArmorPacket::new)
                .encoder(SyncArmorPacket::toBytes)
                .consumerMainThread(SyncArmorPacket::handle)
                .add();
    }

    // 서버가 플레이어에게 보낼 때 (기존)
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    // [추가] 클라이언트가 서버로 보낼 때 (새로 필요함)
    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }
}