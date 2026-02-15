package com.example.examplemod.network;

import com.example.examplemod.gui.MapScreen;

import java.util.UUID;

public class ClientPacketHandler {
    public static void handlePing(float x, float z, boolean isRemove, int index) {
        MapScreen.syncSharedPing(x, z, isRemove, index);
    }
    public static void handleGroupLocation(UUID uuid, String name, double x, double z, int index) {
        MapScreen.updateGroupMember(uuid, name, x, z, index);
    }
    public static void handleBedData(String data) {
        System.out.println("침대 패킷 수신: " + data); // 콘솔에서 확인용
        ClientBedData.handleBedData(data);
    }
}