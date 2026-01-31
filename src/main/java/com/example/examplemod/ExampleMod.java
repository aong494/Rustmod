package com.example.examplemod;

import com.example.examplemod.gui.RustStyleChestScreen;
import com.example.examplemod.gui.RustStyleLargeChestScreen;
import com.example.examplemod.item.ModItems;
import com.example.examplemod.sound.ModSounds;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import software.bernie.geckolib.GeckoLib;
import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.block.ModBlockEntities;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ExampleMod.MODID)
public class    ExampleMod
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "examplemod";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // 2. 실제 커스텀 탭 정의
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ARMORED_DOOR_ITEM.get())) // 탭 아이콘 설정
                    .title(Component.translatable("creativetab.example_tab")) // 언어 파일에서 이름 설정 가능
                    .displayItems((parameters, output) -> {
                        // 탭 안에 들어갈 아이콘들 나열
                        output.accept(ModItems.BIG_DOOR_ITEM.get());
                        output.accept(ModItems.DOOR_DUMMY_ITEM.get());
                        output.accept(ModItems.ARMORED_DOOR_ITEM.get());
                    }).build());

    public ExampleMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        GeckoLib.initialize();

        CREATIVE_MODE_TABS.register(modEventBus);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(ModMessages::register);
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS ||
                event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModItems.DOOR_DUMMY_ITEM.get());
            event.accept(ModItems.BIG_DOOR_ITEM.get());
            event.accept(ModItems.ARMORED_DOOR_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.ARMORED_DOOR_ITEM.get());
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                // [안전 장치] try-catch로 감싸서 중복 등록 에러가 나더라도 게임이 꺼지지 않게 합니다.
                try {
                    // 작은 상자 등록
                    MenuScreens.register(MenuType.GENERIC_9x3, RustStyleChestScreen::new);
                } catch (IllegalStateException e) {
                    LOGGER.warn("RUST_UI: 9x3 screen already registered, skipping...");
                }

                try {
                    // 큰 상자 등록
                    MenuScreens.register(MenuType.GENERIC_9x6, RustStyleLargeChestScreen::new);
                } catch (IllegalStateException e) {
                    LOGGER.warn("RUST_UI: 9x6 screen already registered, skipping...");
                }

                LOGGER.info("RUST_UI: Registered all chest screens safely.");
            });
        }
        @SubscribeEvent
        public static void registerRenderers(final net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.BIG_DOOR.get(),
                    context -> new com.example.examplemod.block.renderer.BigDoorRenderer(context));
        }
    }
}
