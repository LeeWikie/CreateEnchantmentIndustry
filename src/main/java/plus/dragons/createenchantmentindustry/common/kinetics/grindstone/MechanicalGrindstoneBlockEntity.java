package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class MechanicalGrindstoneBlockEntity extends KineticBlockEntity {
    public MechanicalGrindstoneBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.MECHANICAL_GRINDSTONE, pos, state);
    }
}
