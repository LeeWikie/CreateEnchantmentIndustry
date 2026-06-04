package plus.dragons.createenchantmentindustry.registry;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidEntry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.CEI;

public final class CEIFluids {
    public static final FlowableFluid EXPERIENCE = registerFlowable("experience");
    public static final FlowableFluid EXPERIENCE_FLOWING = EXPERIENCE.getEntry().flowing;

    private CEIFluids() {
    }

    private static FlowableFluid registerFlowable(String path) {
        FluidEntry entry = new FluidEntry();
        entry.still = new FlowableFluid.Still(entry);
        entry.flowing = new FlowableFluid.Flowing(entry);
        register(path, entry.still);
        registerFlowing(path, entry.flowing);
        return entry.still;
    }

    public static <T extends Fluid> T register(String path, T fluid) {
        ResourceKey<Fluid> key = CEIIdentifier.key(Registries.FLUID, path);
        return Registry.register(BuiltInRegistries.FLUID, key, fluid);
    }

    public static <T extends Fluid> T registerFlowing(String basePath, T fluid) {
        return register("flowing_" + basePath, fluid);
    }

    public static void register() {
        Identifier experienceId = BuiltInRegistries.FLUID.getKey(EXPERIENCE);
        Identifier flowingExperienceId = BuiltInRegistries.FLUID.getKey(EXPERIENCE_FLOWING);
        CEI.LOGGER.info("CEI Liquid Experience registered: still={}, flowing={}", experienceId, flowingExperienceId);
        CEI.LOGGER.info("CEI Liquid Experience open-pipe effect is currently a safe no-op in the Fabric port.");
    }
}
