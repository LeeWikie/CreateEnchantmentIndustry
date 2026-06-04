package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import plus.dragons.createenchantmentindustry.registry.CEIRecipeTypes;

public record PrintingRecipe(
        int processingTime,
        int baseCount,
        ItemStackTemplate result,
        Ingredient baseIngredient,
        Ingredient templateIngredient,
        FluidIngredient fluidIngredient) implements CreateRecipe<PrintingInput> {
    public static final int DEFAULT_PROCESSING_TIME = 50;
    public static final MapCodec<PrintingRecipe> MAP_CODEC = RecordCodecBuilder.<PrintingRecipe>mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("processing_time", DEFAULT_PROCESSING_TIME).forGetter(PrintingRecipe::processingTime),
            Codec.intRange(1, 64).optionalFieldOf("base_count", 1).forGetter(PrintingRecipe::baseCount),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(PrintingRecipe::result),
            Ingredient.CODEC.fieldOf("base").forGetter(PrintingRecipe::baseIngredient),
            Ingredient.CODEC.fieldOf("template").forGetter(PrintingRecipe::templateIngredient),
            FluidIngredient.CODEC.fieldOf("fluid_ingredient").forGetter(PrintingRecipe::fluidIngredient))
            .apply(instance, PrintingRecipe::new)).validate(PrintingRecipe::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, PrintingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            PrintingRecipe::processingTime,
            ByteBufCodecs.VAR_INT,
            PrintingRecipe::baseCount,
            ItemStackTemplate.STREAM_CODEC,
            PrintingRecipe::result,
            Ingredient.CONTENTS_STREAM_CODEC,
            PrintingRecipe::baseIngredient,
            Ingredient.CONTENTS_STREAM_CODEC,
            PrintingRecipe::templateIngredient,
            FluidIngredient.PACKET_CODEC,
            PrintingRecipe::fluidIngredient,
            PrintingRecipe::new);
    public static final RecipeSerializer<PrintingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public PrintingRecipeParams params() {
        return PrintingRecipeParams.fromRecipe(this);
    }

    private static DataResult<PrintingRecipe> validate(PrintingRecipe recipe) {
        if (recipe.processingTime <= 0) {
            return DataResult.error(() -> "Printing recipe processing_time must be positive");
        }
        if (recipe.fluidIngredient.amount() <= 0) {
            return DataResult.error(() -> "Printing recipe fluid_ingredient amount must be positive");
        }
        return DataResult.success(recipe);
    }

    @Override
    public boolean matches(PrintingInput input, Level level) {
        return matches(input);
    }

    public boolean matches(PrintingInput input) {
        return input.base().getCount() >= baseCount
                && baseIngredient.test(input.base())
                && templateIngredient.test(input.template())
                && fluidIngredient.test(input.fluid());
    }

    @Override
    public ItemStack assemble(PrintingInput input) {
        return result.create();
    }

    @Override
    public RecipeSerializer<PrintingRecipe> getSerializer() {
        return CEIRecipeTypes.PRINTING.serializer();
    }

    @Override
    public RecipeType<PrintingRecipe> getType() {
        return CEIRecipeTypes.PRINTING.type();
    }
}
