package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.Optional;

import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public class CopyPrintingBehaviour implements PrintingBehaviour {
    private final SupportsItemCopying itemCopying;
    private final ItemStack original;

    private CopyPrintingBehaviour(SupportsItemCopying itemCopying, ItemStack original) {
        this.itemCopying = itemCopying;
        this.original = original.copyWithCount(1);
    }

    public static Optional<PrintingBehaviour> create(@Nullable Level level, ItemStack template) {
        if (template.getItem() instanceof SupportsItemCopying itemCopying && itemCopying.canCopyFromItem(template)) {
            return Optional.of(new CopyPrintingBehaviour(itemCopying, template));
        }
        if (template.has(DataComponents.CUSTOM_DATA)) {
            return Optional.of(new CopyPrintingBehaviour(null, template));
        }
        return Optional.empty();
    }

    @Override
    public int getRequiredItemCount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        if (!ItemStack.isSameItem(original, stack)) {
            return 0;
        }
        if (itemCopying != null) {
            return itemCopying.canCopyToItem(stack) ? 1 : 0;
        }
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).equals(CustomData.EMPTY) ? 1 : 0;
    }

    @Override
    public long getRequiredFluidAmount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return PrinterBehaviour.DEFAULT_SPECIAL_FLUID_COST;
    }

    @Override
    public ItemStack getResult(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        ItemStack copy = itemCopying == null ? original.copyWithCount(1) : itemCopying.createCopy(original, 1);
        copy.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        copy.remove(DataComponents.STORED_ENCHANTMENTS);
        return copy;
    }
}
