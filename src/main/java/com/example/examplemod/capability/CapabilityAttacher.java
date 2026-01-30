package com.example.examplemod.capability;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID) // 모드 아이디 변수 확인!
public class CapabilityAttacher {

    @SubscribeEvent
    public static void attachPlayerCaps(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            // "player_gear"라는 이름으로 저장 공간(Capability)을 플레이어에게 실제로 붙여주는 순간입니다.
            event.addCapability(new ResourceLocation(ExampleMod.MODID, "player_gear"),
                    new PlayerGearCapability.Provider());
        }
    }
}