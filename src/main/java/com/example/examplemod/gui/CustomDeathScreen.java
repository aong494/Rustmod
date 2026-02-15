package com.example.examplemod.gui;

import com.example.examplemod.network.ClientBedData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class CustomDeathScreen extends MapScreen {
    private boolean isRespawning = false;
    private static final float WORLD_SIZE = 9343.0f;
    private static final float TEXTURE_SIZE = 1024.0f;
    private static final float PIXEL_PER_BLOCK = TEXTURE_SIZE / WORLD_SIZE;

    public CustomDeathScreen() {
        super();
    }

    @Override
    protected void init() {
        super.init();
        // 부활 버튼 추가
        this.addRenderableWidget(Button.builder(Component.literal("부활하기"), (button) -> {
            if (this.minecraft.player != null && !isRespawning) {
                this.isRespawning = true;
                this.minecraft.player.respawn();
            }
        }).bounds(this.width / 2 - 50, this.height - 40, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 지도와 그룹 멤버 렌더링 (부모 클래스 기능)
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 2. 침대 아이콘 렌더링 (지도 좌표계 적용)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(this.width / 2.0f + (float)this.mapX, this.height / 2.0f + (float)this.mapY, 0);
        guiGraphics.pose().scale(this.scale, this.scale, 1.0f);

        for (com.example.examplemod.network.ClientBedData.BedInfo bed : com.example.examplemod.network.ClientBedData.PLAYER_BEDS) {
            float pixelPerBlock = 1024.0f / 9343.0f;
            float bX = bed.pos.getX() * pixelPerBlock;
            float bZ = bed.pos.getZ() * pixelPerBlock;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(bX, bZ, 0);
            float iconScale = 1.0f / this.scale;
            guiGraphics.pose().scale(iconScale, iconScale, 1.0f);

            int color = bed.isActive ? 0xFF55FF55 : 0xFFFF5555;
            guiGraphics.fill(-3, -3, 3, 3, color);
            if (bed.isActive) {
                guiGraphics.drawCenteredString(this.font, "Spawn", 0, -10, 0xFFFFFFFF);
            }
            guiGraphics.pose().popPose();
        }
        guiGraphics.pose().popPose();

        // 3. [해결 포인트] 기존의 사망 메시지 로직을 여기에 직접 작성
        // 이 부분은 지도 확대/이동의 영향을 받지 않는 화면 고정 UI입니다.
        if (isRespawning) {
            guiGraphics.drawCenteredString(this.font, "부활 중...", this.width / 2, 70, 0xFFFFFF00);
        } else {
            guiGraphics.drawCenteredString(this.font, "당신은 사망했습니다!", this.width / 2, 50, 0xFFFF4444);
        }
    }
}