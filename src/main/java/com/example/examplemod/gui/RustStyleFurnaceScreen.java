package com.example.examplemod.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FurnaceMenu;

public class RustStyleFurnaceScreen extends RustStyleFurnaceScreenBase<FurnaceMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/furnace_rust.png");

    public RustStyleFurnaceScreen(FurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected ResourceLocation getBackgroundTexture() {
        return TEXTURE;
    }
}