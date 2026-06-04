package plus.dragons.createenchantmentindustry.common.fluids.printer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.CEI;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;

public class PrinterBlockEntity extends SmartBlockEntity {
    public static final long TANK_CAPACITY = CEIConfig.fluids().printerFluidCapacity();
    private static final String TEMPLATE_KEY = "Template";
    private static final String ACTIVE_INPUT_KEY = "ActiveInput";
    private static final String LAST_OUTPUT_KEY = "LastOutput";
    private static final String PROCESSING_TICKS_KEY = "ProcessingTicks";
    private static final String TANK_COUNT_KEY = "TankCount";
    private static final String TANK_KEY_PREFIX = "Tank";

    private CEIFluidTankBehaviour fluidTankBehaviour;
    private ItemStack template = ItemStack.EMPTY;
    private ItemStack activeInput = ItemStack.EMPTY;
    private ItemStack lastOutput = ItemStack.EMPTY;
    private int processingTicks = -1;

    record SmokeSerializedState(
            ItemStack template,
            ItemStack activeInput,
            ItemStack lastOutput,
            int processingTicks,
            long fluidAmount) {
    }

    public PrinterBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.PRINTER, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(fluidTankBehaviour = CEIFluidTankBehaviour.singleLiquidExperience(this, TANK_CAPACITY));
        behaviours.add(new BeltProcessingBehaviour(this)
                .whenItemEnters(this::onItemEnters)
                .whileItemHeld(this::onItemHeld));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || processingTicks <= 0) {
            return;
        }
        processingTicks--;
        if (processingTicks == 0 && !activeInput.isEmpty()) {
            lastOutput = applyPrintingBehaviour(level, activeInput, template, getTank(), false);
            activeInput = ItemStack.EMPTY;
            processingTicks = -1;
            setChanged();
        }
    }

    public CEIFluidTankBehaviour getFluidTankBehaviour() {
        return fluidTankBehaviour;
    }

    public CEIConfigurableFluidTank getTank() {
        return fluidTankBehaviour.getPrimaryHandler();
    }

    @Nullable
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction direction) {
        return direction == Direction.DOWN ? null : fluidTankBehaviour.getStorage();
    }

    public ItemStack getTemplate() {
        return template.copy();
    }

    public void setTemplate(ItemStack template) {
        this.template = template.copyWithCount(1);
        setChanged();
    }

    public ItemStack getActiveInput() {
        return activeInput.copy();
    }

    public ItemStack getLastOutput() {
        return lastOutput.copy();
    }

    public int getProcessingTicks() {
        return processingTicks;
    }

    public boolean beginPrinting(ItemStack stack) {
        if (processingTicks != -1 || !activeInput.isEmpty()) {
            return false;
        }
        Optional<PrinterBehaviour> behaviour = findPrintingBehaviour(stack);
        if (behaviour.isEmpty()) {
            return false;
        }
        activeInput = stack.copyWithCount(behaviour.get().getRequiredItemCount(level, stack, getTank()));
        processingTicks = behaviour.get().getProcessingTime(level, stack, getTank());
        setChanged();
        return true;
    }

    public ItemStack tryPrint(ItemStack stack, boolean simulate) {
        return applyPrintingBehaviour(level, stack, template, getTank(), simulate);
    }

    private ProcessingResult onItemEnters(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        if (findPrintingBehaviour(transported.stack).isEmpty()) {
            return ProcessingResult.PASS;
        }
        return ProcessingResult.HOLD;
    }

    private ProcessingResult onItemHeld(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
        Optional<PrinterBehaviour> behaviour = findPrintingBehaviour(transported.stack);
        if (behaviour.isEmpty()) {
            processingTicks = -1;
            return ProcessingResult.PASS;
        }
        if (processingTicks == -1) {
            processingTicks = behaviour.get().getProcessingTime(level, transported.stack, getTank());
            setChanged();
            return ProcessingResult.HOLD;
        }
        if (processingTicks > 5) {
            return ProcessingResult.HOLD;
        }

        ItemStack before = transported.stack.copy();
        ItemStack output = applyPrintingBehaviour(level, transported.stack, template, getTank(), false);
        processingTicks = -1;
        if (output.isEmpty()) {
            transported.stack = before;
            return ProcessingResult.PASS;
        }

        TransportedItemStack result = transported.copy();
        result.stack = output;
        TransportedItemStack held = transported.stack.isEmpty() ? null : transported.copy();
        List<TransportedItemStack> outputs = new ArrayList<>();
        outputs.add(result);
        handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(outputs, held));
        return ProcessingResult.HOLD;
    }

    private Optional<PrinterBehaviour> findPrintingBehaviour(ItemStack base) {
        return PrinterBehaviour.create(level, template)
                .filter(behaviour -> behaviour.canPrint(level, base, getTank()));
    }

    public PrintingInput createInput(ItemStack base) {
        return PrintingInput.fromTank(base, template, getTank());
    }

    public ItemStack applyRecipe(PrintingRecipe recipe, ItemStack base, boolean simulate) {
        return applyRecipe(recipe, base, template, getTank(), simulate);
    }

    public static ItemStack applyRecipe(
            PrintingRecipe recipe,
            ItemStack base,
            ItemStack template,
            CEIConfigurableFluidTank tank,
            boolean simulate) {
        FluidStack fluid = tank.isEmpty()
                ? FluidStack.EMPTY
                : new FluidStack(tank.getFluidVariant().getFluid(), Math.toIntExact(tank.getAmount()));
        PrintingInput input = new PrintingInput(base, template, fluid);
        if (!recipe.matches(input)) {
            return ItemStack.EMPTY;
        }

        long requiredFluid = recipe.fluidIngredient().amount();
        if (tank.getAmount() < requiredFluid) {
            return ItemStack.EMPTY;
        }

        ItemStack output = recipe.assemble(input);
        if (simulate) {
            return output;
        }

        FluidVariant variant = tank.getFluidVariant();
        long extracted;
        try (Transaction transaction = Transaction.openOuter()) {
            extracted = tank.extract(variant, requiredFluid, transaction);
            if (extracted == requiredFluid) {
                transaction.commit();
            }
        }
        if (extracted != requiredFluid) {
            return ItemStack.EMPTY;
        }
        base.shrink(recipe.baseCount());
        return output;
    }

    public static ItemStack applyPrintingBehaviour(
            @Nullable Level level,
            ItemStack base,
            ItemStack template,
            CEIConfigurableFluidTank tank,
            boolean simulate) {
        Optional<PrinterBehaviour> optional = PrinterBehaviour.create(level, template);
        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PrinterBehaviour behaviour = optional.get();
        int requiredItems = behaviour.getRequiredItemCount(level, base, tank);
        long requiredFluid = behaviour.getRequiredFluidAmount(level, base, tank);
        if (requiredItems <= 0 || requiredFluid <= 0 || base.getCount() < requiredItems || tank.getAmount() < requiredFluid) {
            return ItemStack.EMPTY;
        }

        ItemStack output = behaviour.getResult(level, base, tank);
        if (output.isEmpty() || simulate) {
            return output;
        }

        FluidVariant variant = tank.getFluidVariant();
        long extracted;
        try (Transaction transaction = Transaction.openOuter()) {
            extracted = tank.extract(variant, requiredFluid, transaction);
            if (extracted == requiredFluid) {
                transaction.commit();
            }
        }
        if (extracted != requiredFluid) {
            return ItemStack.EMPTY;
        }
        base.shrink(requiredItems);
        return output;
    }

    static CompoundTag writeSmokeState(
            ItemStack template,
            ItemStack activeInput,
            ItemStack lastOutput,
            int processingTicks,
            CEIConfigurableFluidTank tank) {
        try (ProblemReporter.ScopedCollector problems = new ProblemReporter.ScopedCollector(CEI.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(problems, smokeRegistryAccess());
            view.putInt(TANK_COUNT_KEY, 1);
            tank.write(view.child(TANK_KEY_PREFIX + 0));
            view.store(TEMPLATE_KEY, ItemStack.OPTIONAL_CODEC, template);
            view.store(ACTIVE_INPUT_KEY, ItemStack.OPTIONAL_CODEC, activeInput);
            view.store(LAST_OUTPUT_KEY, ItemStack.OPTIONAL_CODEC, lastOutput);
            view.putInt(PROCESSING_TICKS_KEY, processingTicks);
            return view.buildResult();
        }
    }

    static SmokeSerializedState readSmokeState(CompoundTag tag, CEIConfigurableFluidTank tank) {
        try (ProblemReporter.ScopedCollector problems = new ProblemReporter.ScopedCollector(CEI.LOGGER)) {
            ValueInput view = TagValueInput.create(problems, smokeRegistryAccess(), tag);
            if (view.getIntOr(TANK_COUNT_KEY, 0) > 0) {
                tank.read(view.childOrEmpty(TANK_KEY_PREFIX + 0));
            }
            return new SmokeSerializedState(
                    view.read(TEMPLATE_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY),
                    view.read(ACTIVE_INPUT_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY),
                    view.read(LAST_OUTPUT_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY),
                    view.getIntOr(PROCESSING_TICKS_KEY, -1),
                    tank.getAmount());
        }
    }

    private static RegistryAccess.Frozen smokeRegistryAccess() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store(TEMPLATE_KEY, ItemStack.OPTIONAL_CODEC, template);
        view.store(ACTIVE_INPUT_KEY, ItemStack.OPTIONAL_CODEC, activeInput);
        view.store(LAST_OUTPUT_KEY, ItemStack.OPTIONAL_CODEC, lastOutput);
        view.putInt(PROCESSING_TICKS_KEY, processingTicks);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        template = view.read(TEMPLATE_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        activeInput = view.read(ACTIVE_INPUT_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        lastOutput = view.read(LAST_OUTPUT_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        processingTicks = view.getIntOr(PROCESSING_TICKS_KEY, -1);
    }
}
