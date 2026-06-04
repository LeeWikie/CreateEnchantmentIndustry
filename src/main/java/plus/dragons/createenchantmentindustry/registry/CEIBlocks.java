package plus.dragons.createenchantmentindustry.registry;

import java.util.function.Function;

import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import com.zurrtum.create.infrastructure.fluids.FlowableFluid;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlock;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlock;

public final class CEIBlocks {
    public static final Block SUPER_EXPERIENCE_BLOCK = register(
            "super_experience_block",
            Block::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_BLOCK).lightLevel(state -> 15));
    public static final Block EXPERIENCE_HATCH = register(
            "experience_hatch",
            ExperienceHatchBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).lightLevel(state -> 12));
    public static final Block EXPERIENCE_LANTERN = register(
            "experience_lantern",
            ExperienceLanternBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).lightLevel(state -> state.getValue(ExperienceLanternBlock.LIGHT)));
    public static final Block PRINTER = register(
            "printer",
            PrinterBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK));
    public static final MechanicalGrindstoneBlock MECHANICAL_GRINDSTONE = register(
            "mechanical_grindstone",
            MechanicalGrindstoneBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion());
    public static final GrindstoneDrainBlock GRINDSTONE_DRAIN = register(
            "grindstone_drain",
            GrindstoneDrainBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion());
    public static final BlazeEnchanterBlock BLAZE_ENCHANTER = register(
            "blaze_enchanter",
            BlazeEnchanterBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).lightLevel(state -> 13).noOcclusion());
    public static final BlazeForgerBlock BLAZE_FORGER = register(
            "blaze_forger",
            BlazeForgerBlock::new,
            BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK).lightLevel(state -> 13).noOcclusion());
    public static final FluidBlock EXPERIENCE = registerFluid(
            "experience",
            CEIFluids.EXPERIENCE,
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)
                    .mapColor(MapColor.EMERALD)
                    .lightLevel(state -> 15)
                    .noLootTable());

    private CEIBlocks() {
    }

    public static <T extends Block> T register(
            String path,
            Function<BlockBehaviour.Properties, T> factory,
            BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = CEIIdentifier.key(Registries.BLOCK, path);
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    private static FluidBlock registerFluid(
            String path,
            FlowableFluid fluid,
            BlockBehaviour.Properties properties) {
        FluidBlock block = register(path, blockProperties -> new FluidBlock(fluid, blockProperties), properties);
        fluid.getEntry().block = block;
        return block;
    }

    public static void register() {
    }
}
