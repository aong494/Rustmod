package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ArmoredDoorBlock extends DoorBlock {
    public ArmoredDoorBlock(BlockBehaviour.Properties properties) {
        // IRON 타입을 쓰면 내부적으로 소리가 예약되므로 그대로 둡니다.
        super(properties, BlockSetType.IRON);
    }

    // 1. 우클릭 시 소리 제어 (손으로 열 때)
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        state = state.cycle(OPEN);
        boolean isOpen = state.getValue(BlockStateProperties.OPEN);

        level.setBlock(pos, state, 10);
        this.playCustomSound(level, pos, isOpen); // 우리 소리 재생
        level.gameEvent(player, isOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // 2. 레드스톤 신호 변화 감지
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean hasSignal = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.relative(state.getValue(HALF) == DoubleBlockHalf.LOWER ? net.minecraft.core.Direction.UP : net.minecraft.core.Direction.DOWN));

        if (block != this && hasSignal != state.getValue(POWERED)) {
            if (hasSignal != state.getValue(OPEN)) {
                this.playCustomSound(level, pos, hasSignal);
                level.gameEvent(null, hasSignal ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
            }
            level.setBlock(pos, state.setValue(POWERED, hasSignal).setValue(OPEN, hasSignal), 2);
        }
    }

    // 2. 레드스톤 신호 시 소리 제어
    @Override
    public void setOpen(@Nullable net.minecraft.world.entity.Entity entity, Level level, BlockState state, BlockPos pos, boolean open) {
        if (state.getValue(OPEN) != open) {
            level.setBlock(pos, state.setValue(OPEN, open), 10);
            this.playCustomSound(level, pos, open);
            level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        }
    }

    // 공통 소리 재생 로직
    private void playCustomSound(Level level, BlockPos pos, boolean open) {
        level.playSound(null, pos,
                open ? ModSounds.ARMORED_DOOR_OPEN.get() : ModSounds.ARMORED_DOOR_CLOSE.get(),
                SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            // 커스텀 사운드 재생 (더미가 부서질 때 나는 소리)
            level.playSound(null, pos,
                    SoundEvent.createVariableRangeEvent(new ResourceLocation(ExampleMod.MODID, "custom_door_break")),
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        super.playerWillDestroy(level, pos, state, player);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // DoorBlock에서 상속받은 FACING, OPEN, HINGE, POWERED, HALF를 모두 등록해야 합니다.
        super.createBlockStateDefinition(builder);
    }
}