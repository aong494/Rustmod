package com.example.examplemod.sound;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ExampleMod.MODID);
    public static final RegistryObject<SoundEvent> BEGIN_MOVEMENT = SOUND_EVENTS.register("big_door_begin_movement",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, "big_door_begin_movement")));

    public static final RegistryObject<SoundEvent> MOVEMENT = SOUND_EVENTS.register("big_door_movement",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, "big_door_movement")));

    public static final RegistryObject<SoundEvent> CLOSE_SOUND = SOUND_EVENTS.register("big_door_close",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, "big_door_close")));
    public static final RegistryObject<SoundEvent> OPEN_FINISH = SOUND_EVENTS.register("big_door_open_finish",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, "big_door_open_finish")));
    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, name)));
    }
    public static final RegistryObject<SoundEvent> ARMORED_DOOR_OPEN =
            SOUND_EVENTS.register("armored_door_open",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("examplemod", "armored_door_open")));

    public static final RegistryObject<SoundEvent> ARMORED_DOOR_CLOSE =
            SOUND_EVENTS.register("armored_door_close",
                    () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("examplemod", "armored_door_close")));
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}