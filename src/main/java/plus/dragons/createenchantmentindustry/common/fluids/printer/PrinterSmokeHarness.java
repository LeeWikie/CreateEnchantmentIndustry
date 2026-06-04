package plus.dragons.createenchantmentindustry.common.fluids.printer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.zurrtum.create.foundation.fluid.FluidStackIngredient;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;

public final class PrinterSmokeHarness {
    private PrinterSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: PrinterSmokeHarness <valid|invalid|save-load|copy|custom-name|banner|enchantment> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        bindItemComponents(
                Items.BOOK,
                Items.PAPER,
                Items.STICK,
                Items.WRITABLE_BOOK,
                Items.NAME_TAG,
                Items.DIAMOND,
                Items.WHITE_BANNER,
                Items.ENCHANTED_BOOK);
        List<String> lines = switch (args[0]) {
            case "valid" -> validSmoke();
            case "invalid" -> invalidSmoke();
            case "save-load" -> saveLoadSmoke();
            case "copy" -> copySmoke();
            case "custom-name" -> customNameSmoke();
            case "banner" -> bannerSmoke();
            case "enchantment" -> enchantmentSmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Files.write(Path.of(args[1]), lines);
    }

    private static List<String> validSmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 500);
        PrintingRecipe recipe = smokeRecipe(smokeFluid, 250);
        ItemStack base = new ItemStack(Items.BOOK, 2);
        ItemStack template = new ItemStack(Items.PAPER);
        ItemStack output = PrinterBlockEntity.applyRecipe(recipe, base, template, tank, false);

        List<String> lines = new ArrayList<>();
        lines.add("mode=valid");
        lines.add("output=" + output.getItem());
        lines.add("output_count=" + output.getCount());
        lines.add("remaining_base=" + base.getCount());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("valid_pass=" + (output.is(Items.WRITABLE_BOOK) && base.getCount() == 1 && tank.getAmount() == 250));
        return lines;
    }

    private static List<String> invalidSmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 500);
        PrintingRecipe recipe = smokeRecipe(smokeFluid, 250);
        ItemStack base = new ItemStack(Items.STICK, 2);
        ItemStack template = new ItemStack(Items.PAPER);
        ItemStack output = PrinterBlockEntity.applyRecipe(recipe, base, template, tank, false);

        List<String> lines = new ArrayList<>();
        lines.add("mode=invalid");
        lines.add("output_empty=" + output.isEmpty());
        lines.add("remaining_base=" + base.getCount());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("invalid_no_consume=" + (output.isEmpty() && base.getCount() == 2 && tank.getAmount() == 500));
        return lines;
    }

    private static List<String> saveLoadSmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 750);
        ItemStack activeInput = new ItemStack(Items.BOOK);
        ItemStack activeTemplate = new ItemStack(Items.PAPER);
        ItemStack lastOutput = new ItemStack(Items.WRITABLE_BOOK);
        Component lastOutputName = Component.literal("Saved Output");
        lastOutput.set(DataComponents.CUSTOM_NAME, lastOutputName);
        int activeTicks = 25;
        CompoundTag tag = PrinterBlockEntity.writeSmokeState(activeTemplate, activeInput, lastOutput, activeTicks, tank);
        CEIConfigurableFluidTank restored = smokeTank(smokeFluid, 1000);
        PrinterBlockEntity.SmokeSerializedState restoredState = PrinterBlockEntity.readSmokeState(tag, restored);
        long restoredBeforePrint = restoredState.fluidAmount();
        PrintingRecipe recipe = smokeRecipe(smokeFluid, 250);
        ItemStack resumedInput = restoredState.activeInput().copy();
        ItemStack output = PrinterBlockEntity.applyRecipe(recipe, resumedInput, restoredState.template(), restored, false);
        Component restoredLastOutputName = restoredState.lastOutput().get(DataComponents.CUSTOM_NAME);
        boolean stateRestored = restoredState.template().is(Items.PAPER)
                && restoredState.activeInput().is(Items.BOOK)
                && restoredState.lastOutput().is(Items.WRITABLE_BOOK)
                && lastOutputName.equals(restoredLastOutputName)
                && restoredState.processingTicks() == activeTicks
                && restoredBeforePrint == 750;

        List<String> lines = new ArrayList<>();
        lines.add("mode=save-load");
        lines.add("restored_template=" + restoredState.template().getItem());
        lines.add("restored_active_input=" + restoredState.activeInput().getItem());
        lines.add("restored_last_output=" + restoredState.lastOutput().getItem());
        lines.add("restored_last_output_name=" + (restoredLastOutputName == null ? "" : restoredLastOutputName.getString()));
        lines.add("restored_processing_ticks=" + restoredState.processingTicks());
        lines.add("restored_fluid_before_resume=" + restoredBeforePrint);
        lines.add("output=" + output.getItem());
        lines.add("remaining_base=" + resumedInput.getCount());
        lines.add("remaining_fluid=" + restored.getAmount());
        lines.add("save_load_pass=" + (stateRestored && output.is(Items.WRITABLE_BOOK) && resumedInput.isEmpty() && restored.getAmount() == 500));
        return lines;
    }

    private static List<String> copySmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 500);
        ItemStack template = new ItemStack(Items.PAPER);
        CompoundTag copyTag = new CompoundTag();
        copyTag.putString("printer_copy", "source");
        template.set(DataComponents.CUSTOM_DATA, CustomData.of(copyTag));
        ItemStack base = new ItemStack(Items.PAPER, 2);
        ItemStack output = PrinterBlockEntity.applyPrintingBehaviour(null, base, template, tank, false);
        CustomData outputData = output.get(DataComponents.CUSTOM_DATA);
        String copiedValue = outputData == null ? "" : outputData.copyTag().getStringOr("printer_copy", "");

        List<String> lines = new ArrayList<>();
        lines.add("mode=copy");
        lines.add("output=" + output.getItem());
        lines.add("copied_custom_data=" + copiedValue);
        lines.add("remaining_base=" + base.getCount());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("copy_pass=" + (output.is(Items.PAPER) && "source".equals(copiedValue) && base.getCount() == 1 && tank.getAmount() == 250));
        return lines;
    }

    private static List<String> customNameSmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 500);
        Component name = Component.literal("Atlas Printer");
        ItemStack template = new ItemStack(Items.NAME_TAG);
        template.set(DataComponents.CUSTOM_NAME, name);
        ItemStack base = new ItemStack(Items.DIAMOND, 2);
        ItemStack output = PrinterBlockEntity.applyPrintingBehaviour(null, base, template, tank, false);
        Component outputName = output.get(DataComponents.CUSTOM_NAME);

        List<String> lines = new ArrayList<>();
        lines.add("mode=custom-name");
        lines.add("output=" + output.getItem());
        lines.add("output_custom_name=" + (outputName == null ? "" : outputName.getString()));
        lines.add("remaining_base=" + base.getCount());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("custom_name_pass=" + (output.is(Items.DIAMOND) && name.equals(outputName) && base.getCount() == 1 && tank.getAmount() == 250));
        return lines;
    }

    private static List<String> bannerSmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 500);
        Holder<BannerPattern> pattern = Holder.direct(new BannerPattern(Identifier.fromNamespaceAndPath("minecraft", "smoke_stripe"), "smoke_stripe"));
        ItemStack template = new ItemStack(Items.WHITE_BANNER);
        template.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(List.of(new BannerPatternLayers.Layer(pattern, DyeColor.BLACK))));
        ItemStack base = new ItemStack(Items.WHITE_BANNER, 2);
        ItemStack output = PrinterBlockEntity.applyPrintingBehaviour(null, base, template, tank, false);
        BannerPatternLayers layers = output.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);

        List<String> lines = new ArrayList<>();
        lines.add("mode=banner");
        lines.add("output=" + output.getItem());
        lines.add("output_layers=" + layers.layers().size());
        lines.add("remaining_base=" + base.getCount());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("banner_pass=" + (output.is(Items.WHITE_BANNER) && layers.layers().size() == 1 && layers.layers().getFirst().pattern().equals(pattern) && base.getCount() == 1 && tank.getAmount() == 250));
        return lines;
    }

    private static List<String> enchantmentSmoke() {
        Fluid smokeFluid = Fluids.WATER;
        CEIConfigurableFluidTank tank = fillTank(smokeFluid, 500);
        ItemEnchantments enchantments = smokeEnchantments();
        ItemStack template = new ItemStack(Items.ENCHANTED_BOOK);
        template.set(DataComponents.STORED_ENCHANTMENTS, enchantments);
        ItemStack base = new ItemStack(Items.BOOK, 2);
        ItemStack output = PrinterBlockEntity.applyPrintingBehaviour(null, base, template, tank, false);
        ItemEnchantments outputEnchantments = output.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        List<String> lines = new ArrayList<>();
        lines.add("mode=enchantment");
        lines.add("output=" + output.getItem());
        lines.add("output_enchantment_count=" + outputEnchantments.size());
        lines.add("remaining_base=" + base.getCount());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("enchantment_pass=" + (output.is(Items.ENCHANTED_BOOK) && !outputEnchantments.isEmpty() && base.getCount() == 1 && tank.getAmount() == 250));
        return lines;
    }

    private static ItemEnchantments smokeEnchantments() {
        HolderSet<Item> supportedItems = HolderSet.direct(Items.BOOK.builtInRegistryHolder());
        Enchantment enchantment = new Enchantment(
                Component.literal("Smoke Enchantment"),
                Enchantment.definition(
                        supportedItems,
                        1,
                        1,
                        Enchantment.constantCost(1),
                        Enchantment.constantCost(1),
                        1,
                        EquipmentSlotGroup.ANY),
                HolderSet.empty(),
                DataComponentMap.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(Holder.direct(enchantment), 1);
        return mutable.toImmutable();
    }

    private static CEIConfigurableFluidTank fillTank(Fluid fluid, long amount) {
        CEIConfigurableFluidTank tank = smokeTank(fluid, 1000);
        try (Transaction transaction = Transaction.openOuter()) {
            tank.insert(new SmokeFluidVariant(fluid), amount, transaction);
            transaction.commit();
        }
        return tank;
    }

    private static CEIConfigurableFluidTank smokeTank(Fluid fluid, long capacity) {
        return new CEIConfigurableFluidTank(capacity, variant -> variant.isOf(fluid), variant -> variant.isOf(fluid), () -> { }) {
            @Override
            protected FluidVariant createVariant(Fluid fluid) {
                return new SmokeFluidVariant(fluid);
            }

            @Override
            protected FluidVariant createBlankVariant() {
                return new SmokeFluidVariant(Fluids.EMPTY);
            }
        };
    }

    private static PrintingRecipe smokeRecipe(Fluid fluid, int amount) {
        return new PrintingRecipe(
                PrintingRecipe.DEFAULT_PROCESSING_TIME,
                1,
                new ItemStackTemplate(Items.WRITABLE_BOOK.builtInRegistryHolder(), 1, DataComponentPatch.EMPTY),
                Ingredient.of(Items.BOOK),
                Ingredient.of(Items.PAPER),
                new FluidStackIngredient(fluid, DataComponentPatch.EMPTY, amount));
    }

    private static void bindItemComponents(Item... items) {
        for (Item item : items) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            }
        }
    }

    private record SmokeFluidVariant(Fluid fluid) implements FluidVariant {
        @Override
        public boolean isBlank() {
            return fluid == Fluids.EMPTY;
        }

        @Override
        public Fluid getObject() {
            return fluid;
        }

        @Override
        public DataComponentPatch getComponentsPatch() {
            return DataComponentPatch.EMPTY;
        }

        @Override
        public DataComponentMap getComponents() {
            return DataComponentMap.EMPTY;
        }

        @Override
        public FluidVariant withComponents(DataComponentPatch changes) {
            if (!changes.isEmpty()) {
                throw new IllegalArgumentException("Smoke fluid variant does not support components");
            }
            return this;
        }
    }
}
