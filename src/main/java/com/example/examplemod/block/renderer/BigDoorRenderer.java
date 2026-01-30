package com.example.examplemod.block.renderer;

import com.example.examplemod.block.BigDoorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BigDoorRenderer extends GeoBlockRenderer<BigDoorBlockEntity> {
    public BigDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new BigDoorModel());
    }

    // [중요] 문이 4x4로 매우 크기 때문에, 플레이어가 마스터 블록에서 조금만 멀어져도
    // 문이 사라지는 것을 방지하기 위해 렌더링 거리를 늘립니다.
    @Override
    public int getViewDistance() {
        return 128;
    }
}