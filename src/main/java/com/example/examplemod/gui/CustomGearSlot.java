package com.example.examplemod.gui;

import com.example.examplemod.capability.PlayerGearCapability; // import 추가
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class CustomGearSlot extends SlotItemHandler {
    public CustomGearSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        // [수정] 공통 로직을 호출하여 검사합니다.
        return PlayerGearCapability.isItemValid(this.getSlotIndex(), stack);
    }
}