package plus.dragons.createenchantmentindustry.common.fluids.experience;

import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;

public class ExperienceHatchBlockEntity extends SmartBlockEntity {
    public static final long TANK_CAPACITY = 1000;

    private CEIFluidTankBehaviour fluidTankBehaviour;

    public ExperienceHatchBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.EXPERIENCE_HATCH, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(fluidTankBehaviour = CEIFluidTankBehaviour.singleLiquidExperience(this, TANK_CAPACITY));
    }

    public CEIFluidTankBehaviour getFluidTankBehaviour() {
        return fluidTankBehaviour;
    }

    public CEIConfigurableFluidTank getTank() {
        return fluidTankBehaviour.getPrimaryHandler();
    }

    public long depositExperience(Player player) {
        int playerExperience = ExperienceHatchBehaviour.getExperienceForPlayer(player);
        long inserted = insertExperience(playerExperience);
        if (inserted > 0) {
            player.giveExperiencePoints(-ExperienceHatchBehaviour.getExperienceFromLiquid(inserted));
        }
        return inserted;
    }

    public long withdrawExperience(Player player) {
        long extracted = extractExperience(getTank().getAmount());
        if (extracted > 0) {
            player.giveExperiencePoints(ExperienceHatchBehaviour.getExperienceFromLiquid(extracted));
        }
        return extracted;
    }

    public long insertExperience(long experiencePoints) {
        if (experiencePoints <= 0) {
            return 0;
        }
        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = fluidTankBehaviour.getStorage().insert(
                    FluidVariant.of(CEIFluids.EXPERIENCE),
                    ExperienceHatchBehaviour.getLiquidFromExperience(experiencePoints),
                    transaction);
            transaction.commit();
        }
        if (inserted > 0) {
            notifyUpdate();
        }
        return inserted;
    }

    public long extractExperience(long requestedExperiencePoints) {
        if (requestedExperiencePoints <= 0) {
            return 0;
        }
        long extracted;
        try (Transaction transaction = Transaction.openOuter()) {
            extracted = fluidTankBehaviour.getStorage().extract(
                    FluidVariant.of(CEIFluids.EXPERIENCE),
                    ExperienceHatchBehaviour.getLiquidFromExperience(requestedExperiencePoints),
                    transaction);
            transaction.commit();
        }
        if (extracted > 0) {
            notifyUpdate();
        }
        return extracted;
    }
}
