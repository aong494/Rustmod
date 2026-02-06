package com.example.examplemod.block.renderer;

import com.example.examplemod.block.ModBlocks;
import com.example.examplemod.block.ToolCupboardBlock;
import com.example.examplemod.block.ToolCupboardBlockEntity;
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

public class ToolCupboardRenderer implements BlockEntityRenderer<ToolCupboardBlockEntity> { // 대상 변경
    private final BlockRenderDispatcher blockRenderer;

    public ToolCupboardRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(ToolCupboardBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        poseStack.pushPose();

        // [수정] 이제 본체 위치(아래칸)에서 그대로 그립니다.
        // 겹침 방지를 위한 미세한 수치만 조정합니다.
        poseStack.translate(0.001, 0.001, 0.001);
        poseStack.scale(0.998f, 0.998f, 0.998f);

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