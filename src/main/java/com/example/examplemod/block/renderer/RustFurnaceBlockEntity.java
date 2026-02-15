package com.example.examplemod.block.renderer;

import com.example.examplemod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.example.examplemod.world.inventory.RustFurnaceMenu;

public class RustFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public RustFurnaceBlockEntity(BlockPos pos, BlockState state) {
        // [핵심] 여기서 우리 BE 타입과 요리 방식(SMELTING)을 직접 주입합니다.
        super(ModBlockEntities.RUST_FURNACE_BE.get(), pos, state, RecipeType.SMELTING);
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("Rust Furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
        // [핵심] 메뉴를 생성할 때 우리 커스텀 메뉴를 반환합니다.
        return new RustFurnaceMenu(id, playerInventory, this, this.dataAccess);
    }

    // 외부에서 데이터를 가져갈 수 있게 노출
    public ContainerData getContainerData() {
        return this.dataAccess;
    }
}