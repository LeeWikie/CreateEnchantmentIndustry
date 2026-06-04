package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIBlockEntityTypes;
import plus.dragons.createenchantmentindustry.registry.CEIRecipeTypes;

public class GrindstoneDrainBlockEntity extends KineticBlockEntity implements Clearable {
    public static final long TANK_CAPACITY = CEIConfig.fluids().mechanicalGrindstoneFluidCapacity();
    private static final int OUTPUT_SLOTS = 4;

    private CEIFluidTankBehaviour fluidTankBehaviour;
    private ItemStack input = ItemStack.EMPTY;
    private final List<ItemStack> outputs = new ArrayList<>();
    private int processingTicks = -1;

    public GrindstoneDrainBlockEntity(BlockPos pos, BlockState state) {
        super(CEIBlockEntityTypes.GRINDSTONE_DRAIN, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(fluidTankBehaviour = CEIFluidTankBehaviour.singleLiquidExperience(this, TANK_CAPACITY));
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

    public ItemStack getInput() {
        return input.copy();
    }

    public List<ItemStack> getOutputs() {
        return GrindstoneHelper.copyOutputs(outputs);
    }

    public int getProcessingTicks() {
        return processingTicks;
    }

    public boolean tryInsert(ItemStack stack, boolean simulate) {
        if (!input.isEmpty() || stack.isEmpty() || level == null) {
            return false;
        }
        ItemStack single = stack.copyWithCount(1);
        if (findRecipe(single).isEmpty()) {
            return false;
        }
        if (!simulate) {
            input = single;
            processingTicks = -1;
            setChanged();
            sendData();
        }
        return true;
    }

    public ItemStack extractFirstItem(boolean simulate) {
        if (!input.isEmpty()) {
            ItemStack extracted = input.copy();
            if (!simulate) {
                input = ItemStack.EMPTY;
                processingTicks = -1;
                setChanged();
                sendData();
            }
            return extracted;
        }
        if (outputs.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = outputs.getFirst().copy();
        if (!simulate) {
            outputs.removeFirst();
            setChanged();
            sendData();
        }
        return extracted;
    }

    public float getRelativeSpeed() {
        if (level == null || getSpeed() == 0f) {
            return 0f;
        }
        BlockPos above = worldPosition.above();
        BlockState aboveState = level.getBlockState(above);
        if (!(aboveState.getBlock() instanceof MechanicalGrindstoneBlock grinderWheel)) {
            return 0f;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        if (grinderWheel.getRotationAxis(aboveState) != facing.getAxis()) {
            return 0f;
        }
        float wheelSpeed = grinderWheel.getBlockEntityOptional(level, above)
                .map(KineticBlockEntity::getSpeed)
                .orElse(0f);
        return pairedProcessingSpeed(getSpeed(), wheelSpeed);
    }

    public static float pairedProcessingSpeed(float drainSpeed, float wheelSpeed) {
        if (drainSpeed > 0f && wheelSpeed < 0f) {
            return Math.min(drainSpeed, -wheelSpeed);
        }
        if (drainSpeed < 0f && wheelSpeed > 0f) {
            return Math.min(-drainSpeed, wheelSpeed);
        }
        return 0f;
    }

    public static GrindstoneHelper.GrindingResult processForSmoke(
            GrindingRecipe recipe,
            ItemStack input,
            CEIConfigurableFluidTank tank,
            float drainSpeed,
            float wheelSpeed) {
        if (pairedProcessingSpeed(drainSpeed, wheelSpeed) < 1f) {
            return GrindstoneHelper.GrindingResult.failed(input);
        }
        return GrindstoneHelper.applyRecipe(recipe, input, tank, net.minecraft.util.RandomSource.create(13), false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide() || input.isEmpty()) {
            return;
        }
        float speed = getRelativeSpeed();
        if (speed < 1f) {
            return;
        }
        Optional<RecipeHolder<GrindingRecipe>> recipe = findRecipe(input);
        if (recipe.isEmpty()) {
            return;
        }
        if (processingTicks == -1) {
            processingTicks = recipe.get().value().getProcessingDuration();
            setChanged();
            sendData();
            return;
        }
        processingTicks -= Math.max(1, (int) Math.min(128, speed / 24f));
        if (processingTicks > 0) {
            return;
        }
        GrindstoneHelper.GrindingResult result = GrindstoneHelper.applyRecipe(recipe.get().value(), input, getTank(), level.getRandom(), false);
        if (result.success()) {
            input = ItemStack.EMPTY;
            outputs.clear();
            outputs.addAll(GrindstoneHelper.copyOutputs(result.outputs()));
            ejectOutputs();
        }
        processingTicks = -1;
        setChanged();
        sendData();
    }

    private Optional<RecipeHolder<GrindingRecipe>> findRecipe(ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        for (RecipeHolder<?> holder : serverLevel.recipeAccess().getRecipes()) {
            if (holder.value() instanceof GrindingRecipe recipe && recipe.matches(input, level)) {
                return Optional.of(new RecipeHolder<>(holder.id(), recipe));
            }
        }
        return Optional.empty();
    }

    private Direction getOutputSide() {
        Direction facing = getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING);
        float signedSpeed = facing == Direction.WEST || facing == Direction.NORTH ? getSpeed() * -1 : getSpeed();
        return signedSpeed > 0 ? facing.getClockWise() : facing.getCounterClockWise();
    }

    private void ejectOutputs() {
        if (level == null || outputs.isEmpty()) {
            return;
        }
        Direction outputSide = getOutputSide();
        Vec3 itemMovement = new Vec3(outputSide.getStepX(), outputSide.getStepY(), outputSide.getStepZ());
        Vec3 outPos = Vec3.atCenterOf(worldPosition).add(itemMovement.scale(0.55)).add(0, 0.25, 0);
        Vec3 outMotion = itemMovement.scale(0.0625).add(0, 0.125, 0);
        for (ItemStack output : outputs) {
            if (output.isEmpty()) {
                continue;
            }
            ItemEntity entity = new ItemEntity(level, outPos.x, outPos.y, outPos.z, output.copy());
            entity.setDeltaMovement(outMotion);
            level.addFreshEntity(entity);
        }
        outputs.clear();
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.store("Input", ItemStack.OPTIONAL_CODEC, input);
        view.putInt("ProcessingTicks", processingTicks);
        view.putInt("OutputCount", Math.min(outputs.size(), OUTPUT_SLOTS));
        for (int slot = 0; slot < Math.min(outputs.size(), OUTPUT_SLOTS); slot++) {
            view.store("Output" + slot, ItemStack.OPTIONAL_CODEC, outputs.get(slot));
        }
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        input = view.read("Input", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        processingTicks = view.getIntOr("ProcessingTicks", -1);
        outputs.clear();
        int count = Math.min(view.getIntOr("OutputCount", 0), OUTPUT_SLOTS);
        for (int slot = 0; slot < count; slot++) {
            ItemStack output = view.read("Output" + slot, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            if (!output.isEmpty()) {
                outputs.add(output);
            }
        }
    }

    @Override
    public void clearContent() {
        input = ItemStack.EMPTY;
        outputs.clear();
        processingTicks = -1;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (level == null) {
            return;
        }
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), input);
        for (ItemStack output : outputs) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), output);
        }
        clearContent();
    }
}
