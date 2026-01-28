package com.example.examplemod.Hunger;

public class ClientHungerData {
    private static float playerHunger;

    public static void set(float hunger) {
        playerHunger = hunger;
    }

    public static float get() {
        return playerHunger;
    }
}