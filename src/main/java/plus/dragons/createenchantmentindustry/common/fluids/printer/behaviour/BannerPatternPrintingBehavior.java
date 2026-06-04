package plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBehaviour;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public class BannerPatternPrintingBehavior implements PrintingBehaviour {
    private final Holder<BannerPattern> pattern;
    private final DyeColor color;

    private BannerPatternPrintingBehavior(Holder<BannerPattern> pattern, DyeColor color) {
        this.pattern = pattern;
        this.color = color;
    }

    public static Optional<PrintingBehaviour> create(@Nullable Level level, ItemStack template) {
        if (!(template.getItem() instanceof BannerItem)) {
            return Optional.empty();
        }
        BannerPatternLayers layers = template.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (layers.layers().size() != 1) {
            return Optional.empty();
        }
        BannerPatternLayers.Layer layer = layers.layers().getFirst();
        return Optional.of(new BannerPatternPrintingBehavior(layer.pattern(), layer.color()));
    }

    @Override
    public int getRequiredItemCount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        if (!(stack.getItem() instanceof BannerItem)) {
            return 0;
        }
        BannerPatternLayers layers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (!layers.layers().isEmpty() && layers.layers().getLast().pattern().equals(pattern)) {
            return 0;
        }
        return 1;
    }

    @Override
    public long getRequiredFluidAmount(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        return PrinterBehaviour.DEFAULT_SPECIAL_FLUID_COST;
    }

    @Override
    public ItemStack getResult(@Nullable Level level, ItemStack stack, CEIConfigurableFluidTank tank) {
        BannerPatternLayers layers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        List<BannerPatternLayers.Layer> resultLayers = new ArrayList<>(layers.layers());
        resultLayers.add(new BannerPatternLayers.Layer(pattern, color));
        ItemStack result = stack.copyWithCount(1);
        result.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(resultLayers));
        return result;
    }
}
