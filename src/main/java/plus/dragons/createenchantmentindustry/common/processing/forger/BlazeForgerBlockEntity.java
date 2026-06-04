package plus.dragons.createenchantmentindustry.common.processing.forger;

import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.blaze.CEIBlazeBlockEntity;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;

public class BlazeForgerBlockEntity extends CEIBlazeBlockEntity implements Clearable {
    public static final long TANK_CAPACITY = CEIConfig.fluids().blazeForgerFluidCapacity();
    public static final int FORGING_TIME = 200;
    public static final int EXPERIENCE_BOTTLE_AMOUNT = 7;

    private CEIFluidTankBehaviour fluidTankBehaviour;
    private final BlazeForgerInventory inventory = new BlazeForgerInventory();
    private int processingTicks = -1;

    public BlazeForgerBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.BLAZE_FORGER, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(fluidTankBehaviour = CEIFluidTankBehaviour.singleLiquidExperience(this, TANK_CAPACITY));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide()) {
            return;
        }
        inventory.setSpecial(getHeatLevelFromBlock() == HeatLevel.SEETHING);
        int cost = inventory.getExperienceCost();
        if (cost <= 0 || inventory.hasRemainingOutput() || getTank().getAmount() < cost) {
            if (processingTicks != -1) {
                processingTicks = -1;
                notifyUpdate();
            }
            return;
        }
        if (processingTicks < 0) {
            processingTicks = FORGING_TIME;
            notifyUpdate();
            return;
        }
        if (processingTicks > 0) {
            processingTicks--;
            notifyUpdate();
            return;
        }
        finishProcessing(false);
    }

    public BlazeForgerInventory getInventory() {
        return inventory;
    }

    public CEIFluidTankBehaviour getFluidTankBehaviour() {
        return fluidTankBehaviour;
    }

    public Storage<FluidVariant> getFluidStorage(Direction direction) {
        return fluidTankBehaviour.getStorage();
    }

    public CEIConfigurableFluidTank getTank() {
        return fluidTankBehaviour.getPrimaryHandler();
    }

    public int getProcessingTicks() {
        return processingTicks;
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack remainder = inventory.insertItem(stack, simulate);
        if (!simulate && (!ItemStack.isSameItemSameComponents(stack, remainder) || stack.getCount() != remainder.getCount())) {
            processingTicks = -1;
            notifyUpdate();
        }
        return remainder;
    }

    public ItemStack extractItem(boolean simulate) {
        ItemStack extracted = inventory.extractFirstAvailable(simulate);
        if (!simulate && !extracted.isEmpty()) {
            processingTicks = -1;
            notifyUpdate();
        }
        return extracted;
    }

    public boolean finishProcessing(boolean simulate) {
        int cost = inventory.getExperienceCost();
        if (level == null || cost <= 0 || inventory.hasRemainingOutput() || getTank().getAmount() < cost) {
            return false;
        }
        if (!simulate) {
            extractExperience(cost);
            inventory.applyResult();
            processingTicks = -1;
            notifyUpdate();
        }
        return true;
    }

    public long insertExperience(long experiencePoints) {
        if (experiencePoints <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = fluidTankBehaviour.getStorage().insert(
                    FluidVariant.of(CEIFluids.EXPERIENCE),
                    ExperienceHatchBehaviour.getLiquidFromExperience(experiencePoints),
                    transaction);
            transaction.commit();
            if (inserted > 0) {
                updateBlockState();
            }
            return inserted;
        }
    }

    public long extractExperience(long amount) {
        if (amount <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = fluidTankBehaviour.getStorage().extract(FluidVariant.of(CEIFluids.EXPERIENCE), amount, transaction);
            transaction.commit();
            if (extracted > 0) {
                updateBlockState();
            }
            return extracted;
        }
    }

    @Override
    public InteractionResult tryApplyFuel(ItemStack stack, boolean forceOverflow, boolean doNotConsume, boolean simulate) {
        if (!stack.is(Items.EXPERIENCE_BOTTLE) || getTank().getSpace() < EXPERIENCE_BOTTLE_AMOUNT) {
            return InteractionResult.PASS;
        }
        if (!simulate) {
            insertExperience(EXPERIENCE_BOTTLE_AMOUNT);
            if (!doNotConsume) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS.heldItemTransformedTo(doNotConsume ? ItemStack.EMPTY : new ItemStack(Items.GLASS_BOTTLE));
    }

    @Override
    public HeatLevel getHeatLevel() {
        if (isCreative()) {
            return HeatLevel.SEETHING;
        }
        if (fluidTankBehaviour != null && getTank().getAmount() > 0) {
            return HeatLevel.FADING;
        }
        return HeatLevel.SMOULDERING;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt("ProcessingTicks", processingTicks);
        inventory.write(view.child("Inventory"));
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        processingTicks = view.getIntOr("ProcessingTicks", -1);
        inventory.read(view.childOrEmpty("Inventory"));
    }

    @Override
    public void clearContent() {
        inventory.clear();
        processingTicks = -1;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level == null) {
            return;
        }
        for (int slot = 0; slot < 4; slot++) {
            ItemStack extracted = inventory.extractItem(slot, 64, false);
            if (!extracted.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), extracted);
            }
        }
    }
}
