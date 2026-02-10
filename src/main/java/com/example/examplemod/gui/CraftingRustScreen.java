package com.example.examplemod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CraftingRustScreen extends RustStyleChestScreen {
    private static final ResourceLocation CRAFT_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/crafting_rust.png");
    public static int clientAmount = 1;
    public CraftingRustScreen(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        setupCraftingSlots();
    }
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // [핵심] 모드의 폰트 렌더러를 사용하여 텍스트 출력
        // x, y 좌표는 수량 조절 버튼(Slot 7) 위치에 맞춰 조정하세요.
        int amountX = this.leftPos + 388; // 예시 좌표
        int amountY = this.topPos + 280;

        // 서버에서 받은 수량 변수 (예: clientAmount)
        String text = String.valueOf(this.clientAmount);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 300); // 슬롯보다 위에 출력

        // 중앙 정렬하여 그리기
        guiGraphics.drawCenteredString(this.font, text, amountX, amountY, 0xFFFFFFFF);

        guiGraphics.pose().popPose();
    }
    // CraftingRustScreen.java
    @Override
    protected void renderCustomSlotHighlight(GuiGraphics guiGraphics, int sx, int sy, Slot slot) {
        RenderSystem.disableDepthTest();

        if (slot.index >= 0 && slot.index <= 5) {
            // 탭 버튼 전용 수치
            guiGraphics.fill(sx - 7, sy - 7, sx + 47, sy + 11, 0xAAFFFFFF);
        }
        else if (slot.index == 52) {
            // 제작 버튼 전용 수치
            guiGraphics.fill(sx - 3, sy - 3, sx + 32, sy + 21, 0xAAFFFFFF);
        }
        else {
            // 나머지는 부모(InventoryScreen)의 24x24 기본형 사용
            super.renderCustomSlotHighlight(guiGraphics, sx, sy, slot);
        }

        RenderSystem.enableDepthTest();
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // 배경 텍스처 출력
        guiGraphics.blit(CRAFT_TEXTURE, guiX, guiY, 0, 0, this.imageWidth, this.imageHeight, 512, 396);

        // 캐릭터 모델 출력
        renderPlayerModel(guiGraphics, guiX + 60, guiY + 300, mouseX, mouseY);
    }
    private void setupCraftingSlots() {
        int slotImageSize = 24;
        int gap = 4;
        int sSize = slotImageSize + gap; // 28
        int offset = (slotImageSize - 18) / 2; // 바닐라 18px 슬롯을 24px 중앙에 맞춤

        // --- [1] 인벤토리 및 핫바 위치 (RustStyleInventoryScreen과 동일) ---
        int sX = 174; // 인벤토리 뭉치 시작 X
        int sY = 255; // 인벤토리 뭉치 시작 Y
        int hotbarY = sY + (4 * sSize) + 5; // 핫바 Y 좌표

        // ChestMenu 슬롯 인덱스: 0~53(상자), 54~80(인벤토리), 81~89(핫바)

        // 플레이어 메인 인벤토리 (54~80번 슬롯 -> 6x4 배치)
        for (int i = 0; i < 24; i++) {
            Slot invSlot = this.menu.slots.get(i + 54);
            int row = i / 6;
            int col = i % 6;
            setSlotPos(invSlot, (col * sSize) + sX + offset, (row * sSize) + sY + offset);
        }
        // 남은 3칸 숨김
        for (int i = 24; i < 27; i++) setSlotPos(this.menu.slots.get(i + 54), -2000, -2000);

        // 플레이어 핫바 (81~89번 슬롯 -> 6칸 가로 배치)
        for (int i = 0; i < 9; i++) {
            Slot hotbarSlot = this.menu.slots.get(i + 81);
            if (i < 6) {
                setSlotPos(hotbarSlot, (i * sSize) + sX + offset, hotbarY + offset);
            } else {
                setSlotPos(hotbarSlot, -2000, -2000);
            }
        }

        for (int i = 0; i < 54; i++) {
            Slot slot = this.menu.slots.get(i);

            // 1. 상단 탭 (0~5)
            if (i >= 0 && i <= 5) {
                int centerX = CraftingGuiConfigs.TAB_X + 3; // 7에서 3으로 조정 (예시)
                int centerY = (CraftingGuiConfigs.TAB_Y + (i * CraftingGuiConfigs.TAB_SPACING_Y)) + 0;
                setSlotPos(slot, centerX, centerY);
            }
            // 2. 수량 조절 (6~8)
            else if (i >= 6 && i <= 8) {
                setSlotPos(slot, CraftingGuiConfigs.QTY_X + ((i - 6) * sSize), CraftingGuiConfigs.QTY_Y);
            }
            // --- 3. 제작 레시피 목록 (9~44) - 가로 9칸 격자 ---
            else if (i >= 9 && i <= 44) {
                int index = i - 9;
                int col = index % 9; // 가로 9칸 기준 열 번호
                int row = index / 9; // 행 번호
                setSlotPos(slot, CraftingGuiConfigs.LIST_X + (col * sSize), CraftingGuiConfigs.LIST_Y + (row * sSize));
            }
            // 4. 제작 대기열 (45~50)
            else if (i >= 45 && i <= 50) {
                setSlotPos(slot, CraftingGuiConfigs.QUEUE_X + ((i - 45) * sSize) + offset, CraftingGuiConfigs.QUEUE_Y + offset);
            }
            // 5. 선택된 아이템 표시 칸 (51번 슬롯)
            else if (i == 51) {
                int selectIconX = CraftingGuiConfigs.CRAFT_BTN_X - sSize;
                int selectIconY = CraftingGuiConfigs.CRAFT_BTN_Y;
                setSlotPos(slot, selectIconX + offset, selectIconY + offset);
            }
            // 6. 제작 버튼 (52번 슬롯)
            else if (i == 52) {
                setSlotPos(slot, CraftingGuiConfigs.CRAFT_BTN_X + offset, CraftingGuiConfigs.CRAFT_BTN_Y + offset);
            }
            //남는 슬롯은 화면 밖으로
            else {
                setSlotPos(slot, -2000, -2000);
            }
        }
        this.armorStartX = 0;
        this.armorStartY = 325;
        this.extraX0 = armorStartX + (4 * sSize) + offset;
        this.extraY0 = armorStartY + offset;
        this.extraX1 = armorStartX + (5 * sSize) + offset;
        this.extraY1 = armorStartY + offset;
    }
    // 부모 클래스의 렌더링 로직을 분리해서 호출하기 편하게 만듭니다.
    private void renderPlayerModel(GuiGraphics g, int x, int y, int mx, int my) {
        net.minecraft.client.gui.screens.inventory.InventoryScreen.renderEntityInInventoryFollowsMouse(
                g, x, y, 70, (float)x - mx, (float)y - 50 - my, this.minecraft.player
        );
    }
    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        Slot slot = this.findHoveredSlotByPos(pX, pY);
        if (slot != null) {
            double mx = pMouseX - (double)this.leftPos;
            double my = pMouseY - (double)this.topPos;
        if (slot != null && slot.index >= 0 && slot.index <= 5) {
            double mouseX = pMouseX - (double)this.leftPos;
            double mouseY = pMouseY - (double)this.topPos;
            return mouseX >= (double)(slot.x - 7) && mouseX < (double)(slot.x + 47) &&
                    mouseY >= (double)(slot.y - 7) && mouseY < (double)(slot.y + 11);
        }
        else if (slot.index == 52) {
            // 제작 버튼 하이라이트 범위와 동일하게 맞춤
            return mx >= (double)(slot.x - 3) && mx < (double)(slot.x - 3 + 35) &&
                    my >= (double)(slot.y - 3) && my < (double)(slot.y - 3 + 24);
        }
        }
        return super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
}
    // 좌표를 통해 슬롯을 역추적하는 보조 메서드
    private Slot findHoveredSlotByPos(int x, int y) {
        for (Slot slot : this.menu.slots) {
            if (slot.x == x && slot.y == y) return slot;
        }
        return null;
    }
}