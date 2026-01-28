package com.example.examplemod.gui;

import com.example.examplemod.ExampleMod;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class InventoryScreenHandler {
    
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // 바닐라 인벤토리 화면이 열릴 때 러스트 스타일로 교체
        if (event.getScreen() instanceof InventoryScreen vanillaScreen) {
            // 러스트 스타일 인벤토리로 교체 (Enhanced 버전 사용)
            RustStyleInventoryScreenEnhanced rustScreen = new RustStyleInventoryScreenEnhanced(
                vanillaScreen.getMenu(),
                vanillaScreen.getMinecraft().player.getInventory(),
                vanillaScreen.getTitle()
            );
            event.setNewScreen(rustScreen);
        }
    }
}
