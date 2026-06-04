package plus.dragons.createenchantmentindustry.common.processing.forger;

import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.foundation.blaze.CEIBlazeBlock;
import plus.dragons.createenchantmentindustry.registry.CEIBlocks;

public class BlazeForgerArmInteractionPoint extends ArmInteractionPoint {
    public BlazeForgerArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    @Override
    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        if (!(level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger)) {
            return stack;
        }
        ItemStack input = stack.copy();
        InteractionResult result = CEIBlazeBlock.applyFuel(cachedState, level, pos, input, false, false, simulate);
        if (result instanceof InteractionResult.Success success) {
            ItemStack remainder = success.heldItemTransformedTo();
            if (remainder == null) {
                remainder = ItemStack.EMPTY;
            }
            if (input.isEmpty()) {
                return remainder;
            }
            if (!simulate && !remainder.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder);
            }
            return input;
        }
        if (result == InteractionResult.PASS) {
            return forger.insertItem(input, simulate);
        }
        return input;
    }

    @Override
    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        if (level.getBlockEntity(pos) instanceof BlazeForgerBlockEntity forger) {
            return forger.getInventory().extractItem(slot + 2, amount, simulate);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        return 2;
    }

    public static class Type extends ArmInteractionPointType {
        @Override
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.is(CEIBlocks.BLAZE_FORGER);
        }

        @Nullable
        @Override
        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new BlazeForgerArmInteractionPoint(this, level, pos, state);
        }
    }
}
