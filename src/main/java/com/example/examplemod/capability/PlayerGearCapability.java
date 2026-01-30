package com.example.examplemod.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerGearCapability {
    // 1. 에러가 났던 그 심볼(열쇠)을 여기서 정의합니다.
    public static final Capability<PlayerGearCapability> GEAR_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    // 2. 에러가 났던 'inventory' 심볼입니다.
    public final ItemStackHandler inventory = new ItemStackHandler(2);

    // 데이터를 NBT 형식으로 변환 (저장용)
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    // NBT에서 데이터를 읽어옴 (불러오기용)
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }

    // 플레이어에게 이 저장소를 붙여주는 'Provider' 클래스
    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerGearCapability gear = new PlayerGearCapability();
        private final LazyOptional<PlayerGearCapability> optional = LazyOptional.of(() -> gear);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return GEAR_CAPABILITY.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() { return gear.serializeNBT(); }

        @Override
        public void deserializeNBT(CompoundTag nbt) { gear.deserializeNBT(nbt); }
    }
    /**
     * 커스텀 슬롯에 들어갈 수 있는 아이템인지 확인하는 공통 로직
     * @param slotIndex 슬롯 번호 (0: 배낭 슬롯, 1: 토템 슬롯)
     * @param stack 검사할 아이템
     */
    public static boolean isItemValid(int slotIndex, ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 0번 슬롯: 배낭만 가능
        if (slotIndex == 0) {
            // 실제 모드 아이템으로 변경 필요 (예: ModItems.BACKPACK.get())
            return stack.getItem().toString().equals("examplemod:backpack");
        }

        // 1번 슬롯: 불사의 토템만 가능
        else if (slotIndex == 1) {
            return stack.is(Items.TOTEM_OF_UNDYING);
        }

        return false;
    }
}