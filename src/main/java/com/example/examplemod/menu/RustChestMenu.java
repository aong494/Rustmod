package com.example.examplemod.menu;

import com.example.examplemod.capability.PlayerGearCapability;
import com.example.examplemod.gui.CustomGearSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class RustChestMenu extends AbstractContainerMenu {
    private final Container container;
    private final int containerRows;

    public RustChestMenu(MenuType<?> type, int id, Inventory playerInventory, Container container, int rows) {
        super(type, id);
        this.container = container;
        this.containerRows = rows;
        container.startOpen(playerInventory.player);

        // 1. 상자 슬롯 (Index 0 ~ 26)
        for (int i = 0; i < this.containerRows * 9; ++i) {
            this.addSlot(new Slot(container, i, 0, 0));
        }

        // 2. 플레이어 일반 인벤토리 (Index 27 ~ 53)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 0, 0));
            }
        }

        // 3. 핫바 (Index 54 ~ 62)
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 0, 0));
        }

        // 4. 장비 슬롯 (Index 63 ~ 66)
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot slotType = getEquipmentSlotFromLoop(i);
            this.addSlot(new Slot(playerInventory, 39 - i, 0, 0) {
                @Override
                public boolean mayPlace(ItemStack stack) { return stack.canEquip(slotType, playerInventory.player); }
                // ... mayPickup 로직 동일 ...
            });
        }

// 6. 커스텀 슬롯 2개 추가 (이제 Index 67, 68이 됩니다)
        playerInventory.player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
            this.addSlot(new CustomGearSlot(cap.inventory, 0, 0, 0)); // Index 67
            this.addSlot(new CustomGearSlot(cap.inventory, 1, 0, 0)); // Index 68
        });
    }

    // EquipmentSlot 순서를 안전하게 가져오기 위한 헬퍼
    private EquipmentSlot getEquipmentSlotFromLoop(int i) {
        return switch (i) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            default -> EquipmentSlot.FEET;
        };
    }

    // 9x3, 9x6 각각을 위한 정적 생성자 (Registry 등록용)
    public static RustChestMenu threeRows(int id, Inventory inv, Container container) {
        return new RustChestMenu(MenuType.GENERIC_9x3, id, inv, container, 3);
    }

    public static RustChestMenu sixRows(int id, Inventory inv, Container container) {
        return new RustChestMenu(MenuType.GENERIC_9x6, id, inv, container, 6);
    }

    @Override
    public boolean stillValid(Player player) { return this.container.stillValid(player); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 아이템 이동 로직 (Shift-Click) - 기존 ChestMenu 로직을 참고하여 구현 필요
        return ItemStack.EMPTY;
    }
}