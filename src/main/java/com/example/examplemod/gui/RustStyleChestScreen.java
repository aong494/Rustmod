package com.example.examplemod.gui;

import com.example.examplemod.ArmorSwapPacket;
import com.example.examplemod.ModMessages;
import com.example.examplemod.SyncExtraSlotPacket; // 패킷 클래스 이름 확인
import com.example.examplemod.capability.PlayerGearCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

public class RustStyleChestScreen extends AbstractContainerScreen<ChestMenu> {
    // 텍스처 경로는 알려주신 대로 chest_rust.png를 사용합니다.
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/chest_rust.png");

    // 인벤토리 화면과 비율을 맞추기 위해 너비를 확장 (예: 512 -> 800 등으로 이미지에 맞춰 조절 필요)
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 396;

    private static final Field slotXField = ObfuscationReflectionHelper.findField(Slot.class, "f_40220_");
    private static final Field slotYField = ObfuscationReflectionHelper.findField(Slot.class, "f_40221_");

    static {
        slotXField.setAccessible(true);
        slotYField.setAccessible(true);
    }
    // 커스텀 슬롯 데이터
    protected ItemStack extraSlot0 = ItemStack.EMPTY;
    protected ItemStack extraSlot1 = ItemStack.EMPTY;
    protected int extraX0, extraY0, extraX1, extraY1;
    protected int armorStartX, armorStartY;

    public RustStyleChestScreen(ChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 512;
        this.imageHeight = 396;
    }
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
        int sSize = 28;
        int offset = 3;
        // --- [1] 상자 슬롯 (0~26) 및 인벤토리 (27~62) 배치 (기존 코드 유지) ---
        setupSlots();
        // --- [2] 장비 및 커스텀 슬롯 좌표 설정 ---
        // 인벤토리 화면과 똑같은 위치로 잡으려면 0, 325 근처가 적당합니다.
        this.armorStartX = 0;
        this.armorStartY = 325;
        // 커스텀 슬롯 2칸 위치 (갑옷 4칸 바로 옆)
        this.extraX0 = armorStartX + (4 * sSize) + offset;
        this.extraY0 = armorStartY + offset;
        this.extraX1 = armorStartX + (5 * sSize) + offset;
        this.extraY1 = armorStartY + offset;
        // 커스텀 슬롯 데이터 로드
        this.minecraft.player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
            this.extraSlot0 = cap.inventory.getStackInSlot(0).copy();
            this.extraSlot1 = cap.inventory.getStackInSlot(1).copy();
        });
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // 1. 일반 슬롯 렌더링
        renderVanillaSlots(guiGraphics, mouseX, mouseY);

        // 2. 갑옷 4칸 렌더링 (인벤토리에서 직접 참조)
        if (this.minecraft.player != null) {
            for (int i = 0; i < 4; i++) {
                // 서버가 보낸 패킷이 inventory.armor에 저장되므로 여기서 실시간 참조
                ItemStack armorStack = this.minecraft.player.getInventory().armor.get(3 - i);
                renderVirtualSlot(guiGraphics, armorStack, armorStartX + (i * 28) + 3, armorStartY + 3, mouseX, mouseY);
            }

            // 3. 커스텀 슬롯 2칸 렌더링 (Capability 직접 참조)
            this.minecraft.player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
                renderVirtualSlot(guiGraphics, cap.inventory.getStackInSlot(0), extraX0, extraY0, mouseX, mouseY);
                renderVirtualSlot(guiGraphics, cap.inventory.getStackInSlot(1), extraX1, extraY1, mouseX, mouseY);
            });
        }

        renderMouseItem(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 1. 갑옷 4칸 클릭 판정
        for (int i = 0; i < 4; i++) {
            if (isHovering(armorStartX + (i * 28) + 3, armorStartY + 3, 18, 18, mouseX, mouseY)) {
                ModMessages.sendToServer(new ArmorSwapPacket(3 - i));
                return true; // 여기서 true를 반환하면 클릭 처리가 완료됨
            }
        }
        // 2. 커스텀 슬롯 0번 (인덱스 4)
        if (isHovering(extraX0, extraY0, 18, 18, mouseX, mouseY)) {
            ModMessages.sendToServer(new ArmorSwapPacket(4));
        // [중요] updateClientExtraSlots() 삭제! 서버가 보내주는 패킷을 기다려야 합니다.
            return true;
        }
        // 3. 커스텀 슬롯 1번 (인덱스 5)
        if (isHovering(extraX1, extraY1, 18, 18, mouseX, mouseY)) {
            ModMessages.sendToServer(new ArmorSwapPacket(5));
            // [중요] updateClientExtraSlots() 삭제!
            return true;
        }
        // 그 외의 영역(상자 안 등) 클릭은 원래대로 처리
        return super.mouseClicked(mouseX, mouseY, button);
    }
    // --- 헬퍼 메서드들 (기존 로직 유지) ---
    private void renderVanillaSlots(GuiGraphics g, int mx, int my) {
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && slot.x > -1000) {
                int sx = this.leftPos + slot.x;
                int sy = this.topPos + slot.y;
                if (!slot.getItem().isEmpty()) {
                    g.renderItem(slot.getItem(), sx, sy);
                    renderSlotDecorations(g, slot.getItem(), sx, sy);
                }
                if (isHovering(slot.x, slot.y, 18, 18, mx, my)) {
                    g.fill(sx-3, sy-3, sx+21, sy+21, 0x80FFFFFF);
                }
            }
        }
    }

    private void renderMouseItem(GuiGraphics g, int mx, int my) {
        if (!this.menu.getCarried().isEmpty()) {
            g.pose().pushPose();
            g.pose().translate(0, 0, 250);
            g.renderItem(this.menu.getCarried(), mx - 8, my - 8);
            renderSlotDecorations(g, this.menu.getCarried(), mx - 8, my - 8);
            g.pose().popPose();
        }
    }

    private void renderSlotDecorations(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        if (stack.isDamageableItem()) {
            guiGraphics.fill(x - 2, y - 1, x, y + 17, 0xFF222222);
            float damagePos = (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            int barHeight = (int) (17 * damagePos);
            guiGraphics.fill(x - 2, y + 17 - barHeight, x, y + 17, 0xFF5DB31F);
        }
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
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI_TEXTURE, guiX, guiY, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, guiX + 60, guiY + 300, 70, (float)(guiX + 80) - mouseX, (float)(guiY + 140) - mouseY, this.minecraft.player);
        RenderSystem.disableBlend();
    }

    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        if (pWidth <= 18 && pHeight <= 18) return super.isHovering(pX - 3, pY - 3, 24, 24, pMouseX, pMouseY);
        return super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }

    @Override protected void renderLabels(GuiGraphics g, int x, int y) {}
}