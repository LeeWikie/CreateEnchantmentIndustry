package plus.dragons.createenchantmentindustry.registry;

import java.util.function.BiFunction;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;

public final class CEIBlockEntityTypes {
    public static final BlockEntityType<ExperienceHatchBlockEntity> EXPERIENCE_HATCH = register(
            "experience_hatch",
            ExperienceHatchBlockEntity::new,
            CEIBlocks.EXPERIENCE_HATCH);
    public static final BlockEntityType<ExperienceLanternBlockEntity> EXPERIENCE_LANTERN = register(
            "experience_lantern",
            ExperienceLanternBlockEntity::new,
            CEIBlocks.EXPERIENCE_LANTERN);
    public static final BlockEntityType<PrinterBlockEntity> PRINTER = register(
            "printer",
            PrinterBlockEntity::new,
            CEIBlocks.PRINTER);
    public static final BlockEntityType<MechanicalGrindstoneBlockEntity> MECHANICAL_GRINDSTONE = register(
            "mechanical_grindstone",
            MechanicalGrindstoneBlockEntity::new,
            CEIBlocks.MECHANICAL_GRINDSTONE);
    public static final BlockEntityType<GrindstoneDrainBlockEntity> GRINDSTONE_DRAIN = register(
            "grindstone_drain",
            GrindstoneDrainBlockEntity::new,
            CEIBlocks.GRINDSTONE_DRAIN);
    public static final BlockEntityType<BlazeEnchanterBlockEntity> BLAZE_ENCHANTER = register(
            "blaze_enchanter",
            BlazeEnchanterBlockEntity::new,
            CEIBlocks.BLAZE_ENCHANTER);
    public static final BlockEntityType<BlazeForgerBlockEntity> BLAZE_FORGER = register(
            "blaze_forger",
            BlazeForgerBlockEntity::new,
            CEIBlocks.BLAZE_FORGER);

    private CEIBlockEntityTypes() {
    }

    public static <T extends BlockEntity> BlockEntityType<T> register(
            String path,
            BiFunction<BlockPos, BlockState, T> factory,
            Block... blocks) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                CEIIdentifier.id(path),
                FabricBlockEntityTypeBuilder.create((pos, state) -> factory.apply(pos, state), blocks).build());
    }

    public static void register() {
    }
}
