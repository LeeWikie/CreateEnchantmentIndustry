package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingInput;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public class RecipePrintingBehaviour implements PrintingBehaviour {
    private final ItemStack template;

    public RecipePrintingBehaviour(ItemStack template) {
        this.template = template.copyWithCount(1);
    }

    @Override
    public int getRequiredItemCount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return findRecipe(level, stack, tank).map(PrintingRecipe::baseCount).orElse(0);
    }

    @Override
    public long getRequiredFluidAmount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return findRecipe(level, stack, tank).map(recipe -> recipe.fluidIngredient().amount()).orElse(0);
    }

    @Override
    public int getProcessingTime(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return findRecipe(level, stack, tank).map(PrintingRecipe::processingTime).orElse(PrinterBehaviour.DEFAULT_PROCESSING_TIME);
    }

    @Override
    public ItemStack getResult(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return findRecipe(level, stack, tank)
                .map(recipe -> recipe.assemble(PrintingInput.fromTank(stack, template, tank)))
                .orElse(ItemStack.EMPTY);
    }

    private Optional<PrintingRecipe> findRecipe(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        PrintingInput input = PrintingInput.fromTank(stack, template, tank);
        for (RecipeHolder<?> holder : serverLevel.recipeAccess().getRecipes()) {
            if (holder.value() instanceof PrintingRecipe recipe && recipe.matches(input, level)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }
}
