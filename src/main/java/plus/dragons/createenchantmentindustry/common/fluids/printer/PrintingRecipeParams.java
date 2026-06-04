package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.zurrtum.create.foundation.fluid.FluidIngredient;

import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

public record PrintingRecipeParams(
        int processingTime,
        int baseCount,
        ItemStackTemplate result,
        Ingredient baseIngredient,
        Ingredient templateIngredient,
        FluidIngredient fluidIngredient) {
    public static PrintingRecipeParams fromRecipe(PrintingRecipe recipe) {
        return new PrintingRecipeParams(
                recipe.processingTime(),
                recipe.baseCount(),
                recipe.result(),
                recipe.baseIngredient(),
                recipe.templateIngredient(),
                recipe.fluidIngredient());
    }
}
