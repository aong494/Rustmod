package com.example.examplemod.gui;

import com.example.examplemod.ExampleMod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RustInventoryWithTabs extends AbstractContainerScreen<InventoryMenu> {
    
    private static final ResourceLocation RUST_INVENTORY_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/inventory_rust.png");
    
    private float xMouse;
    private float yMouse;
    
    // 러스트 스타일 색상
    private static final int RUST_DARK_BG = 0x1E1E1E;
    private static final int RUST_HEADER_BG = 0x2D2D2D;
    private static final int RUST_BORDER = 0x404040;
    private static final int RUST_HIGHLIGHT = 0xC49B3C;
    private static final int RUST_TEXT = 0xE0E0E0;
    private static final int RUST_SLOT_BG = 0x262626;
    private static final int RUST_TAB_ACTIVE = 0x3A3A3A;
    private static final int RUST_TAB_INACTIVE = 0x2A2A2A;
    
    // 탭 정의
    private enum InventoryTab {
        ALL("전체"),
        WEAPONS("무기"),
        TOOLS("도구"),
        ARMOR("방어구"),
        MATERIALS("재료"),
        FOOD("음식");
        
        private final String displayName;
        
        InventoryTab(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private InventoryTab currentTab = InventoryTab.ALL;
    private EditBox searchBox;

    public RustInventoryWithTabs(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 200; // 탭을 위해 높이 증가
        this.imageWidth = 195; // 너비 약간 증가
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 검색 박스 추가 (러스트 스타일)
        this.searchBox = new EditBox(
            this.font, 
            x + 82, 
            y + 6, 
            90, 
            12, 
            Component.literal("검색...")
        );
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(true);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(RUST_TEXT);
        this.searchBox.setHint(Component.literal("아이템 검색..."));
        this.addWidget(this.searchBox);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 검색 박스 렌더링
        if (this.searchBox != null) {
            this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.xMouse = (float)mouseX;
        this.yMouse = (float)mouseY;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 배경 렌더링
        renderBackground(guiGraphics, x, y);
        
        // 탭 렌더링
        renderTabs(guiGraphics, x, y, mouseX, mouseY);
        
        // 슬롯 배경
        renderSlotBackgrounds(guiGraphics, x, y);
        
        // 플레이어 모델
        InventoryScreen.renderEntityInInventoryFollowsMouse(
            guiGraphics, 
            x + 40, 
            y + 95, 
            25, 
            (float)(x + 40) - this.xMouse, 
            (float)(y + 95 - 50) - this.yMouse, 
            this.minecraft.player
        );
        
        // 슬롯 하이라이트
        renderSlotHighlights(guiGraphics, mouseX, mouseY, x, y);
    }
    
    private void renderBackground(GuiGraphics guiGraphics, int x, int y) {
        // 메인 배경
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF000000 | RUST_DARK_BG);
        
        // 헤더
        guiGraphics.fill(x, y, x + this.imageWidth, y + 22, 0xFF000000 | RUST_HEADER_BG);
        
        // 테두리
        drawBorder(guiGraphics, x, y, this.imageWidth, this.imageHeight, RUST_BORDER);
        
        // 헤더 하단 구분선
        guiGraphics.fill(x, y + 22, x + this.imageWidth, y + 23, 0xFF000000 | RUST_HIGHLIGHT);
        
        // 탭 영역과 본체 구분선
        guiGraphics.fill(x, y + 50, x + this.imageWidth, y + 51, 0xFF000000 | RUST_BORDER);
    }
    
    private void renderTabs(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        int tabX = x + 5;
        int tabY = y + 25;
        int tabWidth = 30;
        int tabHeight = 22;
        int tabSpacing = 2;
        
        for (InventoryTab tab : InventoryTab.values()) {
            boolean isActive = tab == currentTab;
            boolean isHovered = mouseX >= tabX && mouseX < tabX + tabWidth && 
                               mouseY >= tabY && mouseY < tabY + tabHeight;
            
            // 탭 배경
            int tabColor = isActive ? RUST_TAB_ACTIVE : RUST_TAB_INACTIVE;
            if (isHovered && !isActive) {
                tabColor = RUST_TAB_ACTIVE - 0x0A0A0A;
            }
            
            guiGraphics.fill(tabX, tabY, tabX + tabWidth, tabY + tabHeight, 0xFF000000 | tabColor);
            
            // 탭 테두리
            if (isActive) {
                // 상단 하이라이트
                guiGraphics.fill(tabX, tabY, tabX + tabWidth, tabY + 1, 0xFF000000 | RUST_HIGHLIGHT);
            } else {
                drawBorder(guiGraphics, tabX, tabY, tabWidth, tabHeight, RUST_BORDER);
            }
            
            // 탭 텍스트 (축약)
            String tabText = tab.getDisplayName().substring(0, Math.min(2, tab.getDisplayName().length()));
            int textX = tabX + (tabWidth - this.font.width(tabText)) / 2;
            int textY = tabY + (tabHeight - this.font.lineHeight) / 2;
            int textColor = isActive ? RUST_HIGHLIGHT : RUST_TEXT;
            guiGraphics.drawString(this.font, tabText, textX, textY, textColor, false);
            
            tabX += tabWidth + tabSpacing;
        }
    }
    
    private void renderSlotBackgrounds(GuiGraphics guiGraphics, int x, int y) {
        String searchText = this.searchBox != null ? this.searchBox.getValue().toLowerCase() : "";
        
        for (Slot slot : this.menu.slots) {
            ItemStack stack = slot.getItem();
            
            // 검색 필터
            if (!searchText.isEmpty() && !stack.isEmpty()) {
                String itemName = stack.getHoverName().getString().toLowerCase();
                if (!itemName.contains(searchText)) {
                    continue; // 검색어와 맞지 않으면 어둡게 표시
                }
            }
            
            int slotX = x + slot.x;
            int slotY = y + slot.y;
            
            // 슬롯 배경
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF000000 | RUST_SLOT_BG);
            
            // 슬롯 테두리 (입체감)
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY, 0x60000000 | RUST_BORDER);
            guiGraphics.fill(slotX - 1, slotY - 1, slotX, slotY + 17, 0x60000000 | RUST_BORDER);
        }
    }
    
    private void renderSlotHighlights(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (this.hoveredSlot != null) {
            int slotX = x + this.hoveredSlot.x;
            int slotY = y + this.hoveredSlot.y;
            
            RenderSystem.enableBlend();
            
            // 외부 글로우
            guiGraphics.fill(slotX - 2, slotY - 2, slotX + 18, slotY - 1, 0x60000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX - 2, slotY + 17, slotX + 18, slotY + 18, 0x60000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX - 2, slotY - 1, slotX - 1, slotY + 17, 0x60000000 | RUST_HIGHLIGHT);
            guiGraphics.fill(slotX + 17, slotY - 1, slotX + 18, slotY + 17, 0x60000000 | RUST_HIGHLIGHT);
            
            // 내부 하이라이트
            drawBorder(guiGraphics, slotX - 1, slotY - 1, 18, 18, RUST_HIGHLIGHT);
            
            RenderSystem.disableBlend();
        }
    }
    
    private void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, 0xFF000000 | color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF000000 | color);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFF000000 | color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF000000 | color);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 타이틀
        guiGraphics.drawString(this.font, "인벤토리", this.titleLabelX, this.titleLabelY, RUST_TEXT, false);
        
        // 현재 탭 표시
        String tabInfo = currentTab.getDisplayName();
        guiGraphics.drawString(this.font, tabInfo, this.titleLabelX, this.titleLabelY + 38, RUST_HIGHLIGHT, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 검색 박스 클릭 처리
        if (this.searchBox != null && this.searchBox.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        // 탭 클릭 처리
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int tabX = x + 5;
        int tabY = y + 25;
        int tabWidth = 30;
        int tabHeight = 22;
        int tabSpacing = 2;
        
        for (InventoryTab tab : InventoryTab.values()) {
            if (mouseX >= tabX && mouseX < tabX + tabWidth && 
                mouseY >= tabY && mouseY < tabY + tabHeight) {
                currentTab = tab;
                return true;
            }
            tabX += tabWidth + tabSpacing;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 검색 박스 키 입력 처리
        if (this.searchBox != null && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // 검색 박스 문자 입력 처리
        if (this.searchBox != null && this.searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
