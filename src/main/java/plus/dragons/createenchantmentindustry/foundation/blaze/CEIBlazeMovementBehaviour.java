package plus.dragons.createenchantmentindustry.foundation.blaze;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class CEIBlazeMovementBehaviour extends MovementBehaviour {
    @Override
    @Nullable
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }
}
