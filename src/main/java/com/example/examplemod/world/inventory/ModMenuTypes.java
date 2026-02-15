package com.example.examplemod.world.inventory;

import com.example.examplemod.ExampleMod;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ExampleMod.MODID);

    // 커스텀 화로 메뉴 등록
    public static final RegistryObject<MenuType<RustFurnaceMenu>> RUST_FURNACE_MENU =
            MENUS.register("rust_furnace_menu", () -> IForgeMenuType.create(RustFurnaceMenu::new));

    public static void register(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}