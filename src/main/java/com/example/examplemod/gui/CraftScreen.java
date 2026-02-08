package com.example.examplemod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CraftScreen extends AbstractContainerScreen<CraftMenu> {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation("examplemod", "textures/gui/crafting_rust.png");

    public CraftScreen(CraftMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 256;
        this.imageHeight = 256;
    }
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 제목이나 텍스트 위치도 픽셀 단위로 수정 가능
        graphics.drawString(this.font, "Crafting", 8, 6, 4210752, false);
    }
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics); // 어두운 배경 칠하기
        super.render(graphics, mouseX, mouseY, delta);
        renderTooltip(graphics, mouseX, mouseY); // 아이템 정보 표시
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 제작 버튼(Slot 53) 위치에 마우스 오버 시 하이라이트 효과 (옵션)
        if (isHovering(CraftingGuiConfigs.CRAFT_BTN_X, CraftingGuiConfigs.CRAFT_BTN_Y, 18, 18, mouseX, mouseY)) {
            graphics.fill(x + CraftingGuiConfigs.CRAFT_BTN_X, y + CraftingGuiConfigs.CRAFT_BTN_Y,
                    x + CraftingGuiConfigs.CRAFT_BTN_X + 18, y + CraftingGuiConfigs.CRAFT_BTN_Y + 18, 0x55FFFFFF);
        }
    }
}