package com.example.examplemod;

import com.example.examplemod.gui.CraftingRustScreen;
import com.example.examplemod.gui.MapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "examplemod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Screen currentScreen = event.getScreen();

        if (currentScreen instanceof MenuAccess<?> menuAccess) {
            String titleText = currentScreen.getTitle().getString();

            if (titleText.contains("제작 시스템")) {
                if (menuAccess.getMenu() instanceof ChestMenu chestMenu) {
                    event.setNewScreen(new CraftingRustScreen(
                            chestMenu,
                            Minecraft.getInstance().player.getInventory(),
                            currentScreen.getTitle()
                    ));
                }
            }
        }
        if (event.getScreen() instanceof net.minecraft.client.gui.screens.DeathScreen) {
            event.setNewScreen(new com.example.examplemod.gui.CustomDeathScreen());
        }
    }
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == 1) { // 1 = Press (누름)
            if (KeyInit.MAP_KEY.consumeClick()) { // consumeClick()이 중복 방지에 더 좋습니다.
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.screen == null) {
                    // [수정 핵심] 마인크래프트 화면을 우리 지도 화면으로 교체
                    mc.setScreen(new MapScreen());
                }
            }
        }
    }

}