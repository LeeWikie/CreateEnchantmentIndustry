package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.registry.CEIMountedStorageTypes;

public class ExperienceLanternMountedStorage extends WrapperMountedFluidStorage<ExperienceLanternMountedStorage.Handler> {
    public static final MapCodec<ExperienceLanternMountedStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(ExperienceLanternMountedStorage::getCapacity),
            FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(ExperienceLanternMountedStorage::getFluid))
            .apply(instance, ExperienceLanternMountedStorage::new));

    private boolean dirty;

    protected ExperienceLanternMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
        super(type);
        wrapped = new Handler(capacity, stack, CEIFluids.EXPERIENCE);
    }

    protected ExperienceLanternMountedStorage(int capacity, FluidStack stack) {
        this(CEIMountedStorageTypes.EXPERIENCE_LANTERN, capacity, stack);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof ExperienceLanternBlockEntity lantern) {
            writeToTank(lantern.getTank(), getFluid());
        }
    }

    public FluidStack getFluid() {
        return wrapped.getFluid();
    }

    public int getCapacity() {
        return wrapped.getMaxAmountPerStack();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markClean() {
        dirty = false;
    }

    public static ExperienceLanternMountedStorage fromLantern(ExperienceLanternBlockEntity lantern) {
        CEIConfigurableFluidTank tank = lantern.getTank();
        return fromTank(tank);
    }

    public static ExperienceLanternMountedStorage fromTank(CEIConfigurableFluidTank tank) {
        return new ExperienceLanternMountedStorage(Math.toIntExact(tank.getCapacity()), toCreateStack(tank));
    }

    public static ExperienceLanternMountedStorage createForSmoke(int capacity, Fluid fluid, int amount) {
        MountedFluidStorageType<ExperienceLanternMountedStorage> smokeType = new MountedFluidStorageType<>(CODEC) {
            @Nullable
            @Override
            public ExperienceLanternMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
                return null;
            }
        };
        return new ExperienceLanternMountedStorage(smokeType, capacity, amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount), fluid);
    }

    private static FluidStack toCreateStack(CEIConfigurableFluidTank tank) {
        if (tank.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(tank.getFluidVariant().getFluid(), Math.toIntExact(tank.getAmount()));
    }

    private static void writeToTank(CEIConfigurableFluidTank tank, FluidStack stack) {
        if (stack.isEmpty()) {
            tank.clear();
            return;
        }
        tank.setFluid(FluidVariant.of(stack.getFluid()), stack.getAmount());
    }

    private ExperienceLanternMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack, Fluid acceptedFluid) {
        super(type);
        wrapped = new Handler(capacity, stack, acceptedFluid);
    }

    public final class Handler extends FluidTank {
        private final Fluid acceptedFluid;

        public Handler(int capacity, FluidStack stack, Fluid acceptedFluid) {
            super(capacity);
            this.acceptedFluid = acceptedFluid;
            setFluid(stack);
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            return stack.isOf(acceptedFluid);
        }

        @Override
        public void markDirty() {
            dirty = true;
        }
    }
}
