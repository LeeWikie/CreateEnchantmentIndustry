package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;

public final class ExperienceRepairHelper {
    private ExperienceRepairHelper() {
    }

    public static boolean canRepairItem(ServerLevel level, ItemStack stack) {
        if (!stack.isDamaged()) {
            return false;
        }
        return stack.getEnchantments().getLevel(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING)) > 0;
    }

    public static int getExperienceFromFluid(FluidStack fluid) {
        if (fluid.isEmpty() || !fluid.isOf(CEIFluids.EXPERIENCE)) {
            return 0;
        }
        return ExperienceHatchBehaviour.getExperienceFromLiquid(fluid.getAmount());
    }

    public static int getFluidFromExperience(int amount) {
        return Math.toIntExact(ExperienceHatchBehaviour.getLiquidFromExperience(amount));
    }

    public static int repairItem(int amount, ServerLevel level, ItemStack stack, boolean simulate) {
        int repairing = EnchantmentHelper.modifyDurabilityToRepairFromXp(level, stack, amount);
        int repaired = Math.min(repairing, stack.getDamageValue());
        if (repaired == 0) {
            return 0;
        }
        if (!simulate) {
            stack.setDamageValue(stack.getDamageValue() - repaired);
        }
        return Math.max(1, repaired * amount / repairing);
    }
}
