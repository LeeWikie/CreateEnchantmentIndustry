package plus.dragons.createenchantmentindustry.common.fluids.printer;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.BannerPatternPrintingBehavior;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CopyPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.CustomNamePrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.EnchantedBookPrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.RecipePrintingBehaviour;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public class PrinterBehaviour {
    public static final int DEFAULT_PROCESSING_TIME = PrintingRecipe.DEFAULT_PROCESSING_TIME;
    public static final long DEFAULT_SPECIAL_FLUID_COST = 250;

    private final ItemStack template;
    private final PrintingBehaviour printing;

    private PrinterBehaviour(ItemStack template, PrintingBehaviour printing) {
        this.template = template.copyWithCount(1);
        this.printing = printing;
    }

    public static Optional<PrinterBehaviour> create(@Nullable Level level, ItemStack template) {
        if (template.isEmpty()) {
            return Optional.empty();
        }
        Optional<PrinterBehaviour> configuredBehaviour = createConfiguredBehaviour(level, template);
        if (configuredBehaviour.isPresent()) {
            return configuredBehaviour;
        }
        if (level instanceof ServerLevel) {
            return Optional.of(new PrinterBehaviour(template, new RecipePrintingBehaviour(template)));
        }
        return Optional.empty();
    }

    private static Optional<PrinterBehaviour> createConfiguredBehaviour(@Nullable Level level, ItemStack template) {
        if (CEIConfig.fluids().enableCustomNamePrinting()) {
            Optional<PrinterBehaviour> behaviour = createFromProvider(CustomNamePrintingBehaviour::create, level, template);
            if (behaviour.isPresent()) {
                return behaviour;
            }
        }
        if (CEIConfig.fluids().enableBannerPatternPrinting()) {
            Optional<PrinterBehaviour> behaviour = createFromProvider(BannerPatternPrintingBehavior::create, level, template);
            if (behaviour.isPresent()) {
                return behaviour;
            }
        }
        if (CEIConfig.fluids().enableEnchantedBookPrinting()) {
            Optional<PrinterBehaviour> behaviour = createFromProvider(EnchantedBookPrintingBehaviour::create, level, template);
            if (behaviour.isPresent()) {
                return behaviour;
            }
        }
        if (CEIConfig.fluids().enableCreateCopiableItemPrinting()) {
            return createFromProvider(CopyPrintingBehaviour::create, level, template);
        }
        return Optional.empty();
    }

    private static Optional<PrinterBehaviour> createFromProvider(PrintingBehaviour.Provider provider, @Nullable Level level, ItemStack template) {
        return provider.create(level, template).map(printing -> new PrinterBehaviour(template, printing));
    }

    public ItemStack template() {
        return template.copy();
    }

    public PrintingBehaviour printing() {
        return printing;
    }

    public boolean canPrint(@Nullable Level level, ItemStack base, CEIConfigurableFluidTank tank) {
        return getRequiredItemCount(level, base, tank) > 0
                && getRequiredFluidAmount(level, base, tank) > 0
                && tank.getAmount() >= getRequiredFluidAmount(level, base, tank)
                && !getResult(level, base, tank).isEmpty();
    }

    public int getRequiredItemCount(@Nullable Level level, ItemStack base, CEIConfigurableFluidTank tank) {
        int count = printing.getRequiredItemCount(level, base, tank);
        return base.getCount() >= count ? count : 0;
    }

    public long getRequiredFluidAmount(@Nullable Level level, ItemStack base, CEIConfigurableFluidTank tank) {
        return printing.getRequiredFluidAmount(level, base, tank);
    }

    public int getProcessingTime(@Nullable Level level, ItemStack base, CEIConfigurableFluidTank tank) {
        int time = printing.getProcessingTime(level, base, tank);
        return time > 0 ? time : DEFAULT_PROCESSING_TIME;
    }

    public ItemStack getResult(@Nullable Level level, ItemStack base, CEIConfigurableFluidTank tank) {
        return printing.getResult(level, base, tank);
    }

    public void onFinished(@Nullable Level level, PrinterBlockEntity printer) {
        printing.onFinished(level, printer);
    }
}
