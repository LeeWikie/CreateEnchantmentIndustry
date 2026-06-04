package plus.dragons.createenchantmentindustry.foundation.blaze;

import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * CEI-local replacement for the CDP blaze block entity base.
 *
 * <p>Preserves the original CEI/CDP state responsibilities that downstream blaze machines need first: creative mode
 * fuel state, heat-level synchronization with the block, Create-Fly {@link SmartBlockEntity} behaviour hooks, and a
 * single overridable fuel insertion entry point. Fluid contents and concrete fuel lookup remain machine-owned so the
 * Fabric port does not inherit CDP tank or recipe registries.</p>
 */
public abstract class CEIBlazeBlockEntity extends SmartBlockEntity {
    private boolean creative;

    protected CEIBlazeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public boolean isCreative() {
        return creative;
    }

    public void applyCreativeFuel() {
        if (creative) {
            return;
        }
        creative = true;
        updateBlockState();
    }

    public void clearCreativeFuel() {
        if (!creative) {
            return;
        }
        creative = false;
        updateBlockState();
    }

    public InteractionResult tryApplyFuel(ItemStack stack, boolean forceOverflow, boolean doNotConsume, boolean simulate) {
        return InteractionResult.PASS;
    }

    public HeatLevel getHeatLevel() {
        return creative ? HeatLevel.SEETHING : HeatLevel.SMOULDERING;
    }

    public HeatLevel getHeatLevelFromBlock() {
        return CEIBlazeBlock.getHeatLevelOf(getBlockState());
    }

    public HeatLevel getHeatLevelForRender() {
        return getHeatLevelFromBlock();
    }

    public void updateBlockState() {
        setBlockHeat(getHeatLevel());
    }

    protected void setBlockHeat(HeatLevel heat) {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        if (!state.hasProperty(CEIBlazeBlock.HEAT_LEVEL) || getHeatLevelFromBlock() == heat) {
            return;
        }
        level.setBlockAndUpdate(worldPosition, state.setValue(CEIBlazeBlock.HEAT_LEVEL, heat));
        notifyUpdate();
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putBoolean("isCreative", creative);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        creative = view.getBooleanOr("isCreative", false);
        super.read(view, clientPacket);
    }
}
