package com.example.examplemod.block.renderer;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.block.ToolCupboardBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.client.model.data.ModelData;

public class ToolCupboardRenderer implements BlockEntityRenderer<TrappedChestBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public ToolCupboardRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }
    @Override
    public void render(TrappedChestBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        // 플러그인이 준 이름 "도구함"과 일치하는지 확인
        if (be.hasCustomName() && be.getCustomName().getString().equals("도구함")) {
            poseStack.pushPose();

            // 덫상자 텍스처와 겹쳐서 지직거리는(Z-fighting) 현상 방지를 위해 아주 미세하게 확대
            poseStack.translate(0.0005, 0.0005, 0.0005);
            poseStack.scale(0.999f, 0.999f, 0.999f);

            BlockState cupboardState = ModBlocks.TOOL_CUPBOARD.get().defaultBlockState()
                    .setValue(ToolCupboardBlock.HALF, DoubleBlockHalf.LOWER);

            this.blockRenderer.renderSingleBlock(
                    cupboardState,
                    poseStack,
                    buffer,
                    combinedLight,
                    combinedOverlay,
                    ModelData.EMPTY,
                    RenderType.cutout()
            );

            poseStack.popPose();
        }
    }
}