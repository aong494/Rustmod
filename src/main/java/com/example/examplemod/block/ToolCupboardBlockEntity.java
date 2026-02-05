package com.example.examplemod.block;

import com.example.examplemod.block.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.WorldlyContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToolCupboardBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {

    public static final Map<BlockPos, ToolCupboardBlockEntity> ACTIVE_CUPBOARDS = new ConcurrentHashMap<>();

    public NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    public ToolCupboardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOOL_CUPBOARD_BE.get(), pos, state);
    }
    @Override
    public void onLoad() {
        super.onLoad();
        if (this.worldPosition != null) {
            ACTIVE_CUPBOARDS.put(this.worldPosition.immutable(), this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.worldPosition != null) {
            ACTIVE_CUPBOARDS.remove(this.worldPosition);
        }
    }
    @Override
    public int[] getSlotsForFace(Direction side) {
        int[] result = new int[getContainerSize()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }
    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return true; // 외부(호퍼/플러그인)에서 아이템을 넣을 수 있게 허용
    }
    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.literal("도구함");
    }
    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return ChestMenu.threeRows(id, player, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    // 저장 및 불러오기 (아이템 유실 방지)
    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(nbt)) {
            ContainerHelper.loadAllItems(nbt, this.items);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (!this.trySaveLootTable(nbt)) {
            ContainerHelper.saveAllItems(nbt, this.items);
        }
    }
    @Override
    public Component getDisplayName() {
        return Component.literal("도구함");
    }
}