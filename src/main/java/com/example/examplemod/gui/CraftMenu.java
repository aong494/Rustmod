package com.example.examplemod.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public class CraftMenu extends AbstractContainerMenu {
    // 54칸짜리 가상 인벤토리 (9x6 규격이지만 좌표는 내 마음대로)
    private final Container container;

    public CraftMenu(int id, Inventory playerInv) {
        // 실제로는 54칸을 사용하므로 SimpleContainer(54)를 생성하거나 전달받음
        this(id, playerInv, new SimpleContainer(54));
    }

    public CraftMenu(int id, Inventory playerInv, Container container) {
        super(ModMenus.CRAFT_MENU.get(), id);
        this.container = container;
        checkContainerSize(container, 54);

        // 1. 상단 탭 (0~5)
        for (int i = 0; i < 6; i++) {
            this.addSlot(new Slot(container, i, CraftingGuiConfigs.TAB_START_X + (i * 20), CraftingGuiConfigs.TAB_START_Y));
        }

        // 2. 수량 조절 (6~8)
        this.addSlot(new Slot(container, 6, CraftingGuiConfigs.MINUS_BTN_X, CraftingGuiConfigs.MINUS_BTN_Y));
        this.addSlot(new Slot(container, 7, CraftingGuiConfigs.MINUS_BTN_X + 30, CraftingGuiConfigs.MINUS_BTN_Y));
        this.addSlot(new Slot(container, 8, CraftingGuiConfigs.PLUS_BTN_X, CraftingGuiConfigs.PLUS_BTN_Y));

        // 3. 제작 아이템 목록 (9~44) - 9칸씩 4줄 배치 예시
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(container, 9 + (col + row * 9), 8 + col * 18, 40 + row * 18));
            }
        }

        // 4. 대기열 (45~51)
        for (int i = 0; i < 7; i++) {
            this.addSlot(new Slot(container, 45 + i, CraftingGuiConfigs.QUEUE_START_X + (i * 18), CraftingGuiConfigs.QUEUE_START_Y));
        }

        // 5. 제작 버튼 (53)
        this.addSlot(new Slot(container, 53, CraftingGuiConfigs.CRAFT_BTN_X, CraftingGuiConfigs.CRAFT_BTN_Y));

        // 6. 플레이어 인벤토리 (아래쪽에 숨기거나 적절히 배치)
        addPlayerInventory(playerInv);
    }

    private void addPlayerInventory(Inventory playerInv) {
        // 보통 GUI 맨 아래(y=174 등)에 배치합니다.
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 174 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInv, i, 8 + i * 18, 232));
        }
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 쉬프트 클릭 시 아이템 이동 로직 (일단 빈 아이템 반환으로 설정 가능)
        return ItemStack.EMPTY;
    }
}