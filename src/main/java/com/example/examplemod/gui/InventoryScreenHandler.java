package com.example.examplemod.gui;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 이 어노테이션이 있어야 Forge가 이 파일을 감지합니다.
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InventoryScreenHandler {

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // 1. 기본 서바이벌 인벤토리 화면인지 확인
        if (event.getScreen() instanceof InventoryScreen vanillaScreen) {
            // Rust 스타일 스크린 자체가 InventoryScreen을 상속받았다면 무한 루프 방지
            if (event.getScreen() instanceof RustStyleInventoryScreen) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.gameMode == null) return;

            // 2. [핵심] 크리에이티브 모드가 아닐 때만 화면 교체!
            if (mc.gameMode.getPlayerMode() != net.minecraft.world.level.GameType.CREATIVE) {
                RustStyleInventoryScreen rustScreen = new RustStyleInventoryScreen(
                        vanillaScreen.getMenu(),
                        mc.player.getInventory(),
                        vanillaScreen.getTitle()
                );

                // event.setNewScreen을 사용하면 더 깔끔하게 교체됩니다.
                event.setNewScreen(rustScreen);
            }
        }
    }
}