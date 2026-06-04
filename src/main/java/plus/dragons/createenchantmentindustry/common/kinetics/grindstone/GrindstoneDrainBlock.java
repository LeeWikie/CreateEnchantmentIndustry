package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class GrindstoneDrainBlock extends HorizontalKineticBlock implements IBE<GrindstoneDrainBlockEntity> {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 13, 15);

    public GrindstoneDrainBlock(Properties properties) {
        super(properties);
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
        if (!(level.getBlockEntity(pos) instanceof GrindstoneDrainBlockEntity drain)) {
            return InteractionResult.PASS;
        }
        if (stack.isEmpty()) {
            ItemStack extracted = drain.extractFirstItem(false);
            if (extracted.isEmpty()) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!level.isClientSide()) {
                player.setItemInHand(hand, extracted);
            }
            return InteractionResult.SUCCESS;
        }
        if (hitResult.getDirection() != Direction.UP || !drain.tryInsert(stack, true)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            drain.tryInsert(stack, false);
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null) {
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred);
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(HORIZONTAL_FACING) == face;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<GrindstoneDrainBlockEntity> getBlockEntityClass() {
        return GrindstoneDrainBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GrindstoneDrainBlockEntity> getBlockEntityType() {
        return CEIBlockEntityTypes.GRINDSTONE_DRAIN;
    }
}
