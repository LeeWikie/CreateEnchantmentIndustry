package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import plus.dragons.createenchantmentindustry.registry.CEIRecipeTypes;

public record GrindingRecipe(
        int time,
        List<ProcessingOutput> results,
        List<FluidStack> fluidResults,
        List<FluidIngredient> fluidIngredients,
        Ingredient ingredient) implements CreateSingleStackRollableRecipe, TimedRecipe {
    public static final int DEFAULT_PROCESSING_TIME = 50;
    public static final MapCodec<GrindingRecipe> MAP_CODEC = RecordCodecBuilder.<GrindingRecipe>mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("processing_time", DEFAULT_PROCESSING_TIME).forGetter(GrindingRecipe::time),
            ProcessingOutput.CODEC.listOf(1, 4).optionalFieldOf("results", List.of()).forGetter(GrindingRecipe::results),
            FluidStack.CODEC.listOf(1, 1).optionalFieldOf("fluid_results", List.of()).forGetter(GrindingRecipe::fluidResults),
            FluidIngredient.CODEC.listOf(1, 1).optionalFieldOf("fluid_ingredients", List.of()).forGetter(GrindingRecipe::fluidIngredients),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(GrindingRecipe::ingredient))
            .apply(instance, GrindingRecipe::new)).validate(GrindingRecipe::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            GrindingRecipe::time,
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
            GrindingRecipe::results,
            FluidStack.PACKET_CODEC.apply(ByteBufCodecs.list()),
            GrindingRecipe::fluidResults,
            FluidIngredient.PACKET_CODEC.apply(ByteBufCodecs.list()),
            GrindingRecipe::fluidIngredients,
            Ingredient.CONTENTS_STREAM_CODEC,
            GrindingRecipe::ingredient,
            GrindingRecipe::new);
    public static final RecipeSerializer<GrindingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private static DataResult<GrindingRecipe> validate(GrindingRecipe recipe) {
        if (recipe.time <= 0) {
            return DataResult.error(() -> "Grinding recipe processing_time must be positive");
        }
        if (recipe.results.isEmpty() && recipe.fluidResults.isEmpty()) {
            return DataResult.error(() -> "Grinding recipe must have an item result or a fluid result");
        }
        if (recipe.fluidIngredients.size() + recipe.fluidResults.size() > 1) {
            return DataResult.error(() -> "Grinding recipe can have at most one fluid input or one fluid result");
        }
        return DataResult.success(recipe);
    }

    public int getProcessingDuration() {
        return time;
    }

    @Override
    public RecipeSerializer<GrindingRecipe> getSerializer() {
        return CEIRecipeTypes.GRINDING.serializer();
    }

    @Override
    public RecipeType<GrindingRecipe> getType() {
        return CEIRecipeTypes.GRINDING.type();
    }
}
