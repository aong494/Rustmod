package com.example.examplemod.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmokerMenu;

public class RustStyleSmokerScreen extends RustStyleFurnaceScreenBase<SmokerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/furnace_rust.png");

    public RustStyleSmokerScreen(SmokerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}