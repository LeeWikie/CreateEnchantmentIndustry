package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class MechanicalGrindstoneBlock extends RotatedPillarKineticBlock implements IBE<MechanicalGrindstoneBlockEntity> {
    private static final VoxelShape SHAPE = Block.box(3, 3, 3, 13, 13, 13);

    public MechanicalGrindstoneBlock(Properties properties) {
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
        if (stack.isEmpty() || player.isSecondaryUseActive()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        MechanicalGrindstoneBlockEntity grindstone = getBlockEntity(level, pos);
        if (grindstone == null || Math.abs(grindstone.getSpeed()) < SpeedLevel.FAST.getSpeedValue()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos.below()) instanceof GrindstoneDrainBlockEntity drain)) {
            return InteractionResult.PASS;
        }
        if (!drain.tryInsert(stack, true)) {
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
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return getRotationAxis(state) == face.getAxis();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.FAST;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public Class<MechanicalGrindstoneBlockEntity> getBlockEntityClass() {
        return MechanicalGrindstoneBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MechanicalGrindstoneBlockEntity> getBlockEntityType() {
        return CEIBlockEntityTypes.MECHANICAL_GRINDSTONE;
    }
}
