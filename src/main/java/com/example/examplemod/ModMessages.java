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
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.tryParse("examplemod:messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(ThirstSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ThirstSyncPacket::new) // 생성자 참조
                .encoder(ThirstSyncPacket::toBytes) // 메서드 참조
                .consumerMainThread(ThirstSyncPacket::handle) // 메서드 참조
                .add();

        INSTANCE.messageBuilder(HungerSyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(HungerSyncPacket::new)
                .encoder(HungerSyncPacket::toBytes)
                .consumerMainThread(HungerSyncPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}