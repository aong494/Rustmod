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

public class RustStyleInventoryScreen extends AbstractContainerScreen<InventoryMenu> {
    
    private static final ResourceLocation RUST_INVENTORY_TEXTURE =
            ResourceLocation.tryParse("examplemod:textures/gui/inventory_rust.png");
    
    private static final ResourceLocation SLOT_NORMAL =
            ResourceLocation.tryParse("examplemod:textures/gui/slot_normal.png");
    
    private static final ResourceLocation SLOT_SELECTED =
            ResourceLocation.tryParse("examplemod:textures/gui/slot_selected.png");
    
    private float xMouse;
    private float yMouse;

    public RustStyleInventoryScreen(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
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
        
        // 러스트 스타일 배경 렌더링
        guiGraphics.blit(RUST_INVENTORY_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        
        // 플레이어 모델 렌더링 (러스트 스타일로 약간 오른쪽에 배치)
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            guiGraphics, 
            x + 51, 
            y + 75, 
            30, 
            (float)(x + 51) - this.xMouse, 
            (float)(y + 75 - 50) - this.yMouse, 
            this.minecraft.player
        );
        
        // 커스텀 슬롯 하이라이트
        renderSlotHighlights(guiGraphics, mouseX, mouseY, x, y);
    }
    
    private void renderSlotHighlights(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        // 마우스가 올라간 슬롯에 하이라이트 효과
        if (this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            int slotX = x + this.hoveredSlot.x;
            int slotY = y + this.hoveredSlot.y;
            
            // 러스트 스타일 슬롯 선택 효과
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);
            guiGraphics.blit(SLOT_SELECTED, slotX - 1, slotY - 1, 0, 0, 18, 18, 18, 18);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 러스트 스타일로 타이틀 렌더링 (어두운 색상)
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x3F3F3F, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
            // 러스트 스타일 툴팁 (아이템 정보를 더 상세하게 표시할 수 있음)
            guiGraphics.renderTooltip(this.font, this.hoveredSlot.getItem(), x, y);
        }
    }
}
