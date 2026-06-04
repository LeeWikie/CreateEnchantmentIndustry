package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.registry.CEIBlocks;

public class MechanicalGrindStoneItem extends BlockItem {
    public MechanicalGrindStoneItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return place(new PlaceContext(context));
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        if (context instanceof PlaceContext placeContext) {
            return placeContext.getPlacementState();
        }
        return super.getPlacementState(context);
    }

    public class PlaceContext extends BlockPlaceContext {
        private final boolean clickedDrain;

        public PlaceContext(UseOnContext context) {
            super(context);
            BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());
            clickedDrain = clickedState.is(AllBlocks.ITEM_DRAIN);
            replaceClicked |= clickedDrain;
        }

        @Nullable
        public BlockState getPlacementState() {
            if (clickedDrain) {
                return CEIBlocks.GRINDSTONE_DRAIN.defaultBlockState()
                        .setValue(HorizontalKineticBlock.HORIZONTAL_FACING, getHorizontalDirection().getOpposite());
            }
            return MechanicalGrindStoneItem.super.getPlacementState(this);
        }
    }
}
