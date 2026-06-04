package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import java.util.ArrayList;
import java.util.List;

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public final class GrindstoneHelper {
    private GrindstoneHelper() {
    }

    public static GrindingResult applyRecipe(
            GrindingRecipe recipe,
            ItemStack input,
            CEIConfigurableFluidTank tank,
            RandomSource random,
            boolean simulate) {
        if (input.isEmpty() || !recipe.matches(new SingleRecipeInput(input), null)) {
            return GrindingResult.failed(input);
        }

        try (Transaction transaction = Transaction.openOuter()) {
            if (!prepareFluidIngredients(recipe, tank, transaction)) {
                return GrindingResult.failed(input);
            }
            if (!prepareFluidResults(recipe, tank, transaction)) {
                return GrindingResult.failed(input);
            }
            List<ItemStack> outputs = new ArrayList<>();
            ProcessingOutput.rollOutput(random, recipe.results(), outputs::add);
            ItemStack remaining = input.copy();
            if (!simulate) {
                transaction.commit();
                input.shrink(1);
                remaining = input.copy();
            }
            return new GrindingResult(true, remaining, outputs, tank.getAmount());
        }
    }

    private static boolean prepareFluidIngredients(
            GrindingRecipe recipe,
            CEIConfigurableFluidTank tank,
            Transaction transaction) {
        for (FluidIngredient ingredient : recipe.fluidIngredients()) {
            if (tank.isEmpty()) {
                return false;
            }
            long required = ingredient.amount();
            FluidStack available = new FluidStack(tank.getFluidVariant().getFluid(), Math.toIntExact(tank.getAmount()));
            if (required <= 0 || !ingredient.test(available) || tank.getAmount() < required) {
                return false;
            }
            long extracted = tank.extract(tank.getFluidVariant(), required, transaction);
            if (extracted != required) {
                return false;
            }
        }
        return true;
    }

    private static boolean prepareFluidResults(
            GrindingRecipe recipe,
            CEIConfigurableFluidTank tank,
            Transaction transaction) {
        for (FluidStack fluidResult : recipe.fluidResults()) {
            if (fluidResult.isEmpty()) {
                return false;
            }
            var variant = tank.createFluidVariant(fluidResult.getFluid());
            long inserted = tank.insert(variant, fluidResult.getAmount(), transaction);
            if (inserted != fluidResult.getAmount()) {
                return false;
            }
        }
        return true;
    }

    public static List<ItemStack> copyOutputs(List<ItemStack> outputs) {
        return outputs.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    public record GrindingResult(boolean success, ItemStack remainingInput, List<ItemStack> outputs, long fluidAmount) {
        public static GrindingResult failed(ItemStack input) {
            return new GrindingResult(false, input.copy(), List.of(), 0);
        }
    }
}
