package plus.dragons.createenchantmentindustry.registry;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;

import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMovementBehaviour;
import plus.dragons.createenchantmentindustry.foundation.blaze.CEIBlazeMovementBehaviour;

public final class CEICreateBehaviours {
    public static final ExperienceLanternMovementBehaviour EXPERIENCE_LANTERN = new ExperienceLanternMovementBehaviour();
    public static final CEIBlazeMovementBehaviour BLAZE_MACHINE = new CEIBlazeMovementBehaviour();

    private CEICreateBehaviours() {
    }

    public static void register() {
        registerMovement(CEIBlocks.EXPERIENCE_LANTERN, EXPERIENCE_LANTERN);
        registerMovement(CEIBlocks.BLAZE_ENCHANTER, BLAZE_MACHINE);
        registerMovement(CEIBlocks.BLAZE_FORGER, BLAZE_MACHINE);
    }

    private static void registerMovement(Block block, MovementBehaviour behaviour) {
        MovementBehaviour existing = MovementBehaviour.REGISTRY.get(block);
        if (existing == null) {
            MovementBehaviour.REGISTRY.register(block, behaviour);
            return;
        }
        if (existing.getClass() != behaviour.getClass()) {
            throw new IllegalStateException("Create-Fly movement behaviour already registered for " + block);
        }
    }
}
