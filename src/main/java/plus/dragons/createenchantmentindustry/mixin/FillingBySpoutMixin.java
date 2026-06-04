package plus.dragons.createenchantmentindustry.mixin;

import com.zurrtum.create.content.fluids.spout.FillingBySpout;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceRepairHelper;

@Mixin(value = FillingBySpout.class, remap = false)
public class FillingBySpoutMixin {
    @Inject(method = "canItemBeFilled", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/transfer/GenericItemFilling;canItemBeFilled(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Z"), cancellable = true)
    private static void canItemBeFilled$mending(Level level, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (level instanceof ServerLevel serverLevel && ExperienceRepairHelper.canRepairItem(serverLevel, stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getRequiredAmountForItem", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/transfer/GenericItemFilling;getRequiredAmountForItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lcom/zurrtum/create/infrastructure/fluids/FluidStack;)I"), cancellable = true)
    private static void getRequiredAmountForItem$mending(ServerLevel level, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<Integer> cir) {
        if (!ExperienceRepairHelper.canRepairItem(level, stack)) {
            return;
        }
        int availableXp = ExperienceRepairHelper.getExperienceFromFluid(availableFluid);
        if (availableXp == 0) {
            return;
        }
        int requiredXp = ExperienceRepairHelper.repairItem(availableXp, level, stack, true);
        int requiredFluid = ExperienceRepairHelper.getFluidFromExperience(requiredXp);
        if (requiredFluid > 0) {
            cir.setReturnValue(requiredFluid);
        }
    }

    @Inject(method = "fillItem", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/transfer/GenericItemFilling;fillItem(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/item/ItemStack;Lcom/zurrtum/create/infrastructure/fluids/FluidStack;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private static void fillItem$mending(ServerLevel level, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        if (!ExperienceRepairHelper.canRepairItem(level, stack)) {
            return;
        }
        int availableXp = ExperienceRepairHelper.getExperienceFromFluid(availableFluid);
        if (availableXp == 0) {
            return;
        }
        ItemStack result = stack.copy();
        stack.shrink(1);
        ExperienceRepairHelper.repairItem(availableXp, level, result, false);
        availableFluid.decrement(requiredAmount);
        cir.setReturnValue(result);
    }
}
