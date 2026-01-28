package com.example.examplemod.Thirst;

public class ClientThirstData {
    private static float playerThirst;

    public static void set(float thirst) {
        ClientThirstData.playerThirst = thirst;
    }

    public static float get() {
        return playerThirst;
    }
}