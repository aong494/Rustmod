package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "examplemod", bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class KeyInit {
    // 다른 클래스에서도 쓸 수 있게 public으로 선언
    public static final KeyMapping MAP_KEY = new KeyMapping(
            "맵 열기", // 언어 파일에서 사용할 키
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M, // 기본 키 설정: M
            "미니맵" // 설정창의 카테고리 이름
    );

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        // 여기서 실제로 등록을 수행합니다 (ClientRegistry 역할 대체)
        event.register(MAP_KEY);
    }
}