package com.example.examplemod.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.crafting.RecipeType;

public class RustFurnaceMenu extends AbstractFurnaceMenu {
    // 클라이언트 생성용
    public RustFurnaceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModMenuTypes.RUST_FURNACE_MENU.get(), RecipeType.SMELTING, net.minecraft.world.inventory.RecipeBookType.FURNACE,
                containerId, playerInventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    // 서버 생성용 (BlockEntity와 연결됨)
    public RustFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.RUST_FURNACE_MENU.get(), RecipeType.SMELTING, net.minecraft.world.inventory.RecipeBookType.FURNACE,
                containerId, playerInventory, container, data);
    }
}