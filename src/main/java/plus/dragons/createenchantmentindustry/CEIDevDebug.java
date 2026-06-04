package plus.dragons.createenchantmentindustry;

import net.fabricmc.loader.api.FabricLoader;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.config.CEIEnchantmentsConfig;
import plus.dragons.createenchantmentindustry.config.CEIFluidsConfig;
import plus.dragons.createenchantmentindustry.config.CEIKineticsConfig;
import plus.dragons.createenchantmentindustry.config.CEIProcessingConfig;
import plus.dragons.createenchantmentindustry.config.CEIStressConfig;

public final class CEIDevDebug {
    private static final String PREFIX = "[CEI DEV DEBUG] ";

    private CEIDevDebug() {
    }

    public static boolean isDevMode() {
        FabricLoader loader = FabricLoader.getInstance();
        String version = loader.getModContainer(CEI.MOD_ID)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("");
        return version.contains("dev") || loader.isDevelopmentEnvironment();
    }

    public static void logDiagnostics() {
        if (!isDevMode()) return;

        FabricLoader loader = FabricLoader.getInstance();

        CEI.LOGGER.info(PREFIX + "=== Create Enchantment Industry Dev Debug ===");
        CEI.LOGGER.info(PREFIX + "Mod version: {}", modVersion(loader));
        CEI.LOGGER.info(PREFIX + "Environment: {}", loader.isDevelopmentEnvironment() ? "development" : "production");
        CEI.LOGGER.info(PREFIX + "Fabric Loader: {}", modVersion(loader, "fabricloader"));
        CEI.LOGGER.info(PREFIX + "Minecraft: {}", modVersion(loader, "minecraft"));
        CEI.LOGGER.info(PREFIX + "Fabric API: {}", modVersion(loader, "fabric-api"));
        CEI.LOGGER.info(PREFIX + "Create: {}", modVersion(loader, "create"));
        CEI.LOGGER.info(PREFIX + "Config path: {}",
                loader.getConfigDir().resolve(CEI.MOD_ID + ".json"));
        logConfigSummary();
        logRegistrySummary();
        CEI.LOGGER.info(PREFIX + "=== End CEI Dev Debug ===");
    }

    private static String modVersion(FabricLoader loader) {
        return modVersion(loader, CEI.MOD_ID);
    }

    private static String modVersion(FabricLoader loader, String modId) {
        return loader.getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("N/A");
    }

    private static void logConfigSummary() {
        CEI.LOGGER.info(PREFIX + "-- Client Config --");
        CEI.LOGGER.info(PREFIX + "experienceVisionMultiplier={}",
                CEIConfig.client().experienceVisionMultiplier());

        CEI.LOGGER.info(PREFIX + "-- Kinetics --");
        CEIKineticsConfig kin = CEIConfig.kinetics();
        CEI.LOGGER.info(PREFIX +
                "deployerKillDropXp={}, deployerKillXpScale={}, deployerMineDropXp={}, deployerMineXpScale={}",
                kin.deployerKillDropXp(), kin.deployerKillXpScale(),
                kin.deployerMineDropXp(), kin.deployerMineXpScale());
        CEI.LOGGER.info(PREFIX +
                "deployerCollectXp={}, deployerMendItem={}, deployerSweepAttack={}",
                kin.deployerCollectXp(), kin.deployerMendItem(), kin.deployerSweepAttack());
        CEI.LOGGER.info(PREFIX +
                "crushingWheelKillDropXp={}, chance={}, scale={}",
                kin.crushingWheelKillDropXp(), kin.crushingWheelKillDropXpChance(),
                kin.crushingWheelKillDropXpScale());

        CEIStressConfig stress = kin.stress();
        CEI.LOGGER.info(PREFIX +
                "mechanicalGrindstoneImpact={}, grindstoneDrainImpact={}",
                stress.mechanicalGrindstoneImpact(), stress.grindstoneDrainImpact());

        CEI.LOGGER.info(PREFIX + "-- Fluids --");
        CEIFluidsConfig fluid = CEIConfig.fluids();
        CEI.LOGGER.info(PREFIX +
                "printerFluidCapacity={}, blazeEnchanterFluidCapacity={}, blazeForgerFluidCapacity={}",
                fluid.printerFluidCapacity(), fluid.blazeEnchanterFluidCapacity(),
                fluid.blazeForgerFluidCapacity());
        CEI.LOGGER.info(PREFIX +
                "experienceLanternFluidCapacity={}, drainRate={}, pullRadius={}, pullForceMultiplier={}",
                fluid.experienceLanternFluidCapacity(), fluid.experienceLanternDrainRate(),
                fluid.experienceLanternPullRadius(), fluid.experienceLanternPullForceMultiplier());
        CEI.LOGGER.info(PREFIX +
                "mechanicalGrindstoneFluidCapacity={}, vaporizeOnPlacement={}",
                fluid.mechanicalGrindstoneFluidCapacity(), fluid.experienceVaporizeOnPlacement());
        CEI.LOGGER.info(PREFIX +
                "printing: genChange={}, enchantedBookCostMult={}, writtenBook={}, enchantedBook={}, copiable={}",
                fluid.printingGenerationChange(), fluid.printingEnchantedBookCostMultiplier(),
                fluid.enableWrittenBookPrinting(), fluid.enableEnchantedBookPrinting(),
                fluid.enableCreateCopiableItemPrinting());

        CEI.LOGGER.info(PREFIX + "-- Enchantments --");
        CEIEnchantmentsConfig ench = CEIConfig.enchantments();
        CEI.LOGGER.info(PREFIX +
                "blazeEnchanterMaxLevel={}, maxSuperLevel={}, maxLevelExtension={}, ignoreCompatibility={}, splitRespectExtension={}",
                ench.blazeEnchanterMaxEnchantLevel(), ench.blazeEnchanterMaxSuperEnchantLevel(),
                ench.enchantmentMaxLevelExtension(), ench.ignoreEnchantmentCompatibility(),
                ench.splitEnchantmentRespectLevelExtension());

        CEI.LOGGER.info(PREFIX + "-- Processing --");
        CEIProcessingConfig proc = CEIConfig.processing();
        CEI.LOGGER.info(PREFIX + "lightningStrikeTransformXpBlockChance={}",
                proc.regularLightningStrikeTransformXpBlockChance());
    }

    private static void logRegistrySummary() {
        CEI.LOGGER.info(PREFIX + "-- Registry / Content --");
        CEI.LOGGER.info(PREFIX +
                "Blocks: SUPER_EXPERIENCE_BLOCK, EXPERIENCE_HATCH, EXPERIENCE_LANTERN, PRINTER, " +
                "MECHANICAL_GRINDSTONE, GRINDSTONE_DRAIN, BLAZE_ENCHANTER, BLAZE_FORGER, EXPERIENCE (fluid block)");
        CEI.LOGGER.info(PREFIX +
                "Items: SUPER_EXPERIENCE_NUGGET, ENCHANTING_TEMPLATE, SUPER_ENCHANTING_TEMPLATE, " +
                "EXPERIENCE_BUCKET, EXPERIENCE_CAKE_BASE, EXPERIENCE_CAKE, EXPERIENCE_CAKE_SLICE + 8 block items");
        CEI.LOGGER.info(PREFIX + "Fluids: EXPERIENCE (still + flowing)");
        CEI.LOGGER.info(PREFIX +
                "Block Entities: EXPERIENCE_HATCH, EXPERIENCE_LANTERN, PRINTER, " +
                "MECHANICAL_GRINDSTONE, GRINDSTONE_DRAIN, BLAZE_ENCHANTER, BLAZE_FORGER");
        CEI.LOGGER.info(PREFIX + "Recipe Types: PRINTING, GRINDING");
        CEI.LOGGER.info(PREFIX +
                "Arm Interaction Points: BLAZE_ENCHANTER, BLAZE_FORGER");
        CEI.LOGGER.info(PREFIX + "Mounted Storage Types: EXPERIENCE_LANTERN");
        CEI.LOGGER.info(PREFIX +
                "Movement Behaviours: EXPERIENCE_LANTERN, BLAZE_MACHINE (3 blocks)");
        CEI.LOGGER.info(PREFIX +
                "Tags: EXPERIENCE_FLUIDS, BLAZE_ENCHANTER_ENCHANTING, " +
                "BLAZE_ENCHANTER_SUPER_ENCHANTING, BLAZE_ENCHANTER_DENY");
        CEI.LOGGER.info(PREFIX + "Creative Tabs: BASE");
    }
}
