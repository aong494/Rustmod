package com.example.examplemod.block.renderer;

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
import com.example.examplemod.block.model.BigDoorModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BigDoorRenderer extends GeoBlockRenderer<BigDoorBlockEntity> {
    public BigDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new BigDoorModel());
    }

    @Override
    public void preRender(PoseStack poseStack, BigDoorBlockEntity animatable, BakedGeoModel bakedModel, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // 1. super를 호출하기 '전'에 미리 회전 데이터를 주입합니다.
        if (animatable != null && animatable.hasLevel()) {
            Direction facing = animatable.getBlockState().getValue(BigDoorBlock.FACING);

            // 블록 중심축 설정 (0.5, 0, 0.5)
            poseStack.pushPose(); // 현재 상태 저장

            // 블록 중앙으로 이동
            poseStack.translate(0.5, 0, 0.5);

            float rotation = switch (facing) {
                case SOUTH -> 180f;
                case WEST -> 90f;
                case EAST -> 270f;
                default -> 0f; // NORTH
            };

            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

            // 다시 원래 위치로 보정
            poseStack.translate(-0.5, 0, -0.5);

            // 주의: 여기서 super를 호출할 때 poseStack이 이미 회전된 상태여야 합니다.
        }

        super.preRender(poseStack, animatable, bakedModel, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (animatable != null && animatable.hasLevel()) {
            poseStack.popPose(); // 렌더링 후 상태 복구
        }
    }
}