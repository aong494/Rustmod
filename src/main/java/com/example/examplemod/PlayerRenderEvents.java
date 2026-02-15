package com.example.examplemod;

import com.example.examplemod.effect.ModEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PlayerRenderEvents {
    private static final ResourceLocation DOWNED_OVERLAY = ResourceLocation.tryParse("examplemod:textures/gui/downed_screen.png");

    private static boolean isPlayerDowned(Player player) {
        if (player == null) return false;

        // 1. 커스텀 효과 확인
        boolean hasEffect = player.hasEffect(ModEffects.DOWNED.get());
        // 2. 서버에서 보낸 태그 확인
        boolean hasTag = player.getTags().contains("isDowned");
        // 3. 포즈 확인 (서버에서 setPose(SLEEPING)을 했다면 클라이언트에도 동기화됨)
        boolean isSleeping = player.getPose() == Pose.SLEEPING;

        return hasEffect || hasTag || isSleeping;
    }
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player targetPlayer = event.getEntity();

        // isDowned 태그가 있거나, '구속(Slowness)' 효과를 가지고 있을 때 눕힘
        if (isPlayerDowned(targetPlayer)) {
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            targetPlayer.setXRot(0);
            // 캐릭터를 바닥으로 이동 및 회전
            poseStack.translate(0.0D, 0.1D, 0.0D);

            // 플레이어가 바라보는 방향 유지
            poseStack.mulPose(Axis.XP.rotationDegrees(0.0F));

            // 모델 중심축 조정
            poseStack.translate(1.8D, 0.0D, 0.0D);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (isPlayerDowned(event.getEntity())) {
            try {
                event.getPoseStack().popPose();
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && isPlayerDowned(player)) {
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.CLIENT && event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            if (isPlayerDowned(player)) {
                player.setPose(Pose.SLEEPING);
                player.refreshDimensions();
            }
        }
    }
    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && isPlayerDowned(player)) {
            int screenWidth = event.getWindow().getGuiScaledWidth();
            int screenHeight = event.getWindow().getGuiScaledHeight();
            GuiGraphics guiGraphics = event.getGuiGraphics();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.1F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);

            // 1. 이미지의 실제 해상도 설정
            float imgW = 1024.0F;
            float imgH = 792.0F;
            float imgAspect = imgW / imgH;
            float screenAspect = (float) screenWidth / screenHeight;

            int drawX = 0, drawY = 0;
            int drawW = screenWidth;
            int drawH = screenHeight;

            // 2. 비율 계산 로직
            if (screenAspect > imgAspect) {
                drawW = screenWidth;
                drawH = (int) (screenWidth / imgAspect);
                drawY = 0;
                drawX = 0;
            } else {
                drawH = screenHeight;
                drawW = (int) (screenHeight * imgAspect);
                drawX = (screenWidth - drawW) / 2;
                drawY = 0;
            }
            // 3. 렌더링 (인자 9개짜리 public blit 사용)
            // x, y, uOffset, vOffset, width, height, textureWidth, textureHeight
            guiGraphics.blit(DOWNED_OVERLAY, drawX, drawY, 0, 0, drawW, drawH, drawW, drawH);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }
    }
}