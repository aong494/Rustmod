package com.example.examplemod.gui;

import com.example.examplemod.KeyInit;
import com.example.examplemod.network.MapPingPacket;
import com.example.examplemod.network.PacketHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapScreen extends Screen {
    private static final ResourceLocation MAP_TEXTURE = new ResourceLocation("examplemod", "textures/gui/map.png");

    protected double mapX = 0;
    protected double mapY = 0;
    protected float scale = 1.0f;
    public double getMapX() { return mapX; }
    public double getMapY() { return mapY; }
    public float getScale() { return scale; }

    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 6.0f;
    private static final float WORLD_SIZE = 9343.0f;
    private static final float TEXTURE_SIZE = 1024.0f; // 적절한 UI 크기인 1024로 조정
    private static final float PIXEL_PER_BLOCK = TEXTURE_SIZE / WORLD_SIZE;
    private static final java.util.Map<UUID, GroupMemberData> groupMembers = new java.util.HashMap<>();
    private static final List<MapPing> sharedPings = new java.util.concurrent.CopyOnWriteArrayList<>();

    private final List<MapPing> pings = new ArrayList<>();

    public static void updateGroupMember(UUID uuid, String name, double x, double z, int index) {
        groupMembers.put(uuid, new GroupMemberData((float)x, (float)z, name, index));
    }
    public static void syncSharedPing(float x, float z, boolean isRemove, int index) {
        if (isRemove) {
            sharedPings.removeIf(p -> Math.abs(p.x - x) < 0.1 && Math.abs(p.z - z) < 0.1);
        } else {
            // 인덱스에 맞는 색상 가져오기 (가상의 getColor 메서드 혹은 직접 계산)
            int color = getIndexColor(index);
            sharedPings.add(new MapPing(x, z, String.valueOf(index), color));
        }
    }
    private static int getIndexColor(int index) {
        switch (index % 5) {
            case 1: return 0xFFFF4444;
            case 2: return 0xFF4444FF;
            case 3: return 0xFF44FF44;
            case 4: return 0xFFFFFF44;
            default: return 0xFFFF44FF;
        }
    }
    public static void clearSharedPings() {
        sharedPings.clear();
        groupMembers.clear();
    }
    public MapScreen() {
        super(Component.literal("Rust Map"));
        centerOnPlayer();
    }

    private void centerOnPlayer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            // [수정] 좌상단(0,0) 기준이므로 월드 좌표에 오프셋을 더할 필요 없이 그대로 변환
            // 마인크래프트 X, Z 좌표를 지도의 픽셀 위치로 환산
            double pixelX = mc.player.getX() * PIXEL_PER_BLOCK;
            double pixelY = mc.player.getZ() * PIXEL_PER_BLOCK;

            // 화면 중앙(this.width/2)에 이 픽셀이 오도록 mapX, mapY 오프셋 설정
            this.mapX = -pixelX;
            this.mapY = -pixelY;
        }
    }

    // [추가] M키를 다시 눌렀을 때 닫는 기능
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // KeyInit에 등록된 MAP_KEY의 키 코드와 같은지 확인
        if (KeyInit.MAP_KEY.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.width / 2.0f + (float)mapX, this.height / 2.0f + (float)mapY, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        // 1. 지도 배경
        RenderSystem.setShaderTexture(0, MAP_TEXTURE);
        guiGraphics.blit(MAP_TEXTURE, 0, 0, 0, 0, (int)TEXTURE_SIZE, (int)TEXTURE_SIZE, (int)TEXTURE_SIZE, (int)TEXTURE_SIZE);

        // 2. 그룹 멤버 위치 및 숫자 렌더링
        for (java.util.Map.Entry<UUID, GroupMemberData> entry : groupMembers.entrySet()) {
            GroupMemberData member = entry.getValue();

            // 내 위치는 제외
            if (this.minecraft.player != null && entry.getKey().equals(this.minecraft.player.getUUID())) continue;

            // 월드 좌표를 지도 픽셀 좌표로 변환
            float mX = (float)(member.x * PIXEL_PER_BLOCK);
            float mZ = (float)(member.z * PIXEL_PER_BLOCK);

            // [중요] 새로운 Pose를 열어서 이 아이콘만 별도로 제어
            guiGraphics.pose().pushPose();

            // 1단계: 아이콘이 표시될 위치로 이동
            guiGraphics.pose().translate(mX, mZ, 0);

            // 2단계: 역보정 적용 (지도가 scale만큼 커졌으니 아이콘은 1/scale만큼 줄임)
            float iconScale = 1.0f / scale;
            guiGraphics.pose().scale(iconScale, iconScale, 1.0f);

            // 3단계: 이제 (0, 0) 기준으로 아이콘 그리기 (크기가 고정됨)
            // 배경 상자 (색상 적용)
            guiGraphics.fill(-6, -6, 6, 6, member.getColor());

            // 가입 순서 숫자 (흰색)
            String indexStr = String.valueOf(member.index);
            guiGraphics.drawCenteredString(this.font, indexStr, 0, -4, 0xFFFFFFFF);

            // 이름 표시 (숫자 아래에 작게)
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(0.7f, 0.7f, 1.0f);
            guiGraphics.drawCenteredString(this.font, member.name, 0, 10, 0xFFFFFFFF);
            guiGraphics.pose().popPose();

            // 4단계: 이 멤버의 Pose 닫기 (다음 멤버나 내 위치에 영향을 주지 않음)
            guiGraphics.pose().popPose();
        }
        for (MapPing ping : sharedPings) {
            renderPing(guiGraphics, ping.x * PIXEL_PER_BLOCK, ping.z * PIXEL_PER_BLOCK, ping.color, ping.label);
        }

        // 3. 내 위치 렌더링
        if (this.minecraft.player != null) {
            float playerX = (float) (this.minecraft.player.getX() * PIXEL_PER_BLOCK);
            float playerZ = (float) (this.minecraft.player.getZ() * PIXEL_PER_BLOCK);
            float iconScale = 1.0f / scale; // 역보정 값 계산

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(playerX, playerZ, 0); // 플레이어 위치로 이동
            guiGraphics.pose().scale(iconScale, iconScale, 1.0f); // 역보정 적용

            // 이제 0, 0 기준으로 그리면 확대해도 크기가 고정됩니다.
            guiGraphics.fill(-2, -2, 2, 2, 0xFF00FF00);
            guiGraphics.drawCenteredString(this.font, "Me", 0, -10, 0xFF00FF00);

            guiGraphics.pose().popPose();
        }

        guiGraphics.pose().popPose();

        guiGraphics.drawString(this.font, String.format("확대: %.1fx", scale), 10, 10, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "M: 닫기 | 좌클릭 드래그: 이동 | 휠: 확대/축소", this.width / 2, 20, 0xAAAAAA);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    private void renderPing(GuiGraphics guiGraphics, float x, float z, int color, String label) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, z, 0);
        float fixedScale = 1.0f / scale;
        guiGraphics.pose().scale(fixedScale, fixedScale, 1.0f);

        // 배경 상자 (색상 적용)
        guiGraphics.fill(-5, -5, 5, 5, color);
        // [수정] "Shared" 대신 전달받은 숫자(label)를 그립니다.
        guiGraphics.drawCenteredString(this.font, label, 0, -4, 0xFFFFFFFF);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float zoomSpeed = 0.2f;
        float oldScale = this.scale;

        if (delta > 0) this.scale = Mth.clamp(this.scale + zoomSpeed, MIN_SCALE, MAX_SCALE);
        else if (delta < 0) this.scale = Mth.clamp(this.scale - zoomSpeed, MIN_SCALE, MAX_SCALE);

        // [마우스 중심 확대 보정]
        if (oldScale != this.scale) {
            double mouseRelX = mouseX - (this.width / 2.0 + mapX);
            double mouseRelY = mouseY - (this.height / 2.0 + mapY);
            double multiplier = (this.scale / oldScale);
            this.mapX -= (mouseRelX * multiplier - mouseRelX);
            this.mapY -= (mouseRelY * multiplier - mouseRelY);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) { // 우클릭
            // 1. 현재 클릭한 위치의 '지도상 상대 좌표' (확대/이동 반영)
            double relX = (mouseX - (this.width / 2.0) - mapX);
            double relY = (mouseY - (this.height / 2.0) - mapY);

            MapPing toRemove = null;
            double visualThreshold = 10.0; // 클릭 판정 범위 (픽셀)

            // 2. [수정] 이제 내 개인 리스트가 아닌, 서버와 동기화된 sharedPings에서 찾습니다.
            for (MapPing ping : sharedPings) {
                double pingCanvasX = (ping.x * PIXEL_PER_BLOCK) * scale;
                double pingCanvasZ = (ping.z * PIXEL_PER_BLOCK) * scale;

                double dx = pingCanvasX - relX;
                double dz = pingCanvasZ - relY;
                double distanceSq = dx * dx + dz * dz;

                if (distanceSq < (visualThreshold * visualThreshold)) {
                    toRemove = ping;
                    break;
                }
            }

            if (toRemove != null) {
                PacketHandler.INSTANCE.sendToServer(new MapPingPacket(toRemove.x, toRemove.z, true));
            } else {
                // [수정] 좌상단 0,0 기준이므로 단순히 스케일과 픽셀비율로 나눕니다.
                float gameX = (float) ((relX / scale) / PIXEL_PER_BLOCK);
                float gameZ = (float) ((relY / scale) / PIXEL_PER_BLOCK);
                PacketHandler.INSTANCE.sendToServer(new MapPingPacket(gameX, gameZ, false));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            this.mapX += dragX;
            this.mapY += dragY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    @Override
    public boolean isPauseScreen() { return false; }

    // MapScreen.java 내부의 MapPing 클래스 수정
    private static class MapPing {
        float x, z;
        String label; // 숫자가 들어갈 곳
        int color;    // 멤버의 고유 색상

        MapPing(float x, float z, String label, int color) {
            this.x = x;
            this.z = z;
            this.label = label;
            this.color = color;
        }
    }

    // 보관용 내부 클래스
    public static class GroupMemberData {
        public float x, z;
        public String name;
        public int index;

        public GroupMemberData(float x, float z, String name, int index) {
            this.x = x;
            this.z = z;
            this.name = name;
            this.index = index;
        }

        public int getColor() {
            switch (this.index % 5) {
                case 1: return 0xFFFF4444; // 빨강
                case 2: return 0xFF4444FF; // 파랑
                case 3: return 0xFF44FF44; // 초록
                case 4: return 0xFFFFFF44; // 노랑
                default: return 0xFFFFFFFF; // 흰색
            }
        }
    }
}