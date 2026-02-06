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

        // 1. S -> C 패킷들
        INSTANCE.messageBuilder(ThirstSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ThirstSyncPacket::new).encoder(ThirstSyncPacket::toBytes)
                .consumerMainThread(ThirstSyncPacket::handle).add();

        INSTANCE.messageBuilder(HungerSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HungerSyncPacket::new).encoder(HungerSyncPacket::toBytes)
                .consumerMainThread(HungerSyncPacket::handle).add();

        INSTANCE.messageBuilder(SyncGearPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncGearPacket::new)
                .encoder(SyncGearPacket::toBytes)
                .consumerMainThread(SyncGearPacket::handle)
                .add();

        INSTANCE.messageBuilder(SyncArmorPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncArmorPacket::new).encoder(SyncArmorPacket::toBytes)
                .consumerMainThread(SyncArmorPacket::handle).add();

        // 2. C -> S 패킷들 (에러가 발생하던 패킷들)
        INSTANCE.messageBuilder(ArmorSwapPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ArmorSwapPacket::new).encoder(ArmorSwapPacket::toBytes)
                .consumerMainThread(ArmorSwapPacket::handle).add();

        INSTANCE.messageBuilder(SyncExtraSlotPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SyncExtraSlotPacket::new)
                .encoder(SyncExtraSlotPacket::toBytes)
                .consumerMainThread(SyncExtraSlotPacket::handle)
                .add();
        INSTANCE.messageBuilder(SyncResistancePacket.class, 10, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncResistancePacket::new)
                .encoder(SyncResistancePacket::toBytes)
                .consumerMainThread(SyncResistancePacket::handle)
                .add();
        INSTANCE.messageBuilder(S2CMaintenancePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMaintenancePacket::new)
                .encoder(S2CMaintenancePacket::toBytes)
                .consumerMainThread(S2CMaintenancePacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        // INSTANCE가 null인지 체크하여 안전하게 전송
        if (INSTANCE != null) {
            INSTANCE.sendToServer(message);
        }
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        if (INSTANCE != null) {
            INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
        }
    }
}