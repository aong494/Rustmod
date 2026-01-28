package com.example.examplemod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import com.mojang.blaze3d.systems.RenderSystem;

public class RustStyleLargeChestScreen extends RustStyleChestScreen {
    private static final ResourceLocation LARGE_GUI_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/large_chest_rust.png");

    public RustStyleLargeChestScreen(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // 작은 상자와 동일한 크기 설정
        this.imageWidth = 512;
        this.imageHeight = 396;
    }

    @Override
    protected void init() {
        // AbstractContainerScreen의 기본 init을 호출하여 leftPos/topPos를 계산합니다.
        super.init();

        int slotSize = 28;
        int offset = 3;

        // --- [1] 큰 상자 슬롯 배치 (0 ~ 53번) ---
        // 512x396 공간에 9줄을 넣으려면 시작 Y축을 대폭 올려야 합니다.
        int chestStartX = 348;
        int chestStartY = 106; // 작은 상자(199)보다 훨씬 위에서 시작

        for (int i = 0; i < 54; i++) {
            Slot slot = this.menu.slots.get(i);
            int row = i / 6; // 가로 6칸 기준
            int col = i % 6;

            // 부모 클래스의 setSlotPos를 사용하여 상대 좌표로 배치
            setSlotPos(slot, (col * slotSize) + chestStartX + offset, (row * slotSize) + chestStartY + offset);
        }

        // --- [2] 플레이어 인벤토리 배치 (54 ~ 80번) ---
        int invStartX = 174;
        int invStartY = 255; // 작은 상자와 동일한 위치 유지

        for (int i = 0; i < 24; i++) {
            Slot slot = this.menu.slots.get(i + 54);
            int row = i / 6;
            int col = i % 6;
            setSlotPos(slot, (col * slotSize) + invStartX + offset, (row * slotSize) + invStartY + offset);
        }

        // 안 쓰는 3칸 숨김
        for (int i = 24; i < 27; i++) setSlotPos(this.menu.slots.get(i + 54), -2000, -2000);

        // --- [3] 핫바 배치 (81 ~ 89번) ---
        int hotbarY = invStartY + (4 * slotSize) + 5;
        for (int i = 0; i < 9; i++) {
            Slot slot = this.menu.slots.get(i + 81);
            if (i < 6) {
                setSlotPos(slot, (i * slotSize) + invStartX + offset, hotbarY + offset);
            } else {
                setSlotPos(slot, -2000, -2000);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        // 텍스처 크기를 512, 396으로 고정하여 출력
        guiGraphics.blit(LARGE_GUI_TEXTURE, guiX, guiY, 0, 0, this.imageWidth, this.imageHeight, 512, 396);

        // 캐릭터 렌더링
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics, guiX + 60, guiY + 300, 70,
                (float)(guiX + 80) - mouseX, (float)(guiY + 140) - mouseY, this.minecraft.player
        );
        RenderSystem.disableBlend();
    }
}