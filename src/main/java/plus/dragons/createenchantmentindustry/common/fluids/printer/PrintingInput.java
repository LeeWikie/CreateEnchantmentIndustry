package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public record PrintingInput(ItemStack base, ItemStack template, FluidStack fluid) implements RecipeInput {
    public static PrintingInput fromTank(ItemStack base, ItemStack template, CEIConfigurableFluidTank tank) {
        FluidStack fluid = tank.isEmpty()
                ? FluidStack.EMPTY
                : new FluidStack(tank.getFluidVariant().getFluid(), Math.toIntExact(tank.getAmount()));
        return new PrintingInput(base, template, fluid);
    }

    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> base;
            case 1 -> template;
            default -> throw new IllegalArgumentException("No item for index " + index);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
