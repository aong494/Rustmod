package com.example.examplemod.menu;

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

    // 9x3(작은 상자) 또는 9x6(큰 상자) 여부에 따라 행 개수를 조절합니다.
    public RustChestMenu(MenuType<?> type, int id, Inventory playerInventory, Container container, int rows) {
        super(type, id);
        this.container = container;
        this.containerRows = rows;
        container.startOpen(playerInventory.player);

        // 1. 상자 슬롯 (0 ~ N)
        for (int i = 0; i < this.containerRows * 9; ++i) {
            this.addSlot(new Slot(container, i, 0, 0)); // 좌표는 스크린의 init에서 재설정됨
        }

        // 2. 플레이어 일반 인벤토리 (상자 슬롯 이후)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 0, 0));
            }
        }

        // 3. 핫바
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 0, 0));
        }

        // 4. [핵심] 장비 슬롯 (갑옷 4종)
        for (int i = 0; i < 4; ++i) {
            final EquipmentSlot slotType = EquipmentSlot.values()[5 - i]; // HEAD, CHEST, LEGS, FEET 순서
            this.addSlot(new Slot(playerInventory, 39 - i, 0, 0) {
                @Override
                public boolean mayPlace(ItemStack stack) { return stack.canEquip(slotType, playerInventory.player); }
                @Override
                public boolean mayPickup(Player player) {
                    ItemStack itemstack = this.getItem();
                    return (itemstack.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(player);
                }
            });
        }

        // 5. 보조손 슬롯
        this.addSlot(new Slot(playerInventory, 40, 0, 0));
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