package plus.dragons.createenchantmentindustry.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import plus.dragons.createenchantmentindustry.CEI;

public final class CEIIdentifier {
    private CEIIdentifier() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(CEI.MOD_ID, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String path) {
        return ResourceKey.create(registry, id(path));
    }
}
