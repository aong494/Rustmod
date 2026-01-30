package com.example.examplemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BigDoorBlock extends BaseEntityBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

    public BigDoorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // 마스터 블록을 기준으로 4x4 범위를 더미 블록으로 채움
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (x == 0 && y == 0) continue; // 자기 자신 제외

                BlockPos dummyPos = pos.offset(x, y, 0); // 방향에 따라 offset 계산은 달라질 수 있음
                level.setBlock(dummyPos, ModBlocks.DOOR_DUMMY.get().defaultBlockState(), 3);

                // 더미 블록에게 마스터 블록의 위치를 알려줌
                if (level.getBlockEntity(dummyPos) instanceof DoorDummyEntity dummyEntity) {
                    dummyEntity.setMasterPos(pos);
                }
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            level.setBlock(pos, state.cycle(OPEN), 3);
            level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BigDoorBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        // 문이 열려있으면(OPEN == true) 충돌 박스를 비워서 통과 가능하게 만듭니다.
        return state.getValue(BigDoorBlock.OPEN) ? Shapes.empty() : Shapes.block();
    }
}