package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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

public class BlazeEnchanterBlockEntity extends CEIBlazeBlockEntity implements Clearable {
    public static final long TANK_CAPACITY = CEIConfig.fluids().blazeEnchanterFluidCapacity();
    public static final int PROCESSING_TICKS = 100;
    public static final int EXPERIENCE_BOTTLE_AMOUNT = 7;

    private CEIFluidTankBehaviour fluidTankBehaviour;
    private EnchanterBehaviour enchanter;
    private ItemStack heldItem = ItemStack.EMPTY;
    private int processingTicks = -1;
    private long randomSeed = 0xC317_2026L;

    public BlazeEnchanterBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.BLAZE_ENCHANTER, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(fluidTankBehaviour = CEIFluidTankBehaviour.singleLiquidExperience(this, TANK_CAPACITY));
        behaviours.add(enchanter = new EnchanterBehaviour(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || heldItem.isEmpty() || processingTicks < 0) {
            return;
        }
        if (processingTicks > 0) {
            processingTicks--;
            return;
        }
        finishProcessing(false);
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

    public EnchanterBehaviour getEnchanter() {
        return enchanter;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public boolean isSpecialEnchanting() {
        return false;
    }

    public int getMaxEnchantLevel() {
        return CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel();
    }

    public RandomSource getRandom() {
        return RandomSource.create(randomSeed++);
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (level == null || !heldItem.isEmpty() || stack.isEmpty()) {
            return stack;
        }
        ItemStack remainder = stack.copy();
        ItemStack inserted = remainder.split(1);
        if (!enchanter.canProcess(inserted)) {
            return stack;
        }
        if (!simulate) {
            heldItem = inserted;
            processingTicks = PROCESSING_TICKS;
            notifyUpdate();
        }
        return remainder;
    }

    public ItemStack extractItem(boolean forced, boolean simulate) {
        if (heldItem.isEmpty() || (!forced && processingTicks > 0)) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = heldItem.copy();
        if (!simulate) {
            heldItem = ItemStack.EMPTY;
            processingTicks = -1;
            notifyUpdate();
        }
        return extracted;
    }

    public boolean finishProcessing(boolean simulate) {
        if (level == null || heldItem.isEmpty()) {
            return false;
        }
        enchanter.update(heldItem);
        if (!enchanter.canProcess(heldItem)) {
            processingTicks = -1;
            return false;
        }
        int cost = enchanter.getExperienceCost();
        if (cost <= 0 || getTank().getAmount() < cost) {
            return false;
        }
        ItemStack result = enchanter.getResult(heldItem, getRandom());
        if (result.isEmpty()) {
            return false;
        }
        if (!simulate) {
            extractExperience(cost);
            heldItem = result;
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
        return fluidTankBehaviour != null && getTank().getAmount() > 0 ? HeatLevel.FADING : HeatLevel.SMOULDERING;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level != null && !heldItem.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), heldItem);
        }
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store("HeldItem", ItemStack.OPTIONAL_CODEC, heldItem);
        view.putInt("ProcessingTicks", processingTicks);
        view.putLong("RandomSeed", randomSeed);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        heldItem = view.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        processingTicks = view.getIntOr("ProcessingTicks", -1);
        randomSeed = view.getLongOr("RandomSeed", 0xC317_2026L);
    }

    @Override
    public void clearContent() {
        heldItem = ItemStack.EMPTY;
    }
}
