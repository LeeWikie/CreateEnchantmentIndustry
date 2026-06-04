package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public final class GrindingSmokeHarness {
    private GrindingSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: GrindingSmokeHarness <powered|no-power|wrong-speed|drain-storage> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        bindItemComponents(Items.GRINDSTONE, Items.DIAMOND);
        List<String> lines = switch (args[0]) {
            case "powered" -> poweredSmoke();
            case "no-power" -> noPowerSmoke();
            case "wrong-speed" -> wrongSpeedSmoke();
            case "drain-storage" -> drainStorageSmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Files.write(Path.of(args[1]), lines);
    }

    private static List<String> poweredSmoke() {
        Fluid fluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = smokeTank(fluid, GrindstoneDrainBlockEntity.TANK_CAPACITY);
        GrindingRecipe recipe = smokeRecipe(fluid, 50);
        ItemStack input = new ItemStack(Items.GRINDSTONE);
        GrindstoneHelper.GrindingResult result = GrindstoneDrainBlockEntity.processForSmoke(recipe, input, tank, 32, -32);

        List<String> lines = new ArrayList<>();
        lines.add("mode=powered");
        lines.add("recipe_type=create_enchantment_industry:grinding");
        lines.add("input_remaining=" + input.getCount());
        lines.add("output=" + (result.outputs().isEmpty() ? "empty" : result.outputs().getFirst().getItem()));
        lines.add("output_count=" + (result.outputs().isEmpty() ? 0 : result.outputs().getFirst().getCount()));
        lines.add("fluid_amount=" + tank.getAmount());
        lines.add("powered_grinding_pass=" + (result.success()
                && input.isEmpty()
                && !result.outputs().isEmpty()
                && result.outputs().getFirst().is(Items.DIAMOND)
                && tank.getAmount() == 50));
        return lines;
    }

    private static List<String> noPowerSmoke() {
        Fluid fluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = smokeTank(fluid, GrindstoneDrainBlockEntity.TANK_CAPACITY);
        GrindingRecipe recipe = smokeRecipe(fluid, 50);
        ItemStack input = new ItemStack(Items.GRINDSTONE);
        GrindstoneHelper.GrindingResult result = GrindstoneDrainBlockEntity.processForSmoke(recipe, input, tank, 0, 0);

        List<String> lines = new ArrayList<>();
        lines.add("mode=no-power");
        lines.add("recipe_type=create_enchantment_industry:grinding");
        lines.add("input_remaining=" + input.getCount());
        lines.add("outputs=" + result.outputs().size());
        lines.add("fluid_amount=" + tank.getAmount());
        lines.add("no_power_pass=" + (!result.success()
                && input.getCount() == 1
                && result.outputs().isEmpty()
                && tank.getAmount() == 0));
        return lines;
    }

    private static List<String> wrongSpeedSmoke() {
        Fluid fluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = smokeTank(fluid, GrindstoneDrainBlockEntity.TANK_CAPACITY);
        GrindingRecipe recipe = smokeRecipe(fluid, 50);
        ItemStack input = new ItemStack(Items.GRINDSTONE);
        GrindstoneHelper.GrindingResult result = GrindstoneDrainBlockEntity.processForSmoke(recipe, input, tank, 32, 32);

        List<String> lines = new ArrayList<>();
        lines.add("mode=wrong-speed");
        lines.add("recipe_type=create_enchantment_industry:grinding");
        lines.add("input_remaining=" + input.getCount());
        lines.add("outputs=" + result.outputs().size());
        lines.add("fluid_amount=" + tank.getAmount());
        lines.add("wrong_speed_pass=" + (!result.success()
                && input.getCount() == 1
                && result.outputs().isEmpty()
                && tank.getAmount() == 0));
        return lines;
    }

    private static List<String> drainStorageSmoke() {
        Fluid fluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = smokeTank(fluid, GrindstoneDrainBlockEntity.TANK_CAPACITY);
        GrindingRecipe recipe = smokeRecipe(fluid, 50);
        ItemStack input = new ItemStack(Items.GRINDSTONE);
        GrindstoneHelper.GrindingResult result = GrindstoneDrainBlockEntity.processForSmoke(recipe, input, tank, 32, -32);

        List<String> lines = new ArrayList<>();
        lines.add("mode=drain-storage");
        lines.add("capacity=" + tank.getCapacity());
        lines.add("fluid_amount=" + tank.getAmount());
        lines.add("fluid_matches=" + tank.getFluidVariant().isOf(fluid));
        lines.add("drain_storage_pass=" + (result.success()
                && tank.getCapacity() == GrindstoneDrainBlockEntity.TANK_CAPACITY
                && tank.getAmount() == 50
                && tank.getFluidVariant().isOf(fluid)));
        return lines;
    }

    private static GrindingRecipe smokeRecipe(Fluid fluid, int fluidAmount) {
        return new GrindingRecipe(
                20,
                List.of(new ProcessingOutput(Items.DIAMOND, 1)),
                List.of(new FluidStack(fluid, fluidAmount)),
                List.of(),
                Ingredient.of(Items.GRINDSTONE));
    }

    private static CEIConfigurableFluidTank smokeTank(Fluid fluid, long capacity) {
        return new CEIConfigurableFluidTank(capacity, variant -> variant.isOf(fluid), variant -> variant.isOf(fluid), () -> { }) {
            @Override
            protected FluidVariant createVariant(Fluid fluid) {
                return new SmokeFluidVariant(fluid);
            }

            @Override
            protected FluidVariant createBlankVariant() {
                return new SmokeFluidVariant(Fluids.EMPTY);
            }
        };
    }

    private static void bindItemComponents(Item... items) {
        for (Item item : items) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            }
        }
    }

    private record SmokeFluidVariant(Fluid fluid) implements FluidVariant {
        @Override
        public boolean isBlank() {
            return fluid == Fluids.EMPTY;
        }

        @Override
        public Fluid getObject() {
            return fluid;
        }

        @Override
        public DataComponentPatch getComponentsPatch() {
            return DataComponentPatch.EMPTY;
        }

        @Override
        public DataComponentMap getComponents() {
            return DataComponentMap.EMPTY;
        }

        @Override
        public FluidVariant withComponents(DataComponentPatch changes) {
            if (!changes.isEmpty()) {
                throw new IllegalArgumentException("Smoke fluid variant does not support components");
            }
            return this;
        }
    }
}
