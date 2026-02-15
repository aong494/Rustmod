package com.example.examplemod.block;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.block.renderer.RustFurnaceBlockEntity;
import com.example.examplemod.world.inventory.RustFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.tags.BlockTags;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExampleMod.MODID);
    // ModBlocks 클래스 상단에 전용 사운드 타입 정의
    // 2. Big Door (거대 문) - 철 블록보다 약간 더 단단함
    public static final RegistryObject<Block> BIG_DOOR = BLOCKS.register("big_door",
            () -> new BigDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0f, 600.0f) // 철 블록(5.0)보다 조금 더 단단함
                    .noOcclusion()));
    public static final RegistryObject<Block> DOOR_DUMMY = BLOCKS.register("door_dummy",
            () -> new DoorDummyBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(5.0f, 600.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Block> ARMORED_DOOR = BLOCKS.register("armored_door",
            () -> new ArmoredDoorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .sound(SoundType.METAL)
                    .strength(5.0f, 600.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));
    public static final RegistryObject<Block> TOOL_CUPBOARD = BLOCKS.register("tool_cupboard",
            () -> new ToolCupboardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0f)
                    .noOcclusion()
                    .dynamicShape()));
    public static final RegistryObject<Block> RUST_FURNACE = BLOCKS.register("rust_furnace",
            () -> new FurnaceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(FurnaceBlock.LIT) ? 13 : 0)
            ) {
                @Override
                public net.minecraft.world.InteractionResult use(BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
                    if (!level.isClientSide) {
                        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof RustFurnaceBlockEntity furnaceBE) {
                            player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) ->
                                    new RustFurnaceMenu(id, inv, furnaceBE, furnaceBE.getContainerData()),
                                    net.minecraft.network.chat.Component.literal("Rust Furnace")));
                        }
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
                }
                @Override
                public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
                    return ModBlockEntities.RUST_FURNACE_BE.get().create(pos, state);
                }
                @Override
                public <T extends net.minecraft.world.level.block.entity.BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
                    if (level.isClientSide) return null;
                    return createFurnaceTicker(level, type, ModBlockEntities.RUST_FURNACE_BE.get());
                }
                @Override
                public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) { return false; }
                @Override
                public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) { return Shapes.empty(); }
                @Override
                public void animateTick(BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
                    if (state.getValue(FurnaceBlock.LIT)) {
                        double x = (double)pos.getX() + 0.5D;
                        double y = (double)pos.getY() + 0.6D;
                        double z = (double)pos.getZ();

                        if (random.nextDouble() < 0.1D) {
                            level.playLocalSound(x, y, z, net.minecraft.sounds.SoundEvents.FURNACE_FIRE_CRACKLE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F, false);
                        }

                        net.minecraft.core.Direction direction = state.getValue(FurnaceBlock.FACING);
                        net.minecraft.core.Direction.Axis axis = direction.getAxis();
                        double d4 = random.nextDouble() * 0.6D - 0.3D;
                        double d5 = axis == net.minecraft.core.Direction.Axis.X ? (double)direction.getStepX() * 0.52D : d4;
                        double d6 = random.nextDouble() * 6.0D / 16.0D;
                        double d7 = axis == net.minecraft.core.Direction.Axis.Z ? (double)direction.getStepZ() * 0.52D : d4;

                        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE, x + d5, y + d6, z + d7, 0.0D, 0.0D, 0.0D);
                        level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x + d5, y + d6, z + d7, 0.0D, 0.0D, 0.0D);
                    }
                }
            });
    public static final RegistryObject<Block> GREEN_KEYCARD_BLOCK = BLOCKS.register("green_keycard_block",
            () -> new KeycardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()));
    public static final RegistryObject<Block> BLUE_KEYCARD_BLOCK = BLOCKS.register("blue_keycard_block",
            () -> new KeycardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()));
    public static final RegistryObject<Block> RED_KEYCARD_BLOCK = BLOCKS.register("red_keycard_block",
            () -> new KeycardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .noCollission()
                    .instabreak()
                    .noOcclusion()));
    public static final RegistryObject<Block> KEYCARD_READER = BLOCKS.register("keycard_reader",
            () -> new KeycardReaderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .noOcclusion()));
}