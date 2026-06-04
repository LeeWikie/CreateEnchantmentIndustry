package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class ExperienceLanternBlock extends WrenchableDirectionalBlock implements IBE<ExperienceLanternBlockEntity> {
    public static final MapCodec<ExperienceLanternBlock> CODEC = simpleCodec(ExperienceLanternBlock::new);
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public ExperienceLanternBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.UP)
                .setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(LIGHT, 0);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public Class<ExperienceLanternBlockEntity> getBlockEntityClass() {
        return ExperienceLanternBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExperienceLanternBlockEntity> getBlockEntityType() {
        return CEIBlockEntityTypes.EXPERIENCE_LANTERN;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
