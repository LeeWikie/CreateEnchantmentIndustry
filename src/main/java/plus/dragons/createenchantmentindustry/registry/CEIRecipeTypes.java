package plus.dragons.createenchantmentindustry.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrintingRecipe;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindingRecipe;
import plus.dragons.createenchantmentindustry.foundation.recipe.CEIRecipeTypeInfo;

public final class CEIRecipeTypes {
    public static final CEIRecipeTypeInfo<PrintingRecipe> PRINTING = registerInfo("printing", PrintingRecipe.SERIALIZER);
    public static final CEIRecipeTypeInfo<GrindingRecipe> GRINDING = registerInfo("grinding", GrindingRecipe.SERIALIZER);

    private CEIRecipeTypes() {
    }

    public static <T extends Recipe<?>> RecipeType<T> registerType(String path) {
        Identifier id = CEIIdentifier.id(path);
        return Registry.register(
                BuiltInRegistries.RECIPE_TYPE,
                id,
                new RecipeType<>() {
                    @Override
                    public String toString() {
                        return id.toString();
                    }
                });
    }

    public static <T extends Recipe<?>, S extends RecipeSerializer<T>> S registerSerializer(String path, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CEIIdentifier.id(path), serializer);
    }

    public static <T extends Recipe<?>> CEIRecipeTypeInfo<T> registerInfo(String path, RecipeSerializer<T> serializer) {
        Identifier id = CEIIdentifier.id(path);
        RecipeType<T> type = registerType(path);
        RecipeSerializer<T> registeredSerializer = registerSerializer(path, serializer);
        return new CEIRecipeTypeInfo<>(id, type, registeredSerializer);
    }

    public static void register() {
    }
}
