package com.example.examplemod.block.renderer;

import com.example.examplemod.block.ArmoredDoorBlock;
import com.example.examplemod.block.BigDoorBlock;
import com.example.examplemod.block.BigDoorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import software.bernie.geckolib.cache.object.BakedGeoModel;
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
    @Override
    public void preRender(PoseStack poseStack, BigDoorBlockEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        // 1. 블록이 바라보는 방향 가져오기
        Direction facing = animatable.getBlockState().getValue(BigDoorBlock.FACING);
        // 2. 블록 중앙을 기준으로 회전하기 위해 모델을 중앙으로 이동 (0.5, 0, 0.5)
        poseStack.translate(0.5D, 0.0D, 0.5D);
        // 3. 방향에 따른 회전 (기존 코드에서 180도 차이가 난다면 +180 또는 -facing.get2DDataValue() * 90)
        // 아래 코드는 바닐라와 GeckoLib의 좌표 차이를 보정한 표준 방식입니다.
        float rotation = facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation + 180f)); // 여기서 180도를 더하거나 빼서 맞춥니다.
        // 4. 회전 후 다시 원래 위치로 복귀
        poseStack.translate(-0.5D, 0.0D, -0.5D);
    }
}