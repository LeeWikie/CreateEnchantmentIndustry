package plus.dragons.createenchantmentindustry.registry;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;

import net.minecraft.core.Registry;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerArmInteractionPoint;

public final class CEIArmInteractionPointTypes {
    public static final BlazeEnchanterArmInteractionPoint.Type BLAZE_ENCHANTER = register(
            "blaze_enchanter",
            new BlazeEnchanterArmInteractionPoint.Type());
    public static final BlazeForgerArmInteractionPoint.Type BLAZE_FORGER = register(
            "blaze_forger",
            new BlazeForgerArmInteractionPoint.Type());

    private CEIArmInteractionPointTypes() {
    }

    private static <T extends ArmInteractionPointType> T register(String path, T type) {
        return Registry.register(CreateRegistries.ARM_INTERACTION_POINT_TYPE, CEIIdentifier.id(path), type);
    }

    public static void register() {
        ArmInteractionPointType.register();
    }
}
