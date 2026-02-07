package com.example.examplemod.gui;

import com.example.examplemod.ClientStats;
import com.example.examplemod.ModMessages;
import com.example.examplemod.ArmorSwapPacket;
import com.example.examplemod.capability.PlayerGearCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public abstract class RustStyleFurnaceScreenBase<T extends net.minecraft.world.inventory.AbstractFurnaceMenu>
        extends AbstractContainerScreen<T> implements net.minecraft.client.gui.screens.inventory.MenuAccess<T> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/furnace_rust.png");
    private static final ResourceLocation STATS_BG = ResourceLocation.tryParse("examplemod:textures/gui/icons/stats_bg.png");
    private static final ResourceLocation INSUL_ICON = ResourceLocation.tryParse("examplemod:textures/gui/icons/insulation_icon.png");
    private static final ResourceLocation RAD_ICON = ResourceLocation.tryParse("examplemod:textures/gui/icons/radiation_icon.png");
    private static final ResourceLocation WIDGETS_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/furnace_widgets.png");

    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 396;

    private double mouseX;
    private double mouseY;

    private static final Field slotXField = ObfuscationReflectionHelper.findField(Slot.class, "f_40220_");
    private static final Field slotYField = ObfuscationReflectionHelper.findField(Slot.class, "f_40221_");

    static {
        slotXField.setAccessible(true);
        slotYField.setAccessible(true);
    }

    protected ItemStack extraSlot0 = ItemStack.EMPTY;
    protected ItemStack extraSlot1 = ItemStack.EMPTY;
    protected int extraX0, extraY0, extraX1, extraY1;
    protected int armorStartX = 0;
    protected int armorStartY = 325;

    public RustStyleFurnaceScreenBase(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 512;
        this.imageHeight = 396;
    }

    // 배경 이미지를 각 장치 클래스에서 지정할 수 있게 추상 메서드로 만듭니다.
    protected abstract ResourceLocation getBackgroundTexture();

    protected void setSlotPos(Slot slot, int x, int y) {
        try {
            slotXField.set(slot, x);
            slotYField.set(slot, y);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void init() {
        super.init();
        int sSize = 28; // 슬롯 간격 (24px 슬롯 + 4px 여백)
        int offset = 3; // 슬롯 배경 이미지와 실제 아이템 위치 정렬용

        // 1. 화로 영역 기준점
        int fx = 348;
        int fy = 287;

        // --- [화로 개별 슬롯 위치 조절] ---
        // 아래 숫자들을 수정하여 텍스처 디자인에 딱 맞게 배치하세요.
        int inputX = fx + 70;
        int inputY = fy;
        setSlotPos(this.menu.slots.get(0), inputX + offset, inputY + offset); // 재료

        int fuelX = fx + 22;
        int fuelY = fy;
        setSlotPos(this.menu.slots.get(1), fuelX + offset, fuelY + offset); // 연료

        int resultX = fx + 118;
        int resultY = fy;
        setSlotPos(this.menu.slots.get(2), resultX + offset, resultY + offset); // 결과물


        // 2. 플레이어 인벤토리 (메인 24칸)
        int invX = 174;
        int invY = 255;
        for (int i = 0; i < 24; i++) {
            setSlotPos(this.menu.slots.get(i + 3), (i % 6 * sSize) + invX + offset, (i / 6 * sSize) + invY + offset);
        }
        // 사용하지 않는 인벤토리 슬롯 3칸 숨김
        for (int i = 24; i < 27; i++) setSlotPos(this.menu.slots.get(i + 3), -2000, -2000);

        // 3. 핫바 (하단 6칸)
        for (int i = 0; i < 9; i++) {
            Slot slot = this.menu.slots.get(i + 30);
            if (i < 6) {
                setSlotPos(slot, (i * sSize) + invX + offset, invY + (4 * sSize) + 5 + offset);
            } else {
                setSlotPos(slot, -2000, -2000);
            }
        }

        // 4. 커스텀 장비 슬롯 (갑옷 옆 2칸)
        this.extraX0 = armorStartX + (4 * sSize) + offset;
        this.extraY0 = armorStartY + offset;
        this.extraX1 = armorStartX + (5 * sSize) + offset;
        this.extraY1 = armorStartY + offset;

        // 데이터 로드
        this.minecraft.player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
            this.extraSlot0 = cap.inventory.getStackInSlot(0).copy();
            this.extraSlot1 = cap.inventory.getStackInSlot(1).copy();
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 마우스 좌표 저장 (findHoveredSlot에서 사용됨)
        this.mouseX = (double)mouseX;
        this.mouseY = (double)mouseY;

        this.renderBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // 화로 애니메이션 (불꽃, 화살표)
        renderFurnaceAnimations(guiGraphics);

        // [중요] 현재 마우스 아래에 있는 슬롯 찾기
        // 이 코드가 있어야 super.renderTooltip이 어떤 아이템 정보를 띄울지 알 수 있습니다.
        this.hoveredSlot = findHoveredSlot();

        // 슬롯 및 장비 렌더링
        renderSlots(guiGraphics, mouseX, mouseY);
        renderStats(guiGraphics);

        // 툴팁 렌더링 (가장 마지막)
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderFurnaceAnimations(GuiGraphics g) {
        int fx = this.leftPos + 356;
        int fy = this.topPos + 239;

        // 1. 연료 불꽃 애니메이션
        if (this.menu.isLit()) {
            int lit = this.menu.getLitProgress(); // 0 ~ 13

            // 화면에 그릴 위치 (슬롯 배치에 맞춰 조절)
            int flameX = fx + 23 + 20;
            int flameY = fy + 36 + 17;

            // 새로운 WIDGETS_TEXTURE 사용
            // blit(텍스처, 화면X, 화면Y, 소스U, 소스V, 가로, 세로, 소스텍스처폭, 소스텍스처높이)
            g.blit(WIDGETS_TEXTURE,
                    flameX, flameY + (12 - lit),
                    0, 12 - lit,  // widgets.png 내부의 불꽃 좌표 (0,0 기준일 때)
                    14, lit + 1,
                    32, 32);      // widgets.png가 32x32일 경우
        }

        // 2. 화살표 진행도 애니메이션
        int burn = this.menu.getBurnProgress(); // 0 ~ 24

        int arrowX = fx + 47 + 39;
        int arrowY = fy + 36 + 17;

        g.blit(WIDGETS_TEXTURE,
                arrowX, arrowY,
                0, 15,            // widgets.png 내부의 화살표 좌표 (0,15 기준일 때)
                burn + 1, 16,
                32, 32);
    }
    private void setupSlots() {
        int sSize = 28;
        int offset = 3;
        int chestStartX = 348;
        int chestStartY = 199;
        // 상자 27칸
        for (int i = 0; i < 27; i++) {
            Slot slot = this.menu.slots.get(i);
            int row = i / 6; int col = i % 6;
            if (i < 24) setSlotPos(slot, (col * sSize) + chestStartX + offset, (row * sSize) + chestStartY + offset);
            else setSlotPos(slot, ((i - 24) * sSize) + chestStartX + offset, (4 * sSize) + chestStartY + offset);
        }
        // 플레이어 인벤토리
        int invStartX = 174;
        int invStartY = 255;
        for (int i = 0; i < 24; i++) {
            setSlotPos(this.menu.slots.get(i + 27), (i % 6 * sSize) + invStartX + offset, (i / 6 * sSize) + invStartY + offset);
        }
        for (int i = 24; i < 27; i++) setSlotPos(this.menu.slots.get(i + 27), -2000, -2000);
        // 핫바 6칸
        for (int i = 0; i < 9; i++) {
            Slot slot = this.menu.slots.get(i + 54);
            if (i < 6) setSlotPos(slot, (i * sSize) + invStartX + offset, invStartY + (4 * sSize) + 5 + offset);
            else setSlotPos(slot, -2000, -2000);
        }
    }
    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 1. 바닐라 슬롯(상자 내부, 인벤토리 내부) 툴팁 우선 처리
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        // 2. 이미 바닐라 슬롯 위에 마우스가 있다면 커스텀 체크 생략
        if (this.hoveredSlot != null) return;

        // 3. 갑옷 슬롯 4칸 툴팁 체크
        if (this.minecraft.player != null) {
            for (int i = 0; i < 4; i++) {
                int slotX = armorStartX + (i * 28) + 3;
                int slotY = armorStartY + 3;
                if (isHovering(slotX, slotY, 18, 18, mouseX, mouseY)) {
                    ItemStack armorStack = this.minecraft.player.getInventory().armor.get(3 - i);
                    if (!armorStack.isEmpty()) {
                        guiGraphics.renderTooltip(this.font, armorStack, mouseX, mouseY);
                    }
                }
            }
        }

        // 4. 커스텀 추가 슬롯(extraSlot0) 툴팁 체크
        if (isHovering(extraX0, extraY0, 18, 18, mouseX, mouseY)) {
            if (!extraSlot0.isEmpty()) {
                guiGraphics.renderTooltip(this.font, extraSlot0, mouseX, mouseY);
            }
        }

        // 5. 커스텀 추가 슬롯(extraSlot1) 툴팁 체크
        if (isHovering(extraX1, extraY1, 18, 18, mouseX, mouseY)) {
            if (!extraSlot1.isEmpty()) {
                guiGraphics.renderTooltip(this.font, extraSlot1, mouseX, mouseY);
            }
        }
    }
    @javax.annotation.Nullable
    private Slot findHoveredSlot() {
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            // 슬롯이 활성화 상태고 좌표가 화면 안에 있을 때만 감지
            if (slot.isActive() && slot.x > -1000) {
                if (this.isHovering(slot.x, slot.y, 18, 18, this.mouseX, this.mouseY)) {
                    return slot;
                }
            }
        }
        return null;
    }
    private void renderStats(GuiGraphics g) {
        int statsX = this.leftPos + this.imageWidth - 428;
        int statsYStart = this.topPos + 285;
        drawStatBar(g, statsX, statsYStart, String.valueOf(ClientStats.insulation), INSUL_ICON);
        drawStatBar(g, statsX, statsYStart + 20, String.valueOf(ClientStats.radiation), RAD_ICON);
    }
    private void drawStatBar(GuiGraphics g, int x, int y, String value, ResourceLocation icon) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.9f);
        g.blit(STATS_BG, x, y, 0, 0, 80, 16, 80, 16);
        g.drawString(this.font, value, x + 55, y + 4, 0xFFFFFFFF, true);
        g.blit(icon, x + 65, y + 2, 0, 0, 12, 12, 12, 12);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        // 추가 슬롯 0번 클릭
        if (isHovering(extraX0, extraY0, 18, 18, pMouseX, pMouseY)) {
            handleCustomSlotClick(0);
            return true;
        }
        // 추가 슬롯 1번 클릭
        if (isHovering(extraX1, extraY1, 18, 18, pMouseX, pMouseY)) {
            handleCustomSlotClick(1);
            return true;
        }
        // 갑옷 4칸 클릭 (ArmorSwapPacket 0~3번 호출)
        for (int i = 0; i < 4; i++) {
            if (isHovering(armorStartX + (i * 28) + 3, armorStartY + 3, 18, 18, pMouseX, pMouseY)) {
                ModMessages.sendToServer(new ArmorSwapPacket(3 - i));
                return true;
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    private void handleCustomSlotClick(int index) {
        ItemStack carried = this.menu.getCarried();
        if (!carried.isEmpty() && !PlayerGearCapability.isItemValid(index, carried)) return;

        ModMessages.sendToServer(new ArmorSwapPacket(index + 4));

        // 클라이언트 시뮬레이션
        if (index == 0) {
            ItemStack old = this.extraSlot0.copy();
            this.extraSlot0 = carried.copy();
            this.menu.setCarried(old);
        } else {
            ItemStack old = this.extraSlot1.copy();
            this.extraSlot1 = carried.copy();
            this.menu.setCarried(old);
        }
    }
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(getBackgroundTexture(), guiX, guiY, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, guiX + 60, guiY + 300, 70, (float)(guiX + 60) - mouseX, (float)(guiY + 50) - mouseY, this.minecraft.player);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {}
    private void renderSlots(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 1. 바닐라 슬롯(화로 3칸 + 인벤토리 + 핫바) 렌더링
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && slot.x > -1000) {
                int sx = this.leftPos + slot.x;
                int sy = this.topPos + slot.y;
                if (!slot.getItem().isEmpty()) {
                    guiGraphics.renderItem(slot.getItem(), sx, sy);
                    renderSlotDecorations(guiGraphics, slot.getItem(), sx, sy);
                }
                // 마우스 오버 하이라이트
                if (isHovering(slot.x, slot.y, 18, 18, mouseX, mouseY)) {
                    RenderSystem.disableDepthTest();
                    guiGraphics.fill(sx - 3, sy - 3, sx + 21, sy + 21, 0x80FFFFFF);
                    RenderSystem.enableDepthTest();
                }
            }
        }

        // 2. 가상 갑옷 슬롯 렌더링 (캐릭터 왼쪽 4칸)
        if (this.minecraft.player != null) {
            for (int i = 0; i < 4; i++) {
                ItemStack armorStack = this.minecraft.player.getInventory().armor.get(3 - i);
                renderVirtualSlot(guiGraphics, armorStack, armorStartX + (i * 28) + 3, armorStartY + 3, mouseX, mouseY);
            }

            // 3. 커스텀 추가 슬롯 2칸 렌더링 (장갑, 신발 등)
            this.minecraft.player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                renderVirtualSlot(guiGraphics, cap.inventory.getStackInSlot(0), extraX0, extraY0, mouseX, mouseY);
                renderVirtualSlot(guiGraphics, cap.inventory.getStackInSlot(1), extraX1, extraY1, mouseX, mouseY);
            });
        }

        // 4. 마우스가 들고 있는 아이템 그리기
        if (!this.menu.getCarried().isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 250);
            guiGraphics.renderItem(this.menu.getCarried(), mouseX - 8, mouseY - 8);
            renderSlotDecorations(guiGraphics, this.menu.getCarried(), mouseX - 8, mouseY - 8);
            guiGraphics.pose().popPose();
        }
    }

    private void renderVirtualSlot(GuiGraphics g, ItemStack stack, int x, int y, int mx, int my) {
        int sx = this.leftPos + x;
        int sy = this.topPos + y;
        if (!stack.isEmpty()) {
            g.renderItem(stack, sx, sy);
            renderSlotDecorations(g, stack, sx, sy);
        }
        if (isHovering(x, y, 18, 18, mx, my)) {
            RenderSystem.disableDepthTest();
            g.fill(sx - 3, sy - 3, sx + 21, sy + 21, 0x80FFFFFF);
            RenderSystem.enableDepthTest();
        }
    }

    private void renderSlotDecorations(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;

        // 내구도 바
        if (stack.isDamageableItem()) {
            guiGraphics.fill(x - 2, y - 1, x, y + 17, 0xFF222222);
            float damagePos = (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            int barHeight = (int) (17 * damagePos);
            guiGraphics.fill(x - 2, y + 17 - barHeight, x, y + 17, 0xFF5DB31F);
        }

        // 아이템 개수
        if (stack.getCount() > 1) {
            String countText = String.valueOf(stack.getCount());
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + 17, y + 17, 200);
            guiGraphics.pose().scale(0.65f, 0.65f, 0.65f);
            guiGraphics.drawString(this.font, countText, -this.font.width(countText), -this.font.lineHeight, 0xFFFFFFFF, true);
            guiGraphics.pose().popPose();
        }
    }
    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        if (pWidth <= 18 && pHeight <= 18) return super.isHovering(pX - 3, pY - 3, 24, 24, pMouseX, pMouseY);
        return super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
}