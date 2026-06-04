package plus.dragons.createenchantmentindustry.foundation.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * CEI-local value wrapper replacing CDP's recipe type info helper.
 *
 * <p>Preserves the original CEI convenience of carrying a recipe id, type, and serializer together while relying on
 * direct Fabric/vanilla registration performed by {@code CEIRecipeTypes}.</p>
 */
public record CEIRecipeTypeInfo<T extends Recipe<?>>(Identifier id, RecipeType<T> type, RecipeSerializer<T> serializer) {
}
