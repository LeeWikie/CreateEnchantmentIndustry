package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public class EnchantedBookPrintingBehaviour implements PrintingBehaviour {
    private final ItemEnchantments enchantments;
    private final long cost;

    private EnchantedBookPrintingBehaviour(ItemEnchantments enchantments) {
        this.enchantments = enchantments;
        long baseCost = Math.max(PrinterBehaviour.DEFAULT_SPECIAL_FLUID_COST, enchantments.size() * PrinterBehaviour.DEFAULT_SPECIAL_FLUID_COST);
        this.cost = Math.max(1L, Math.round(baseCost * (double) CEIConfig.fluids().printingEnchantedBookCostMultiplier()));
    }

    public static Optional<PrintingBehaviour> create(@Nullable Level level, ItemStack template) {
        if (!template.is(Items.ENCHANTED_BOOK)) {
            return Optional.empty();
        }
        ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(template);
        return enchantments.isEmpty() ? Optional.empty() : Optional.of(new EnchantedBookPrintingBehaviour(enchantments));
    }

    @Override
    public int getRequiredItemCount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return stack.is(Items.BOOK) ? 1 : 0;
    }

    @Override
    public long getRequiredFluidAmount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return cost;
    }

    @Override
    public ItemStack getResult(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        ItemStack result = stack.transmuteCopy(Items.ENCHANTED_BOOK, 1);
        result.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
        return result;
    }
}
