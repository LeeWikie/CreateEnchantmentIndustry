package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public class CustomNamePrintingBehaviour implements PrintingBehaviour {
    private final Component name;

    private CustomNamePrintingBehaviour(Component name) {
        this.name = name.copy();
    }

    public static Optional<PrintingBehaviour> create(@Nullable Level level, ItemStack template) {
        if (!template.is(Items.NAME_TAG)) {
            return Optional.empty();
        }
        Component name = template.get(DataComponents.CUSTOM_NAME);
        return name == null ? Optional.empty() : Optional.of(new CustomNamePrintingBehaviour(name));
    }

    @Override
    public int getRequiredItemCount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        Component existing = CEIConfig.fluids().printingCustomNameAsItemName()
                ? stack.get(DataComponents.ITEM_NAME)
                : stack.get(DataComponents.CUSTOM_NAME);
        return name.equals(existing) ? 0 : 1;
    }

    @Override
    public long getRequiredFluidAmount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return PrinterBehaviour.DEFAULT_SPECIAL_FLUID_COST;
    }

    @Override
    public ItemStack getResult(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        ItemStack result = stack.copyWithCount(1);
        if (CEIConfig.fluids().printingCustomNameAsItemName()) {
            result.set(DataComponents.ITEM_NAME, name.copy());
            result.remove(DataComponents.CUSTOM_NAME);
        } else {
            result.set(DataComponents.CUSTOM_NAME, name.copy());
        }
        return result;
    }
}
