package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ExampleMod.MODID);
    public static final RegistryObject<Item> BIG_DOOR_ITEM = ITEMS.register("big_door",
            () -> new BlockItem(ModBlocks.BIG_DOOR.get(), new Item.Properties()));

    public static final RegistryObject<Item> DOOR_DUMMY_ITEM = ITEMS.register("door_dummy",
            () -> new BlockItem(ModBlocks.DOOR_DUMMY.get(), new Item.Properties()));

    public static final RegistryObject<Item> ARMORED_DOOR_ITEM = ITEMS.register("armored_door",
            () -> new BlockItem(ModBlocks.ARMORED_DOOR.get(), new Item.Properties()));
    public static final RegistryObject<Item> TOOL_CUPBOARD_ITEM = ITEMS.register("tool_cupboard",
            () -> new BlockItem(ModBlocks.TOOL_CUPBOARD.get(), new Item.Properties()));
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}