package plus.dragons.createenchantmentindustry.registry;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;

import net.minecraft.core.Registry;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMountedFluidStorageType;

public final class CEIMountedStorageTypes {
    public static final ExperienceLanternMountedFluidStorageType EXPERIENCE_LANTERN = register(
            "experience_lantern",
            new ExperienceLanternMountedFluidStorageType());

    private CEIMountedStorageTypes() {
    }

    private static <T extends MountedFluidStorageType<?>> T register(String path, T type) {
        return Registry.register(CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE, CEIIdentifier.id(path), type);
    }

    public static void register() {
        MountedFluidStorageType.REGISTRY.register(CEIBlocks.EXPERIENCE_LANTERN, EXPERIENCE_LANTERN);
    }
}
