package plus.dragons.createenchantmentindustry.foundation.blaze;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;

/**
 * CEI-local replacement for the CDP blaze block base.
 *
 * <p>Preserves the shared block-state contract used by CEI blaze machines: a horizontal facing, a Create-compatible
 * heat level property, typed block-entity lookup through Create-Fly's {@link IBE}, and a fuel interaction flow that
 * delegates machine-specific fuel rules to the local block entity. It intentionally does not port CDP rendering,
 * advancement, or concrete fuel registries.</p>
 */
public abstract class CEIBlazeBlock<T extends CEIBlazeBlockEntity> extends HorizontalDirectionalBlock implements IBE<T> {
    public static final EnumProperty<HeatLevel> HEAT_LEVEL = EnumProperty.create("blaze", HeatLevel.class);

    protected CEIBlazeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.SOUTH)
                .setValue(HEAT_LEVEL, HeatLevel.SMOULDERING));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, HEAT_LEVEL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        boolean doNotConsume = player.isCreative();
        boolean forceOverflow = true;
        InteractionResult result = applyFuel(state, level, pos, stack, forceOverflow, doNotConsume, false);
        if (result instanceof InteractionResult.Success success) {
            ItemStack leftover = success.heldItemTransformedTo();
            if (!level.isClientSide() && !doNotConsume && leftover != null && !leftover.isEmpty()) {
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, leftover);
                } else if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        }
        return result.consumesAction() ? result : InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    public static InteractionResult applyFuel(
            BlockState state,
            Level level,
            BlockPos pos,
            ItemStack stack,
            boolean forceOverflow,
            boolean doNotConsume,
            boolean simulate) {
        if (!state.hasBlockEntity()) {
            return InteractionResult.FAIL;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CEIBlazeBlockEntity blaze)) {
            return InteractionResult.FAIL;
        }
        return blaze.tryApplyFuel(stack, forceOverflow, doNotConsume, simulate);
    }

    public static HeatLevel getHeatLevelOf(BlockState state) {
        return state.hasProperty(HEAT_LEVEL) ? state.getValue(HEAT_LEVEL) : HeatLevel.NONE;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && state.getBlock() != oldState.getBlock()) {
            withBlockEntityDo(level, pos, CEIBlazeBlockEntity::updateBlockState);
        }
    }

    @Override
    public abstract Class<T> getBlockEntityClass();
}
