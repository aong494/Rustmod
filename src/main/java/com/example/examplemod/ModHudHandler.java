package com.example.examplemod;

import com.example.examplemod.Thirst.ClientThirstData;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.*;


@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ModHudHandler {

    // 경로에 대문자가 없는지 다시 확인하세요!
    private static final ResourceLocation HUD_BG = ResourceLocation.tryParse("examplemod:textures/gui/hud_bg.png");
    private static final ResourceLocation HP_ICON = ResourceLocation.tryParse("examplemod:textures/gui/icons/hp_icon.png");
    private static final ResourceLocation HUNGER_ICON2 = ResourceLocation.tryParse("examplemod:textures/gui/icons/hunger_icon2.png");
    private static final ResourceLocation THIRST_ICON2 = ResourceLocation.tryParse("examplemod:textures/gui/icons/thirst_icon2.png");
    private static final ResourceLocation HUNGER_ICON = ResourceLocation.tryParse("examplemod:textures/gui/icons/hunger_icon.png");
    private static final ResourceLocation THIRST_ICON = ResourceLocation.tryParse("examplemod:textures/gui/icons/thirst_icon.png");
    private static final ResourceLocation DEBUFF_BG = ResourceLocation.tryParse("examplemod:textures/gui/hud_bg2.png");
    private static final ResourceLocation COMPASS_BAR = ResourceLocation.tryParse("examplemod:textures/gui/compass_bar.png");
    private static final ResourceLocation SLOT_NORMAL = ResourceLocation.tryParse("examplemod:textures/gui/slot_normal.png");
    private static final ResourceLocation SLOT_SELECTED = ResourceLocation.tryParse("examplemod:textures/gui/slot_selected.png");
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // --- 1. 나침반 섹션 (상단 중앙) ---
        int compassWidth = 200;
        int compassHeight = 15; // 높이를 살짝 줄여서 더 슬림하게
        int compassX = (screenWidth / 2) - (compassWidth / 2);

        renderCompass(guiGraphics, mc, compassX, 0, compassWidth, compassHeight);
        renderRustHotbar(guiGraphics, mc, screenWidth, screenHeight);
        // --- 2. 상태 바 섹션 (우측 하단) ---
        // 변수 이름을 barX로 변경하여 중복 방지
        int barX = screenWidth - 130;
        int barYStart = screenHeight - 50;

        // 체력 (최대치 100 반영)
        renderRustBar(guiGraphics, mc, HUD_BG, HP_ICON, barX, barYStart,
                mc.player.getHealth(), 100.0f, 0xFF5DB31F);

        // 허기 (커스텀 500 반영)
        renderRustBar(guiGraphics, mc, HUD_BG, HUNGER_ICON, barX, barYStart + 32,
                com.example.examplemod.Hunger.ClientHungerData.get(), 500.0f, 0xFFD87820);

        // 갈증 (250 반영)
        renderRustBar(guiGraphics, mc, HUD_BG, THIRST_ICON, barX, barYStart + 16,
                ClientThirstData.get(), 250.0f, 0xFF1DA1F2);

        // --- 3. 디버프 알림 섹션 (바 위에 쌓기) ---
        int alertX = barX;
        int alertY = barYStart - 20;

        if (com.example.examplemod.Hunger.ClientHungerData.get() <= 40.0f) {
            renderStatusAlert(guiGraphics, mc, DEBUFF_BG, HUNGER_ICON2, "배고픔", alertX, alertY, 0xFFD87820);
            alertY -= 18;
        }

        if (ClientThirstData.get() <= 25.0f) {
            renderStatusAlert(guiGraphics, mc, DEBUFF_BG, THIRST_ICON2, "목마름", alertX, alertY, 0xFF1DA1F2);
            alertY -= 18;
        }
    }

    private static void renderStatusAlert(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation bg, ResourceLocation icon, String label, int x, int y, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. 배경 그리기 (알파값 1.0으로 선명하게)
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.2F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        guiGraphics.blit(bg, x, y, 0, 0, 120, 16, 150, 20);

        // 2. 아이콘 그리기
        if (icon != null) {
            guiGraphics.blit(icon, x + 5, y + 3, 0, 0, 10, 10, 10, 10);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1F);

        // 3. 텍스트 렌더링 (굵기 보강 및 위치 조정)
        guiGraphics.pose().pushPose();
        // 글자가 너무 얇아 보일 때는 scale을 조금 키우고 '그림자'를 키는 것이 가장 효과적입니다.
        guiGraphics.pose().translate(x + 22, y + 5, 0);
        guiGraphics.pose().scale(1f, 1f, 1f); // 0.7 -> 0.8로 약간 확대

        // drawString의 마지막 인자를 true로 바꾸면 그림자가 생겨 글씨가 훨씬 진하고 굵게 보입니다.
        guiGraphics.drawString(mc.font, label, 0, 0, 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();

        RenderSystem.disableBlend();
    }

    private static void renderRustBar(GuiGraphics guiGraphics, Minecraft mc, ResourceLocation bgPath, ResourceLocation iconPath, int x, int y, float value, float max, int barColor) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1F);
        // 1. 배경 PNG 그리기 (150x20 사이즈에 최적화)
        if (bgPath != null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, bgPath);

            // 기존 blit은 가끔 알파를 씹습니다. 아래 코드로 교체해 보세요.
            guiGraphics.blit(bgPath, x, y, 0, 0, 120, 16, 150, 20);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // 2. 아이콘 PNG 그리기 (배경을 그린 직후 텍스처를 다시 지정)
        if (iconPath != null) {

            // 아이콘 파일이 64x64든 128x128든 상관없이 10x10 크기로 화면에 꽉 채워 그립니다.
            // 마지막 두 숫자는 이미지 파일의 실제 크기를 적는 것이 원칙이나,
            // 깨짐이 심할 경우 파일의 실제 픽셀 값(예: 64, 64)으로 수정해 보세요.
            guiGraphics.blit(iconPath, x + 5, y + 3, 0, 0, 10, 10, 10, 10);
            RenderSystem.setShaderTexture(0, iconPath);
        }

        // 3. 게이지 바 그리기 (도형 렌더링용 셰이더로 교체)
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        int barPadding = 20;
        int maxBarWidth = 95;
        int barWidth = (int) (maxBarWidth * (Math.min(value / max, 1.0f)));

        // 배경 크기(150x20)에 맞춰 바의 위치와 높이(10)를 조정
        guiGraphics.fill(x + barPadding, y + 2, x + barPadding + barWidth, y + 14, barColor | 0xFF000000);

        // 4. 숫자 표시
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x + barPadding + 2, y + 6, 0);
        guiGraphics.pose().scale(0.7f, 0.7f, 0.7f);
        guiGraphics.drawString(mc.font, String.valueOf((int)value), 0, 0, 0xFFFFFFFF, false);
        guiGraphics.pose().popPose();

        RenderSystem.disableBlend();
    }
    @SubscribeEvent
    public static void onRenderGuiPre(RenderGuiOverlayEvent.Pre event) {
        String path = event.getOverlay().id().getPath();

        // 차단할 대상 (마인크래프트 순정 UI만 정확히 집어서 차단)
        if (path.equals("player_health") ||
                path.equals("food_level") ||
                path.equals("hotbar") ||
                path.equals("experience_bar") ||
                path.equals("armor_level") ||
                path.equals("air_level")) {
            event.setCanceled(true);
        }
        // 여기에 해당하지 않는 보이스챗 등 타 모드의 UI는 통과됩니다.
    }
    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == net.minecraftforge.event.TickEvent.Phase.END) {
            net.minecraft.world.entity.player.Player player = event.player;
            if (player == null) return;

            int currentSlot = player.getInventory().selected;

            // 6번(인덱스 5)을 초과하는 경우 (오른쪽 휠)
            if (currentSlot >= 6 && currentSlot <= 8) {
                player.getInventory().selected = 0;
            }
            // 1번(인덱스 0)에서 왼쪽 휠을 돌려 9번쪽(인덱스 8)으로 간 경우
            else if (currentSlot > 5) { // 사실상 6, 7, 8인 경우지만 명확히 처리
                player.getInventory().selected = 5;
            }
            // 만약 9번 슬롯(8) 인덱스가 직접 잡힌다면 5로 강제 고정
            if (currentSlot == 8 || currentSlot == 7 || currentSlot == 6) {
                // 이전에 어디에 있었는지에 따라 방향성을 주면 좋지만,
                // 단순하게는 6번으로 점프하게 합니다.
                player.getInventory().selected = (currentSlot == 8) ? 5 : 0;
            }
        }
    }

    @SubscribeEvent
    public static void onKeyInput(net.minecraftforge.client.event.InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 숫자 키 7, 8, 9 (GLFW_KEY_7 = 55, 8 = 56, 9 = 57) 입력을 직접 감지하여 취소 시도
        int key = event.getKey();
        if (key >= 55 && key <= 57) {
            // 숫자 키 7~9를 눌러도 아무 반응이 없도록 현재 슬롯을 강제로 유지하거나 0으로 고정
            if (mc.player.getInventory().selected >= 6) {
                mc.player.getInventory().selected = 0;
            }
        }
    }
    private static void renderCompass(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // 1. 현재 각도 (0 ~ 360도)
        float yaw = (mc.player.getViewYRot(mc.getFrameTime()) % 360 + 360) % 360;
        float realTextureWidth = 1400.0f;
        float realTextureHeight = 17.0f;
        float pixelsPerDegree = (realTextureWidth / 360.0f);
        // 4. 오프셋 조정 (0도 기준점 맞추기)
        // 앞서 맞추셨던 20.0f 값을 유지하되, 보정된 배율에 맞춰 미세 조정이 필요할 수 있습니다.
        float degreeOffset = 0.0f;
        float correctedYaw = (yaw + degreeOffset) % 360;
        // 5. UV 좌표 계산
        float u = (correctedYaw * pixelsPerDegree) - (width / 2.0f);
        // 무한 루프 보정
        u = (u % realTextureWidth + realTextureWidth) % realTextureWidth;
        // 6. 나침반 이미지 렌더링
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        guiGraphics.blit(COMPASS_BAR, x, y, u, 0, width, height, (int)realTextureWidth, (int)realTextureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        // 7. 중앙 고정 포인터
        guiGraphics.fill(x + (width / 2), 0, x + (width / 2) + 1, 5, 0xFFFFFFFF);
        RenderSystem.disableBlend();
    }
    private static void renderRustHotbar(GuiGraphics guiGraphics, Minecraft mc, int screenWidth, int screenHeight) {
        int slotCount = 6;
        int slotSize = 24;
        int padding = 4;
        int totalWidth = (slotCount * slotSize) + ((slotCount - 1) * padding);
        int startX = (screenWidth / 2) - (totalWidth / 2);
        int y = screenHeight - slotSize - 10;

        // --- [1단계: 모든 슬롯의 배경만 먼저 그리기] ---
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // 투명도 설정 (0.1F가 너무 흐리면 0.4F 정도로 추천합니다)
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        for (int i = 0; i < slotCount; i++) {
            int slotX = startX + (i * (slotSize + padding));
            boolean isSelected = (mc.player.getInventory().selected == i);
            ResourceLocation slotTexture = isSelected ? SLOT_SELECTED : SLOT_NORMAL;

            guiGraphics.blit(slotTexture, slotX, y, 0, 0, slotSize, slotSize, slotSize, slotSize);
        }

        // --- [2단계: 아이템 및 기타 요소 그리기 (불투명)] ---
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // 다시 완전 불투명으로 복구

        for (int i = 0; i < slotCount; i++) {
            int slotX = startX + (i * (slotSize + padding));
            net.minecraft.world.item.ItemStack stack = mc.player.getInventory().getItem(i);

            if (!stack.isEmpty()) {
                // 아이템 본체
                guiGraphics.renderItem(stack, slotX + 4, y + 4);

                // 아이템 개수 (직접 그리기)
                if (stack.getCount() > 1) {
                    String countText = String.valueOf(stack.getCount());
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(slotX + 23, y + 23, 200);
                    guiGraphics.pose().scale(0.65f, 0.65f, 0.65f);
                    guiGraphics.drawString(mc.font, countText, -mc.font.width(countText), -mc.font.lineHeight, 0xFFFFFFFF, true);
                    guiGraphics.pose().popPose();
                }

                // 커스텀 내구도 바 (왼쪽 세로)
                if (stack.isDamageableItem()) {
                    guiGraphics.fill(slotX + 1, y + 4, slotX + 3, y + 20, 0xFF222222);
                    float damagePos = (float) (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
                    int barHeight = (int) (16 * damagePos);
                    guiGraphics.fill(slotX + 1, y + 20 - barHeight, slotX + 3, y + 20, 0xFF5DB31F);
                }
            }

            // 슬롯 번호
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(slotX + 3, y + 2, 200);
            guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
            guiGraphics.drawString(mc.font, String.valueOf(i + 1), 0, 0, 0xFFFFFFFF, true);
            guiGraphics.pose().popPose();
        }

        RenderSystem.disableBlend();
    }
}