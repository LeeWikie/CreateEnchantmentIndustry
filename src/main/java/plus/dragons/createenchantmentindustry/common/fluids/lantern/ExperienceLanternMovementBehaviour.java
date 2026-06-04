package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;

public class ExperienceLanternMovementBehaviour extends MovementBehaviour {
    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide() || context.position == null) {
            return;
        }
        AABB effectiveArea = new AABB(
                context.position.subtract(0.5d, 0.5d, 0.5d),
                context.position.add(0.5d, 0.5d, 0.5d))
                .inflate(0.5d);
        if (context.world.getGameTime() % 10 == 0) {
            drainExperience(context.world, effectiveArea, context.contraption.getStorage().getFluids());
        }
        if (CEIConfig.fluids().experienceLanternPullToggle()) {
            pullExperience(context.world, effectiveArea, context.position);
        }
    }

    protected void drainExperience(Level level, AABB effectiveArea, MountedFluidStorageWrapper storage) {
        int rate = CEIConfig.fluids().experienceLanternDrainRate();
        List<Player> players = level.getEntitiesOfClass(Player.class, effectiveArea, player -> player.isAlive() && !player.isSpectator());
        if (!players.isEmpty()) {
            AtomicInteger available = new AtomicInteger();
            players.forEach(player -> {
                int playerExperience = ExperienceHatchBehaviour.getExperienceForPlayer(player);
                if (playerExperience >= rate) {
                    available.addAndGet(rate);
                } else if (playerExperience > 0) {
                    available.addAndGet(playerExperience);
                }
            });
            int inserted = storage.insert(new FluidStack(CEIFluids.EXPERIENCE, available.get()));
            for (Player player : players) {
                if (inserted <= 0) {
                    break;
                }
                int playerExperience = ExperienceHatchBehaviour.getExperienceForPlayer(player);
                int drained = Math.min(inserted, Math.min(rate, playerExperience));
                if (drained > 0) {
                    player.giveExperiencePoints(-drained);
                    inserted -= drained;
                }
            }
        }

        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(ExperienceOrb.class, effectiveArea);
        for (ExperienceOrb orb : experienceOrbs) {
            int amount = orb.getValue();
            int inserted = storage.insert(new FluidStack(CEIFluids.EXPERIENCE, amount));
            if (inserted == amount) {
                orb.remove(Entity.RemovalReason.DISCARDED);
            } else {
                if (inserted > 0) {
                    int remainder = amount - inserted;
                    level.addFreshEntity(new ExperienceOrb(level, orb.position(), orb.getDeltaMovement(), remainder));
                    orb.remove(Entity.RemovalReason.DISCARDED);
                }
                break;
            }
        }
    }

    protected void pullExperience(Level level, AABB effectiveArea, Vec3 position) {
        List<ExperienceOrb> experienceOrbs = level.getEntitiesOfClass(
                ExperienceOrb.class,
                effectiveArea.inflate(CEIConfig.fluids().experienceLanternPullRadius()));
        for (ExperienceOrb orb : experienceOrbs) {
            if (orb.getDeltaMovement().length() <= 0.5d) {
                double pushForce = CEIConfig.fluids().experienceLanternPullForceMultiplier() / orb.position().distanceTo(position);
                Vec3 direction = position.subtract(orb.position()).normalize().multiply(pushForce, pushForce, pushForce);
                orb.push(direction);
            }
        }
    }
}
