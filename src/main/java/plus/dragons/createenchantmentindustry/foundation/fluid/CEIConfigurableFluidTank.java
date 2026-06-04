package plus.dragons.createenchantmentindustry.foundation.fluid;

import java.util.Objects;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;

public class CEIConfigurableFluidTank extends SnapshotParticipant<CEIConfigurableFluidTank.Snapshot>
        implements SingleSlotStorage<FluidVariant> {
    private static final String FLUID_KEY = "Fluid";
    private static final String AMOUNT_KEY = "Amount";

    private final long capacity;
    private final Predicate<FluidVariant> insertPredicate;
    private final Predicate<FluidVariant> extractPredicate;
    private final Runnable updateCallback;
    private FluidVariant variant;
    private long amount;
    private long version;

    public CEIConfigurableFluidTank(long capacity) {
        this(capacity, variant -> true, variant -> true, () -> { });
    }

    public CEIConfigurableFluidTank(long capacity, Runnable updateCallback) {
        this(capacity, variant -> true, variant -> true, updateCallback);
    }

    public CEIConfigurableFluidTank(
            long capacity,
            Predicate<FluidVariant> insertPredicate,
            Predicate<FluidVariant> extractPredicate,
            Runnable updateCallback) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Tank capacity must be positive");
        }
        this.capacity = capacity;
        this.insertPredicate = Objects.requireNonNull(insertPredicate, "insertPredicate");
        this.extractPredicate = Objects.requireNonNull(extractPredicate, "extractPredicate");
        this.updateCallback = Objects.requireNonNull(updateCallback, "updateCallback");
    }

    public static CEIConfigurableFluidTank forFluid(Fluid fluid, long capacity) {
        return forFluid(fluid, capacity, () -> { });
    }

    public static CEIConfigurableFluidTank forFluid(Fluid fluid, long capacity, Runnable updateCallback) {
        Objects.requireNonNull(fluid, "fluid");
        Predicate<FluidVariant> predicate = variant -> variant.isOf(fluid);
        return new CEIConfigurableFluidTank(capacity, predicate, predicate, updateCallback);
    }

    public static CEIConfigurableFluidTank liquidExperience(long capacity) {
        return liquidExperience(capacity, () -> { });
    }

    public static CEIConfigurableFluidTank liquidExperience(long capacity, Runnable updateCallback) {
        return forFluid(CEIFluids.EXPERIENCE, capacity, updateCallback);
    }

    @Override
    public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);
        if (maxAmount == 0 || !insertPredicate.test(insertedVariant)) {
            return 0;
        }
        if (!isEmpty() && !variant.equals(insertedVariant)) {
            return 0;
        }
        long insertedAmount = Math.min(maxAmount, getSpace());
        if (insertedAmount <= 0) {
            return 0;
        }

        updateSnapshots(transaction);
        if (isEmpty()) {
            variant = insertedVariant;
        }
        amount += insertedAmount;
        return insertedAmount;
    }

    @Override
    public long extract(FluidVariant extractedVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(extractedVariant, maxAmount);
        if (maxAmount == 0 || isEmpty() || !variant.equals(extractedVariant) || !extractPredicate.test(extractedVariant)) {
            return 0;
        }
        long extractedAmount = Math.min(maxAmount, amount);
        if (extractedAmount <= 0) {
            return 0;
        }

        updateSnapshots(transaction);
        amount -= extractedAmount;
        if (amount == 0) {
            variant = createBlankVariant();
        }
        return extractedAmount;
    }

    @Override
    public boolean isResourceBlank() {
        return isEmpty();
    }

    @Override
    public FluidVariant getResource() {
        return isEmpty() ? createBlankVariant() : variant;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getVersion() {
        return version;
    }

    public FluidVariant getFluidVariant() {
        return getResource();
    }

    public FluidVariant createFluidVariant(Fluid fluid) {
        return createVariant(fluid);
    }

    public Identifier getFluidId() {
        return BuiltInRegistries.FLUID.getKey(getResource().getFluid());
    }

    public long getFluidAmount() {
        return amount;
    }

    public long getConfiguredCapacity() {
        return capacity;
    }

    public long getSpace() {
        return capacity - amount;
    }

    public boolean isEmpty() {
        return amount <= 0 || variant == null || variant.isBlank();
    }

    public boolean isFull() {
        return amount >= capacity;
    }

    public boolean canStore(FluidVariant candidate) {
        return !candidate.isBlank() && insertPredicate.test(candidate);
    }

    public void setFluid(FluidVariant variant, long amount) {
        setFluid(variant, amount, true);
    }

    public void clear() {
        setFluid(createBlankVariant(), 0, true);
    }

    public CompoundTag writeNbt() {
        CompoundTag tag = new CompoundTag();
        writeNbt(tag);
        return tag;
    }

    public void writeNbt(CompoundTag tag) {
        tag.putString(FLUID_KEY, isEmpty() ? "" : getFluidId().toString());
        tag.putLong(AMOUNT_KEY, amount);
    }

    public void readNbt(CompoundTag tag) {
        long storedAmount = tag.getLongOr(AMOUNT_KEY, 0);
        String fluidId = tag.getStringOr(FLUID_KEY, "");
        readFluid(fluidId, storedAmount);
    }

    public void write(ValueOutput view) {
        view.putString(FLUID_KEY, isEmpty() ? "" : getFluidId().toString());
        view.putLong(AMOUNT_KEY, amount);
    }

    public void read(ValueInput view) {
        long storedAmount = view.getLongOr(AMOUNT_KEY, 0);
        String fluidId = view.getStringOr(FLUID_KEY, "");
        readFluid(fluidId, storedAmount);
    }

    private void readFluid(String fluidId, long storedAmount) {
        if (storedAmount == 0 && fluidId.isEmpty()) {
            setFluid(createBlankVariant(), 0, false);
            return;
        }
        Identifier id = Identifier.tryParse(fluidId);
        if (id == null) {
            throw new IllegalArgumentException("Invalid serialized fluid id: " + fluidId);
        }
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown serialized fluid id: " + fluidId));
        if (fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("Serialized tank fluid cannot be minecraft:empty with amount " + storedAmount);
        }
        setFluid(createVariant(fluid), storedAmount, false);
    }

    protected FluidVariant createVariant(Fluid fluid) {
        return FluidVariant.of(fluid);
    }

    protected FluidVariant createBlankVariant() {
        return FluidVariant.blank();
    }

    private void setFluid(FluidVariant variant, long amount, boolean notify) {
        Objects.requireNonNull(variant, "variant");
        if (amount < 0) {
            throw new IllegalArgumentException("Tank amount cannot be negative");
        }
        if (amount > capacity) {
            throw new IllegalArgumentException("Tank amount " + amount + " exceeds capacity " + capacity);
        }
        if (amount == 0) {
            this.variant = createBlankVariant();
            this.amount = 0;
            if (notify) {
                markChanged();
            }
            return;
        }
        if (variant.isBlank()) {
            throw new IllegalArgumentException("Non-empty tank amount requires a non-blank fluid variant");
        }
        if (!insertPredicate.test(variant) || !extractPredicate.test(variant)) {
            throw new IllegalArgumentException("Fluid variant is not accepted by this tank: " + variant);
        }
        this.variant = variant;
        this.amount = amount;
        if (notify) {
            markChanged();
        }
    }

    private void markChanged() {
        version++;
        updateCallback.run();
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(variant, amount);
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        variant = snapshot.variant();
        amount = snapshot.amount();
    }

    @Override
    protected void onFinalCommit() {
        markChanged();
    }

    protected record Snapshot(FluidVariant variant, long amount) {
    }
}
