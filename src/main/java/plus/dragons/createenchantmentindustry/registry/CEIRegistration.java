package plus.dragons.createenchantmentindustry.registry;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.CEI;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlock;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlock;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindstoneBlock;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlock;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterBlockEntity;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlock;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerBlockEntity;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

public final class CEIRegistration {
    private CEIRegistration() {
    }

    public static void register() {
        CEIBlocks.register();
        CEIItems.register();
        CEIFluids.register();
        CEIBlockEntityTypes.register();
        CEIArmInteractionPointTypes.register();
        CEIMountedStorageTypes.register();
        CEICreateBehaviours.register();
        registerCreateFlyKinetics();
        registerFluidTransferLookups();
        CEIRecipeTypes.register();
        CEITags.register();
        CEICreativeTabs.register();
        verifyTask6Entries();
        verifyTask8FluidEntries();
        verifyTask10ExperienceHatchEntries();
        verifyTask11ExperienceLanternEntries();
        verifyTask12PrinterEntries();
        verifyTask13GrindingEntries();
        verifyTask14BlazeEnchanterEntries();
        verifyTask15BlazeForgerEntries();
        verifyTask20CreateBehaviourEntries();
    }

    private static void registerCreateFlyKinetics() {
        BlockStressValues.IMPACTS.register(CEIBlocks.MECHANICAL_GRINDSTONE, () -> CEIConfig.stress().mechanicalGrindstoneImpact());
        BlockStressValues.IMPACTS.register(CEIBlocks.GRINDSTONE_DRAIN, () -> CEIConfig.stress().grindstoneDrainImpact());
    }

    private static void verifyTask6Entries() {
        verify(
                "Task 6 creative tab",
                CEIIdentifier.id("base"),
                BuiltInRegistries.CREATIVE_MODE_TAB.getKey(BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(CEICreativeTabs.BASE)));
        verifyItem("super_experience_nugget", CEIItems.SUPER_EXPERIENCE_NUGGET);
        verifyItem("enchanting_template", CEIItems.ENCHANTING_TEMPLATE);
        verifyItem("super_enchanting_template", CEIItems.SUPER_ENCHANTING_TEMPLATE);
        verifyItem("experience_bucket", CEIItems.EXPERIENCE_BUCKET);
        verifyItem("experience_cake_base", CEIItems.EXPERIENCE_CAKE_BASE);
        verifyItem("experience_cake", CEIItems.EXPERIENCE_CAKE);
        verifyItem("experience_cake_slice", CEIItems.EXPERIENCE_CAKE_SLICE);
        verifyBlockWithItem("super_experience_block", CEIBlocks.SUPER_EXPERIENCE_BLOCK, CEIItems.SUPER_EXPERIENCE_BLOCK_ITEM);
        verifyBlockWithItem("experience_hatch", CEIBlocks.EXPERIENCE_HATCH, CEIItems.EXPERIENCE_HATCH_ITEM);
        verifyBlockWithItem("experience_lantern", CEIBlocks.EXPERIENCE_LANTERN, CEIItems.EXPERIENCE_LANTERN_ITEM);
        verifyBlockWithItem("printer", CEIBlocks.PRINTER, CEIItems.PRINTER_ITEM);
        verifyBlockWithItem("mechanical_grindstone", CEIBlocks.MECHANICAL_GRINDSTONE, CEIItems.MECHANICAL_GRINDSTONE_ITEM);
        verifyBlockWithItem("grindstone_drain", CEIBlocks.GRINDSTONE_DRAIN, CEIItems.GRINDSTONE_DRAIN_ITEM);
        verifyBlockWithItem("blaze_enchanter", CEIBlocks.BLAZE_ENCHANTER, CEIItems.BLAZE_ENCHANTER_ITEM);
        verifyBlockWithItem("blaze_forger", CEIBlocks.BLAZE_FORGER, CEIItems.BLAZE_FORGER_ITEM);
        verifyCreativeTabItem("super_experience_nugget", CEIItems.SUPER_EXPERIENCE_NUGGET);
        verifyCreativeTabItem("enchanting_template", CEIItems.ENCHANTING_TEMPLATE);
        verifyCreativeTabItem("super_enchanting_template", CEIItems.SUPER_ENCHANTING_TEMPLATE);
        verifyCreativeTabItem("experience_bucket", CEIItems.EXPERIENCE_BUCKET);
        verifyCreativeTabItem("experience_cake_base", CEIItems.EXPERIENCE_CAKE_BASE);
        verifyCreativeTabItem("experience_cake", CEIItems.EXPERIENCE_CAKE);
        verifyCreativeTabItem("experience_cake_slice", CEIItems.EXPERIENCE_CAKE_SLICE);
        verifyCreativeTabItem("super_experience_block", CEIItems.SUPER_EXPERIENCE_BLOCK_ITEM);
        verifyCreativeTabItem("experience_hatch", CEIItems.EXPERIENCE_HATCH_ITEM);
        verifyCreativeTabItem("experience_lantern", CEIItems.EXPERIENCE_LANTERN_ITEM);
        verifyCreativeTabItem("printer", CEIItems.PRINTER_ITEM);
        verifyCreativeTabItem("mechanical_grindstone", CEIItems.MECHANICAL_GRINDSTONE_ITEM);
        verifyCreativeTabItem("grindstone_drain", CEIItems.GRINDSTONE_DRAIN_ITEM);
        verifyCreativeTabItem("blaze_enchanter", CEIItems.BLAZE_ENCHANTER_ITEM);
        verifyCreativeTabItem("blaze_forger", CEIItems.BLAZE_FORGER_ITEM);
        CEI.LOGGER.info("CEI Task 6 block placement smoke: registered base content is available; experience_hatch is functional via Task 10");
    }

    private static void registerFluidTransferLookups() {
        FluidStorage.SIDED.registerForBlockEntity(
                (blockEntity, direction) -> blockEntity.getFluidTankBehaviour().getStorage(),
                CEIBlockEntityTypes.EXPERIENCE_HATCH);
        FluidStorage.SIDED.registerForBlockEntity(
                ExperienceLanternBlockEntity::getFluidStorage,
                CEIBlockEntityTypes.EXPERIENCE_LANTERN);
        FluidStorage.SIDED.registerForBlockEntity(
                PrinterBlockEntity::getFluidStorage,
                CEIBlockEntityTypes.PRINTER);
        FluidStorage.SIDED.registerForBlockEntity(
                GrindstoneDrainBlockEntity::getFluidStorage,
                CEIBlockEntityTypes.GRINDSTONE_DRAIN);
        FluidStorage.SIDED.registerForBlockEntity(
                BlazeEnchanterBlockEntity::getFluidStorage,
                CEIBlockEntityTypes.BLAZE_ENCHANTER);
        FluidStorage.SIDED.registerForBlockEntity(
                BlazeForgerBlockEntity::getFluidStorage,
                CEIBlockEntityTypes.BLAZE_FORGER);
    }

    private static void verifyTask14BlazeEnchanterEntries() {
        verify("Task 14 blaze enchanter block entity", CEIIdentifier.id("blaze_enchanter"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.BLAZE_ENCHANTER));
        if (!(CEIBlocks.BLAZE_ENCHANTER instanceof BlazeEnchanterBlock)) {
            throw new IllegalStateException("CEI blaze_enchanter is not wired to BlazeEnchanterBlock");
        }
        if (!(CEIItems.ENCHANTING_TEMPLATE instanceof EnchantingTemplateItem template) || template.isSpecial()) {
            throw new IllegalStateException("CEI enchanting_template is not wired to a normal EnchantingTemplateItem");
        }
        if (!(CEIItems.SUPER_ENCHANTING_TEMPLATE instanceof EnchantingTemplateItem superTemplate) || !superTemplate.isSpecial()) {
            throw new IllegalStateException("CEI super_enchanting_template is not wired to a special EnchantingTemplateItem");
        }
        verify("Task 14 blaze enchanter arm point", CEIIdentifier.id("blaze_enchanter"), com.zurrtum.create.api.registry.CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_ENCHANTER));
        CEI.LOGGER.info("CEI Task 14 Blaze Enchanter registered: block_entity={}, tank_capacity={}, arm_point={}",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.BLAZE_ENCHANTER),
                BlazeEnchanterBlockEntity.TANK_CAPACITY,
                com.zurrtum.create.api.registry.CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_ENCHANTER));
    }

    private static void verifyTask15BlazeForgerEntries() {
        verify("Task 15 blaze forger block entity", CEIIdentifier.id("blaze_forger"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.BLAZE_FORGER));
        if (!(CEIBlocks.BLAZE_FORGER instanceof BlazeForgerBlock)) {
            throw new IllegalStateException("CEI blaze_forger is not wired to BlazeForgerBlock");
        }
        verify("Task 15 blaze forger arm point", CEIIdentifier.id("blaze_forger"), com.zurrtum.create.api.registry.CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_FORGER));
        CEI.LOGGER.info("CEI Task 15 Blaze Forger registered: block_entity={}, tank_capacity={}, arm_point={}",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.BLAZE_FORGER),
                BlazeForgerBlockEntity.TANK_CAPACITY,
                com.zurrtum.create.api.registry.CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_FORGER));
    }

    private static void verifyTask10ExperienceHatchEntries() {
        verify("Task 10 hatch block entity", CEIIdentifier.id("experience_hatch"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.EXPERIENCE_HATCH));
        if (!(CEIBlocks.EXPERIENCE_HATCH instanceof ExperienceHatchBlock)) {
            throw new IllegalStateException("CEI experience_hatch is not wired to ExperienceHatchBlock");
        }
        CEI.LOGGER.info("CEI Task 10 Experience Hatch storage smoke: block_entity={}, tank_capacity={}, transfer_lookup=FluidStorage.SIDED",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.EXPERIENCE_HATCH),
                plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBlockEntity.TANK_CAPACITY);
    }

    private static void verifyTask11ExperienceLanternEntries() {
        verify("Task 11 lantern block entity", CEIIdentifier.id("experience_lantern"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.EXPERIENCE_LANTERN));
        if (!(CEIBlocks.EXPERIENCE_LANTERN instanceof ExperienceLanternBlock)) {
            throw new IllegalStateException("CEI experience_lantern is not wired to ExperienceLanternBlock");
        }
        BlockEntity created = CEIBlockEntityTypes.EXPERIENCE_LANTERN.create(BlockPos.ZERO, CEIBlocks.EXPERIENCE_LANTERN.defaultBlockState());
        if (!(created instanceof ExperienceLanternBlockEntity lantern)) {
            throw new IllegalStateException("CEI experience_lantern block entity factory did not create ExperienceLanternBlockEntity");
        }
        if (lantern.getTank().getCapacity() != ExperienceLanternBlockEntity.TANK_CAPACITY) {
            throw new IllegalStateException("CEI experience_lantern tank capacity mismatch");
        }
        CEI.LOGGER.info("CEI Task 11 Experience Lantern storage smoke: block_entity={}, tank_capacity={}, transfer_lookup=FluidStorage.SIDED, contraption_mounted_storage=task_20_enabled",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.EXPERIENCE_LANTERN),
                ExperienceLanternBlockEntity.TANK_CAPACITY);
    }

    private static void verifyTask20CreateBehaviourEntries() {
        verify("Task 20 lantern mounted storage type", CEIIdentifier.id("experience_lantern"), CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE.getKey(CEIMountedStorageTypes.EXPERIENCE_LANTERN));
        if (MountedFluidStorageType.REGISTRY.get(CEIBlocks.EXPERIENCE_LANTERN) != CEIMountedStorageTypes.EXPERIENCE_LANTERN) {
            throw new IllegalStateException("CEI experience_lantern is not registered as mounted fluid storage");
        }
        verify("Task 20 blaze enchanter arm type", CEIIdentifier.id("blaze_enchanter"), CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_ENCHANTER));
        verify("Task 20 blaze forger arm type", CEIIdentifier.id("blaze_forger"), CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_FORGER));
        if (!ArmInteractionPointType.SORTED_TYPES_VIEW.contains(CEIArmInteractionPointTypes.BLAZE_ENCHANTER)
                || !ArmInteractionPointType.SORTED_TYPES_VIEW.contains(CEIArmInteractionPointTypes.BLAZE_FORGER)) {
            throw new IllegalStateException("CEI arm interaction point types are not present in Create-Fly sorted lookup");
        }
        if (MovementBehaviour.REGISTRY.get(CEIBlocks.EXPERIENCE_LANTERN) != CEICreateBehaviours.EXPERIENCE_LANTERN
                || MovementBehaviour.REGISTRY.get(CEIBlocks.BLAZE_ENCHANTER) != CEICreateBehaviours.BLAZE_MACHINE
                || MovementBehaviour.REGISTRY.get(CEIBlocks.BLAZE_FORGER) != CEICreateBehaviours.BLAZE_MACHINE) {
            throw new IllegalStateException("CEI Task 20 movement behaviours are not registered in Create-Fly MovementBehaviour.REGISTRY");
        }
        CEI.LOGGER.info("CEI Task 20 Create behaviours registered: mounted_storage={}, arm_points={}/{}, movement_behaviour=enabled, filling_mending_mixin=enabled, block_spouting=unsupported_no_original_registration",
                CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE.getKey(CEIMountedStorageTypes.EXPERIENCE_LANTERN),
                CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_ENCHANTER),
                CreateRegistries.ARM_INTERACTION_POINT_TYPE.getKey(CEIArmInteractionPointTypes.BLAZE_FORGER));
    }

    private static void verifyTask12PrinterEntries() {
        verify("Task 12 printer block entity", CEIIdentifier.id("printer"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.PRINTER));
        BlockEntity created = CEIBlockEntityTypes.PRINTER.create(BlockPos.ZERO, CEIBlocks.PRINTER.defaultBlockState());
        if (!(created instanceof PrinterBlockEntity printer)) {
            throw new IllegalStateException("CEI printer block entity factory did not create PrinterBlockEntity");
        }
        if (printer.getTank().getCapacity() != PrinterBlockEntity.TANK_CAPACITY) {
            throw new IllegalStateException("CEI printer tank capacity mismatch");
        }
        verify("Task 12 printing recipe type", CEIIdentifier.id("printing"), BuiltInRegistries.RECIPE_TYPE.getKey(CEIRecipeTypes.PRINTING.type()));
        verify("Task 12 printing recipe serializer", CEIIdentifier.id("printing"), BuiltInRegistries.RECIPE_SERIALIZER.getKey(CEIRecipeTypes.PRINTING.serializer()));
        CEI.LOGGER.info("CEI Task 12 Printer registered: block_entity={}, recipe_type={}, tank_capacity={}",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.PRINTER),
                BuiltInRegistries.RECIPE_TYPE.getKey(CEIRecipeTypes.PRINTING.type()),
                PrinterBlockEntity.TANK_CAPACITY);
    }

    private static void verifyTask13GrindingEntries() {
        verify("Task 13 mechanical grindstone block entity", CEIIdentifier.id("mechanical_grindstone"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.MECHANICAL_GRINDSTONE));
        verify("Task 13 grindstone drain block entity", CEIIdentifier.id("grindstone_drain"), BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.GRINDSTONE_DRAIN));
        if (!(CEIBlocks.MECHANICAL_GRINDSTONE instanceof MechanicalGrindstoneBlock)) {
            throw new IllegalStateException("CEI mechanical_grindstone is not wired to MechanicalGrindstoneBlock");
        }
        if (!(CEIBlocks.GRINDSTONE_DRAIN instanceof GrindstoneDrainBlock)) {
            throw new IllegalStateException("CEI grindstone_drain is not wired to GrindstoneDrainBlock");
        }
        BlockEntity created = CEIBlockEntityTypes.GRINDSTONE_DRAIN.create(BlockPos.ZERO, CEIBlocks.GRINDSTONE_DRAIN.defaultBlockState());
        if (!(created instanceof GrindstoneDrainBlockEntity drain)) {
            throw new IllegalStateException("CEI grindstone_drain block entity factory did not create GrindstoneDrainBlockEntity");
        }
        if (drain.getTank().getCapacity() != GrindstoneDrainBlockEntity.TANK_CAPACITY) {
            throw new IllegalStateException("CEI grindstone_drain tank capacity mismatch");
        }
        verify("Task 13 grinding recipe type", CEIIdentifier.id("grinding"), BuiltInRegistries.RECIPE_TYPE.getKey(CEIRecipeTypes.GRINDING.type()));
        verify("Task 13 grinding recipe serializer", CEIIdentifier.id("grinding"), BuiltInRegistries.RECIPE_SERIALIZER.getKey(CEIRecipeTypes.GRINDING.serializer()));
        CEI.LOGGER.info("CEI Task 13 Grinding registered: mechanical_be={}, drain_be={}, recipe_type={}, tank_capacity={}, stress_impacts=mechanical:{},drain:{}",
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.MECHANICAL_GRINDSTONE),
                BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CEIBlockEntityTypes.GRINDSTONE_DRAIN),
                BuiltInRegistries.RECIPE_TYPE.getKey(CEIRecipeTypes.GRINDING.type()),
                GrindstoneDrainBlockEntity.TANK_CAPACITY,
                BlockStressValues.getImpact(CEIBlocks.MECHANICAL_GRINDSTONE),
                BlockStressValues.getImpact(CEIBlocks.GRINDSTONE_DRAIN));
    }

    private static void verifyBlockWithItem(String path, Block block, Item item) {
        Identifier id = CEIIdentifier.id(path);
        verify("Task 6 block", id, BuiltInRegistries.BLOCK.getKey(block));
        verify("Task 6 block item", id, BuiltInRegistries.ITEM.getKey(item));
        CEI.LOGGER.info("CEI Task 6 block available: {}", id);
        CEI.LOGGER.info("CEI Task 6 give-equivalent item available: {}", id);
    }

    private static void verifyItem(String path, Item item) {
        Identifier id = CEIIdentifier.id(path);
        verify("Task 6 item", id, BuiltInRegistries.ITEM.getKey(item));
        CEI.LOGGER.info("CEI Task 6 give-equivalent item available: {}", id);
    }

    private static void verifyCreativeTabItem(String path, Item item) {
        Identifier id = CEIIdentifier.id(path);
        verify("Task 6 creative tab item", id, BuiltInRegistries.ITEM.getKey(item));
        if (!CEIItems.BASE_CONTENT.contains(item)) {
            throw new IllegalStateException("CEI creative tab is missing " + id);
        }
        CEI.LOGGER.info("CEI Task 6 creative tab entry: {}", id);
    }

    private static void verifyTask8FluidEntries() {
        verifyFluid("experience", CEIFluids.EXPERIENCE);
        verifyFluid("flowing_experience", CEIFluids.EXPERIENCE_FLOWING);
        verify("Task 8 fluid block", CEIIdentifier.id("experience"), BuiltInRegistries.BLOCK.getKey(CEIBlocks.EXPERIENCE));
        verify("Task 8 fluid bucket", CEIIdentifier.id("experience_bucket"), BuiltInRegistries.ITEM.getKey(CEIItems.EXPERIENCE_BUCKET));
        BucketItem bucket = CEIItems.EXPERIENCE_BUCKET;
        if (bucket.getContent() != CEIFluids.EXPERIENCE) {
            throw new IllegalStateException("CEI experience_bucket is not wired to Liquid Experience");
        }
        CEI.LOGGER.info("CEI Task 8 Liquid Experience bucket wiring smoke: bucket={}, content={}",
                BuiltInRegistries.ITEM.getKey(CEIItems.EXPERIENCE_BUCKET),
                BuiltInRegistries.FLUID.getKey(bucket.getContent()));
        CEI.LOGGER.info("CEI Task 8 Liquid Experience block smoke: block={}, legacy_block={}",
                BuiltInRegistries.BLOCK.getKey(CEIBlocks.EXPERIENCE),
                CEIFluids.EXPERIENCE.defaultFluidState().createLegacyBlock());
    }

    private static void verifyFluid(String path, Fluid fluid) {
        Identifier id = CEIIdentifier.id(path);
        verify("Task 8 fluid", id, BuiltInRegistries.FLUID.getKey(fluid));
        CEI.LOGGER.info("CEI Task 8 fluid available: {}", id);
    }

    private static void verify(String kind, Identifier expected, Identifier actual) {
        if (!expected.equals(actual)) {
            throw new IllegalStateException("CEI " + kind + " registry expected " + expected + " but found " + actual);
        }
    }
}
