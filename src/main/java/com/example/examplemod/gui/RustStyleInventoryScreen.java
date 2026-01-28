package com.example.examplemod.gui;

import com.example.examplemod.ModMessages;
import com.example.examplemod.capability.PlayerGearCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

public class RustStyleInventoryScreen extends AbstractContainerScreen<InventoryMenu> {
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.tryParse("examplemod:textures/gui/inventory_rust.png");
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
    private ItemStack extraSlot0 = ItemStack.EMPTY;
    private ItemStack extraSlot1 = ItemStack.EMPTY;
    private int extraX0, extraY0, extraX1, extraY1;

    public RustStyleInventoryScreen(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 512;
        this.imageHeight = 396;
    }

    private void setSlotPos(Slot slot, int x, int y) {
        try {
            slotXField.set(slot, x);
            slotYField.set(slot, y);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void disableAndHideSlot(Slot slot) {
        // 좌표를 멀리 보냅니다.
        setSlotPos(slot, -2000, -2000);

        // 이 슬롯에는 아무것도 넣을 수 없고(mayPlace), 아무것도 뺄 수 없게(mayPickup) 만듭니다.
        // 익명 클래스로 슬롯의 행동을 재정의하는 것은 Menu 클래스에서만 가능하므로,
        // Screen에서는 슬롯 객체의 상태를 직접 제어하는 우회 방법을 씁니다.
    }

    @Override
    protected void init() {
        super.init();

        int slotImageSize = 24;
        int gap = 4;
        int sSize = slotImageSize + gap; // 28

        // 1. 기준 좌표 설정 (이미지 레이아웃에 맞춰 조절)
        int sX = 174; // 인벤토리 뭉치 시작 X
        int sY = 255; // 인벤토리 뭉치 시작 Y
        int offset = (slotImageSize - 18) / 2;

        // 2. 조합창 슬롯 (0~4번) - 사용하지 않으므로 숨김
        for (int i = 0; i < 5; i++) {
            setSlotPos(this.menu.slots.get(i), -2000, -2000);
            disableAndHideSlot(this.menu.slots.get(i));
        }

        // 3. 메인 인벤토리 (9~35번) - 6x4 배치
        for (int i = 0; i < 24; i++) {
            Slot slot = this.menu.slots.get(i + 9);
            int row = i / 6;
            int col = i % 6;
            setSlotPos(slot, (col * sSize) + sX + offset, (row * sSize) + sY + offset);
        }
        // 인벤토리 남은 슬롯 숨김
        for (int i = 24; i < 27; i++) setSlotPos(this.menu.slots.get(i + 9), -2000, -2000);
        for (int i = 24; i < 27; i++) {
            disableAndHideSlot(this.menu.slots.get(i + 9));
        }

        // 4. [교정] 진짜 핫바 (36~44번) - 가로 배치
        // 마인크래프트 내부 인덱스에서 36~44번이 하단 핫바입니다.
        int hotbarX = sX;
        int hotbarY = sY + (4 * sSize) + 5; // 인벤토리 뭉치 아래 5px 여백

        for (int i = 0; i < 9; i++) {
            Slot hotbarSlot = this.menu.slots.get(i + 36);
            if (i < 6) { // 6개만 가로로 나열
                setSlotPos(hotbarSlot, (i * sSize) + hotbarX + offset, hotbarY + offset);
            } else {
                setSlotPos(hotbarSlot, -2000, -2000);
            }
        }
        for (int i = 6; i < 9; i++) {
            disableAndHideSlot(this.menu.slots.get(i + 36));
        }

        // 5. 장비창 (5~8번)
        int armorX = 0; // 캐릭터 왼쪽 적당한 위치
        int armorY = 325;

        for (int i = 0; i < 4; i++) {
            // 5: 머리, 6: 가슴, 7: 바지, 8: 신발 (또는 역순)
            Slot armorSlot = this.menu.slots.get(i + 5);
            setSlotPos(armorSlot, armorX + (i * sSize) + offset, armorY + offset);
        }

        // 6. 왼손(Offhand) 슬롯 (45번)
        // 1.20.1 InventoryMenu 기준 왼손 슬롯은 보통 45번입니다.
        if (this.menu.slots.size() > 45) disableAndHideSlot(this.menu.slots.get(45)); // 왼손
        this.minecraft.player.getCapability(PlayerGearCapability.GEAR_CAPABILITY).ifPresent(cap -> {
            // 슬롯 객체를 추가하지 않고, 아이템 데이터만 복사해옵니다.
            this.extraSlot0 = cap.inventory.getStackInSlot(0).copy();
            this.extraSlot1 = cap.inventory.getStackInSlot(1).copy();
        });
        this.extraX0 = armorX + (4 * sSize) + offset;
        this.extraY0 = armorY + offset;
        this.extraX1 = armorX + (5 * sSize) + offset;
        this.extraY1 = armorY + offset;
    }
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Slot slot = this.findHoveredSlot();
        // 클릭한 슬롯이 숨겨진 슬롯이면 아예 이벤트 전파를 막음
        if (slot != null && slot.x < -1000) {
            return false;
        }
        // 추가 슬롯 0번 클릭 판정
        if (isHovering(extraX0, extraY0, 18, 18, pMouseX, pMouseY)) {
            handleCustomSlotClick(0);
            return true;
        }
        // 추가 슬롯 1번 클릭 판정
        if (isHovering(extraX1, extraY1, 18, 18, pMouseX, pMouseY)) {
            handleCustomSlotClick(1);
            return true;
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
    private void handleCustomSlotClick(int index) {
        ItemStack carried = this.menu.getCarried();

        // 1. 조건 확인: 비어있지 않은 아이템을 넣으려고 할 때만 체크
        if (!carried.isEmpty()) {
            if (!isItemValidForSlot(index, carried)) {
                // 조건에 맞지 않으면 아무 일도 일어나지 않음 (또는 소리 재생 가능)
                return;
            }
        }

        // 2. 기존 교체 로직 (조건 통과 시 실행)
        if (index == 0) {
            ItemStack temp = this.extraSlot0.copy();
            this.extraSlot0 = carried.copy();
            this.menu.setCarried(temp);
            ModMessages.sendToServer(new SyncExtraSlotPacket(0, this.extraSlot0));
        } else {
            ItemStack temp = this.extraSlot1.copy();
            this.extraSlot1 = carried.copy();
            this.menu.setCarried(temp);
            ModMessages.sendToServer(new SyncExtraSlotPacket(1, this.extraSlot1));
        }
    }
    private boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) {
            // 예: 특정 아이템 ID로 제한 (examplemod:backpack)
            return stack.getItem().toString().equals("examplemod:backpack");

            // 예: 모든 종류의 '칼'만 허용
            //return stack.getItem() instanceof net.minecraft.world.item.SwordItem;
        } else {
            // 예: 특정 아이템 객체로 제한
            return stack.is(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
        }
    }
    // --- [핵심] 실제 상호작용 박스 크기 조절 로직 ---
    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType clickType) {
        // 1. 숨겨진 슬롯 차단 (기존 로직)
        if (slot != null && slot.x < -1000) {
            return;
        }

        if (slot instanceof CustomGearSlot gearSlot) {
            // 마우스에 들고 있는 아이템
            ItemStack carried = this.menu.getCarried();

            // 슬롯에 아이템을 강제로 넣고 서버에 즉시 보고
            gearSlot.set(carried.copy());
            ModMessages.sendToServer(new SyncExtraSlotPacket(gearSlot.getSlotIndex(), gearSlot.getItem()));

            // 마우스 아이템 비우기 (클라이언트 시뮬레이션)
            this.menu.setCarried(ItemStack.EMPTY);
            return; // super 호출 방지 (서버의 거절 메시지를 무시하기 위함)
        }
        super.slotClicked(slot, slotId, mouseButton, clickType);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 1. 먼저 바닐라 슬롯들의 툴팁을 그립니다.
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        // 2. 만약 마우스가 바닐라 슬롯 위에 있다면, 커스텀 툴팁을 그리지 않고 종료합니다 (중복 방지).
        if (this.hoveredSlot != null) {
            return;
        }

        // 3. 커스텀 슬롯 0번 툴팁 (아이템이 있을 때만)
        if (isHovering(extraX0, extraY0, 18, 18, (double)mouseX, (double)mouseY)) {
            if (!extraSlot0.isEmpty()) {
                guiGraphics.renderTooltip(this.font, extraSlot0, mouseX, mouseY);
            }
        }

        // 4. 커스텀 슬롯 1번 툴팁
        if (isHovering(extraX1, extraY1, 18, 18, (double)mouseX, (double)mouseY)) {
            if (!extraSlot1.isEmpty()) {
                guiGraphics.renderTooltip(this.font, extraSlot1, mouseX, mouseY);
            }
        }
    }

    // 1. 모든 슬롯 관련 판정의 '진짜 뿌리' 메서드입니다.
    // 인자 이름(p_97768_ 등)은 사용하시는 환경에 따라 다를 수 있지만,
    // 개수와 타입(int 4개, double 2개)이 일치하면 무조건 작동합니다.
    @Override
    protected boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
        // 만약 들어온 범위가 바닐라의 16x16 또는 18x18이라면, 강제로 24x24로 확장해서 판정합니다.
        if (pWidth <= 18 && pHeight <= 18) {
            // 중심은 유지하고 범위를 24로 넓힙니다.
            return super.isHovering(pX - 3, pY - 3, 24, 24, pMouseX, pMouseY);
        }
        return super.isHovering(pX, pY, pWidth, pHeight, pMouseX, pMouseY);
    }
    // --- [렌더링 로직] ---

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.mouseX = (double)mouseX;
        this.mouseY = (double)mouseY;
        this.renderBackground(guiGraphics);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

        // [1] 모든 슬롯 순회 (추가 슬롯 + 바닐라 슬롯)
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && slot.x > -1000) {
                int sx = this.leftPos + slot.x;
                int sy = this.topPos + slot.y;

                // 아이템 본체만 그리기 (장식 제외)
                if (!slot.getItem().isEmpty()) {
                    guiGraphics.renderItem(slot.getItem(), sx, sy);
                    // 우리가 만든 커스텀 장식(개수, 내구도) 그리기
                    renderSlotDecorations(guiGraphics, slot.getItem(), sx, sy);
                }
            }
        }
        // [1] 커스텀 슬롯 0번 그리기 (아이템 + 내구도 + 개수)
        int x0 = this.leftPos + extraX0;
        int y0 = this.topPos + extraY0;
        if (!extraSlot0.isEmpty()) {
            guiGraphics.renderItem(extraSlot0, x0, y0);
            // renderCustomDurabilityBar 대신 통합 메서드 사용
            renderSlotDecorations(guiGraphics, extraSlot0, x0, y0);
        }

        // [2] 커스텀 슬롯 1번 그리기 (아이템 + 내구도 + 개수)
        int x1 = this.leftPos + extraX1;
        int y1 = this.topPos + extraY1;
        if (!extraSlot1.isEmpty()) {
            guiGraphics.renderItem(extraSlot1, x1, y1);
            renderSlotDecorations(guiGraphics, extraSlot1, x1, y1);
        }

        // [4] 커스텀 슬롯 하이라이트 (마우스 오버 시)
        RenderSystem.disableDepthTest();
        if (isHovering(extraX0, extraY0, 18, 18, (double)mouseX, (double)mouseY)) {
            guiGraphics.fill(x0 - 3, y0 - 3, x0 + 21, y0 + 21, 0x80FFFFFF);
        }
        if (isHovering(extraX1, extraY1, 18, 18, (double)mouseX, (double)mouseY)) {
            guiGraphics.fill(x1 - 3, y1 - 3, x1 + 21, y1 + 21, 0x80FFFFFF);
        }
        RenderSystem.enableDepthTest();

        // [5] 바닐라 슬롯 하이라이트 (기존 로직 유지)
        Slot hoveredSlot = this.findHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.isActive()) {
            int sx = this.leftPos + hoveredSlot.x;
            int sy = this.topPos + hoveredSlot.y;
            RenderSystem.disableDepthTest();
            guiGraphics.fill(sx - 3, sy - 3, sx + 21, sy + 21, 0x80FFFFFF);
            RenderSystem.enableDepthTest();
        }
        // [6] 마우스가 들고 있는 아이템(Carried Item) 그리기
        if (!this.menu.getCarried().isEmpty()) {
            guiGraphics.pose().pushPose();
            // 마우스 아이템은 가장 앞 레이어(Z-index 200 이상)에 그려야 합니다.
            guiGraphics.pose().translate(0, 0, 250);
            // 아이템 본체 그리기 (마우스 위치 기준 -8픽셀 하여 중앙 맞춤)
            guiGraphics.renderItem(this.menu.getCarried(), mouseX - 8, mouseY - 8);
            // 마우스 아이템의 개수와 내구도도 Rust 스타일로 적용!
            renderSlotDecorations(guiGraphics, this.menu.getCarried(), mouseX - 8, mouseY - 8);
            guiGraphics.pose().popPose();
        }

        // [7] 툴팁 그리기 (가장 마지막)
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    private void renderSlotDecorations(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;

        // --- [1] 커스텀 세로 내구도 바 (왼쪽 배치) ---
        if (stack.isDamageableItem()) {
            // 검은색 배경 바 (아이템 왼쪽 -2 위치)
            // y 좌표는 아이템의 높이에 맞춰 0~16 (또는 디자인에 따라 조정)
            guiGraphics.fill(x - 2, y - 1, x, y + 17, 0xFF222222);

            // 내구도 비율 계산
            float damagePos = (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            int barHeight = (int) (17 * damagePos); // 전체 16픽셀 기준

            // 내구도 초록색 바 (아래에서 위로 차오름)
            guiGraphics.fill(x - 2, y + 17 - barHeight, x, y + 17, 0xFF5DB31F);
        }

        // --- [1] 아이템 개수 (우측 하단, 그림자 포함) ---
        if (stack.getCount() > 1) {
            String countText = String.valueOf(stack.getCount());
            guiGraphics.pose().pushPose();

            // 아이템 슬롯(18x18)의 우측 하단 끝점에 가깝게 배치 (x+17, y+17)
            // 핫바와 동일한 0.65배율 및 200 레이어 설정
            guiGraphics.pose().translate(x + 17, y + 17, 200);
            guiGraphics.pose().scale(0.65f, 0.65f, 0.65f);

            // [핵심] 마지막 인자를 true로 설정하여 Hotbar와 똑같은 선명한 그림자 텍스트 생성
            guiGraphics.drawString(this.font, countText, -this.font.width(countText), -this.font.lineHeight, 0xFFFFFFFF, true);

            guiGraphics.pose().popPose();
        }
    }
    @Override
    public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
        return super.mouseReleased(p_97812_, p_97813_, p_97814_);
    }
    @Override
    protected List<Component> getTooltipFromContainerItem(ItemStack p_283689_) {
        return super.getTooltipFromContainerItem(p_283689_);
    }

    @Nullable
    private Slot findHoveredSlot() {
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            Slot slot = this.menu.slots.get(i);
            // 이미 성공한 isHovering 판정 로직 활용 (범위 18로 넣어도 내부에서 24로 확장됨)
            if (this.isHovering(slot.x, slot.y, 18, 18, this.mouseX, this.mouseY)) {
                return slot;
            }
        }
        return null;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 1. 반투명 처리를 위한 렌더 시스템 설정
        RenderSystem.enableBlend(); // 블렌딩 활성화
        RenderSystem.defaultBlendFunc(); // 표준 블렌딩 함수 설정
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // 색상 왜곡 방지

        int guiX = (this.width - this.imageWidth) / 2;
        int guiY = (this.height - this.imageHeight) / 2;

        if (GUI_TEXTURE != null) {
            // 배경 이미지 그리기
            guiGraphics.blit(GUI_TEXTURE,
                    guiX, guiY,
                    0, 0,
                    this.imageWidth, this.imageHeight,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT
            );
        }

        // 2. 플레이어 모델 렌더링
        InventoryScreen.renderEntityInInventoryFollowsMouse(
                guiGraphics,
                guiX + 60, guiY + 300,
                70,
                (float)(guiX + 80) - mouseX,
                (float)(guiY + 240 - 100) - mouseY,
                this.minecraft.player
        );

        // 3. 렌더링이 끝난 후 블렌딩 끄기 (다른 UI에 영향을 주지 않기 위함)
        RenderSystem.disableBlend();
    }
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 이미지에 텍스트가 있다면 비워둡니다.
    }
}
