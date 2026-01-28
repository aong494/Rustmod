package com.example.examplemod.gui;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;

public class RustStyleInventoryScreenEnhanced extends AbstractContainerScreen<InventoryMenu> {
    
    private static final ResourceLocation RUST_INVENTORY_TEXTURE =
            ResourceLocation.tryParse("examplemod:textures/gui/inventory_rust.png");
    
    private static final ResourceLocation SLOT_NORMAL =
            ResourceLocation.tryParse("examplemod:textures/gui/slot_normal.png");
    
    private static final ResourceLocation SLOT_SELECTED =
            ResourceLocation.tryParse("examplemod:textures/gui/slot_selected.png");
    
    private float xMouse;
    private float yMouse;
    
    // 러스트 스타일 색상 정의
    private static final int RUST_DARK_GRAY = 0x2B2B2B;
    private static final int RUST_MEDIUM_GRAY = 0x3F3F3F;
    private static final int RUST_LIGHT_GRAY = 0x5A5A5A;
    private static final int RUST_HIGHLIGHT = 0xD4AF37;
    private static final int RUST_TEXT_COLOR = 0xC9C9C9;
    private static final int RUST_SLOT_BG = 0x1A1A1A;

    public RustStyleInventoryScreenEnhanced(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 러스트 스타일 배경 (어두운 테마)
        renderRustBackground(guiGraphics, x, y);
        
        // 슬롯 배경 렌더링
        renderSlotBackgrounds(guiGraphics, x, y);
        
        // 플레이어 모델 렌더링
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            guiGraphics, 
            x + 51, 
            y + 75, 
            30, 
            (float)(x + 51) - this.xMouse, 
            (float)(y + 75 - 50) - this.yMouse, 
            this.minecraft.player
        );
        
        // 슬롯 하이라이트
        renderSlotHighlights(guiGraphics, mouseX, mouseY, x, y);
    }
    
    private void renderRustBackground(GuiGraphics guiGraphics, int x, int y) {
        // 메인 배경
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000 | RUST_DARK_GRAY);
        
        // 상단 헤더 바
        guiGraphics.fill(x, y, x + this.imageWidth, y + 18, 0xFF000000 | RUST_MEDIUM_GRAY);
        
        // 테두리 (러스트 스타일)
        // 상단
        guiGraphics.fill(x, y, x + this.imageWidth, y + 1, 0xFF000000 | RUST_LIGHT_GRAY);
        // 하단
        guiGraphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF000000 | RUST_LIGHT_GRAY);
        // 좌측
        guiGraphics.fill(x, y, x + 1, y + this.imageHeight, 0xFF000000 | RUST_LIGHT_GRAY);
        // 우측
        guiGraphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000 | RUST_LIGHT_GRAY);
        
        // 강조선 (헤더 하단)
        guiGraphics.fill(x, y + 18, x + this.imageWidth, y + 19, 0xFF000000 | RUST_HIGHLIGHT);
        
        // 인벤토리 섹션 구분선
        guiGraphics.fill(x + 7, y + 83, x + this.imageWidth - 7, y + 84, 0xFF000000 | RUST_MEDIUM_GRAY);
    }
    
    private void renderSlotBackgrounds(GuiGraphics guiGraphics, int x, int y) {
        // 각 슬롯에 어두운 배경 추가 (러스트 스타일)
        for (Slot slot : this.menu.slots) {
            int slotX = x + slot.x;
            int slotY = y + slot.y;
            
            // 슬롯 배경
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF000000 | RUST_SLOT_BG);
            
            // 슬롯 테두리
            // 상단/좌측 (밝은 테두리)
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0x80000000 | RUST_MEDIUM_GRAY);
            guiGraphics.fill(slotX - 1, slotY - 1, slotX, slotY + 17, 0x80000000 | RUST_MEDIUM_GRAY);
            
            // 하단/우측 (어두운 테두리)
            guiGraphics.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0x80000000 | RUST_DARK_GRAY);
            guiGraphics.fill(slotX + 16, slotY - 1, slotX + 17, slotY + 17, 0x80000000 | RUST_DARK_GRAY);
        }
    }
    
    private void renderSlotHighlights(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (this.hoveredSlot != null) {
            int slotX = x + this.hoveredSlot.x;
            int slotY = y + this.hoveredSlot.y;
            
            // 러스트 스타일 하이라이트 (금색 테두리)
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            
            // 외부 글로우 효과
            guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY - 1, 0x80000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX - 2, slotY + 17, slotX + 18, slotY + 18, 0x80000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX - 2, slotY - 1, slotX - 1, slotY + 17, 0x80000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX + 17, slotY - 1, slotX + 18, slotY + 17, 0x80000000 | RUST_HIGHLIGHT);
            
            // 내부 하이라이트
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0xFF000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX - 1, slotY + 16, slotX + 17, slotY + 17, 0xFF000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX - 1, slotY, slotX, slotY + 16, 0xFF000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX + 16, slotY, slotX + 17, slotY + 16, 0xFF000000 | RUST_HIGHLIGHT);
            
            RenderSystem.disableBlend();
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 러스트 스타일 타이틀 (밝은 회색)
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, RUST_TEXT_COLOR, false);
        
        // 인벤토리 라벨 제거 (러스트 스타일에서는 표시 안 함)
        // guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, RUST_TEXT_COLOR, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            // 러스트 스타일 툴팁
            guiGraphics.renderTooltip(this.font, this.hoveredSlot.getItem(), x, y);
        }
    }
    
    @Override
    public boolean isPauseScreen() {
        return false; // 러스트처럼 인벤토리 열어도 게임 일시정지 안 함
    }
}
