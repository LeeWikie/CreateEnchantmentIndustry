package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class ExperienceLanternBlockEntity extends SmartBlockEntity {
    public static final long TANK_CAPACITY = CEIConfig.fluids().experienceLanternFluidCapacity();

    private CEIFluidTankBehaviour fluidTankBehaviour;

    public ExperienceLanternBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.EXPERIENCE_LANTERN, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(fluidTankBehaviour = new CEIFluidTankBehaviour(
                this,
                List.of(CEIConfigurableFluidTank.liquidExperience(TANK_CAPACITY, this::onTankContentsChanged))));
    }

    @Override
    public void initialize() {
        super.initialize();
        syncLightLevel();
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        if (!clientPacket) {
            syncLightLevel();
        }
    }

    public CEIFluidTankBehaviour getFluidTankBehaviour() {
        return fluidTankBehaviour;
    }

    public CEIConfigurableFluidTank getTank() {
        return fluidTankBehaviour.getPrimaryHandler();
    }

    @Nullable
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction direction) {
        if (direction == null || direction.getOpposite() == getBlockState().getValue(ExperienceLanternBlock.FACING)) {
            return fluidTankBehaviour.getStorage();
        }
        return null;
    }

    public static int getLightLevel(long amount, long capacity) {
        if (amount <= 0) {
            return 0;
        }
        return (int) Math.min(15, amount * 15 / capacity);
    }

    private void onTankContentsChanged() {
        setChanged();
        syncLightLevel();
    }

    private void syncLightLevel() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        int light = getLightLevel(getTank().getAmount(), getTank().getCapacity());
        if (state.hasProperty(ExperienceLanternBlock.LIGHT) && state.getValue(ExperienceLanternBlock.LIGHT) != light) {
            level.setBlockAndUpdate(worldPosition, state.setValue(ExperienceLanternBlock.LIGHT, light));
        }
    }
}
