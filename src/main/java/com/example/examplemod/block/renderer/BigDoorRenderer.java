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
import com.example.examplemod.block.model.BigDoorModel;
import org.jetbrains.annotations.Nullable;

public class BigDoorRenderer extends GeoBlockRenderer<BigDoorBlockEntity> {
    public BigDoorRenderer(BlockEntityRendererProvider.Context context) {
        super(new BigDoorModel());
    }

    @Override
    public void preRender(PoseStack poseStack, BigDoorBlockEntity animatable, BakedGeoModel bakedModel, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, bakedModel, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        if (animatable.getLevel() != null) {
            // 현재 블록의 FACING 값을 읽어옴
            Direction facing = animatable.getBlockState().getValue(BigDoorBlock.FACING);
            // 모델의 중심을 기준으로 회전시키기 위해 0.5만큼 이동
            poseStack.translate(0.5, 0, 0.5);
            // 방향에 따라 각도 조절
            float rotation = switch (facing) {
                case SOUTH -> 180f;
                case WEST -> 90f;
                case EAST -> 270f;
                default -> 0f; // NORTH
            };
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            // 다시 중심을 되돌림
            poseStack.translate(-0.5, 0, -0.5);
        }
    }
}