package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class ExperienceHatchBlock extends HorizontalDirectionalBlock
        implements IBE<ExperienceHatchBlockEntity>, IWrenchable, ProperWaterloggedBlock {
    public static final MapCodec<ExperienceHatchBlock> CODEC = simpleCodec(ExperienceHatchBlock::new);

    public ExperienceHatchBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.SOUTH)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING, WATERLOGGED));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null || context.getClickedFace().getAxis().isVertical()) {
            return null;
        }
        return withWater(state.setValue(FACING, context.getClickedFace().getOpposite()), context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random) {
        updateWater(level, tickView, state, pos);
        return state;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (FakePlayerHandler.has(player)) {
            return InteractionResult.SUCCESS;
        }

        return onBlockEntityUse(level, pos, hatch -> {
            long transferred = player.isShiftKeyDown() ? hatch.withdrawExperience(player) : hatch.depositExperience(player);
            return transferred == 0 ? InteractionResult.PASS : InteractionResult.SUCCESS;
        });
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.ITEM_HATCH.get(state.getValue(FACING).getOpposite());
    }

    @Override
    public Class<ExperienceHatchBlockEntity> getBlockEntityClass() {
        return ExperienceHatchBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ExperienceHatchBlockEntity> getBlockEntityType() {
        return CEIBlockEntityTypes.EXPERIENCE_HATCH;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
