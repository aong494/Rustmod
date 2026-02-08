package com.example.examplemod;

import com.example.examplemod.gui.CraftingRustScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraftforge.api.distmarker.Dist;
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
    }
}