package plus.dragons.createenchantmentindustry.foundation.tag;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

/**
 * CEI-local tag helper replacing CDP tag factory shortcuts.
 *
 * <p>Preserves only the needed primitive: constructing typed tag keys for the CEI namespace or the common {@code c}
 * namespace. It does not port CDP datagen, optional integration, or tag population helpers.</p>
 */
public final class CEITagHelper {
    public static final String COMMON_NAMESPACE = "c";

    private CEITagHelper() {
    }

    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> registry, Identifier id) {
        return TagKey.create(registry, id);
    }

    public static <T> TagKey<T> common(ResourceKey<? extends Registry<T>> registry, String path) {
        return create(registry, Identifier.fromNamespaceAndPath(COMMON_NAMESPACE, path));
    }
}
