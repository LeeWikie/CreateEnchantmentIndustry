package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public interface PrintingBehaviour {
    int getRequiredItemCount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank);

    long getRequiredFluidAmount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank);

    ItemStack getResult(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank);

    default int getProcessingTime(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return PrinterBehaviour.DEFAULT_PROCESSING_TIME;
    }

    default void onFinished(@Nullable Level level, PrinterBlockEntity printer) {
    }

    @FunctionalInterface
    interface Provider {
        Optional<PrintingBehaviour> create(@Nullable Level level, ItemStack template);
    }
}
