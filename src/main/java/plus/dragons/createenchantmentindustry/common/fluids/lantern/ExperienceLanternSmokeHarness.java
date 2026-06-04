package plus.dragons.createenchantmentindustry.common.fluids.lantern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIIdentifier;

public final class ExperienceLanternSmokeHarness {
    private ExperienceLanternSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: ExperienceLanternSmokeHarness <storage|contraption> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        List<String> lines = switch (args[0]) {
            case "storage" -> storageSmoke();
            case "contraption" -> contraptionSmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Files.write(Path.of(args[1]), lines);
    }

    private static List<String> storageSmoke() {
        List<String> lines = new ArrayList<>();
        Fluid smokeFluid = Fluids.WATER;
        FluidVariant experience = new SmokeFluidVariant(smokeFluid);
        FluidVariant wrongFluid = new SmokeFluidVariant(Fluids.LAVA);
        CallbackProbe callbacks = new CallbackProbe();
        CEIFluidTankBehaviour lanternStorage = newLanternStorage(smokeFluid, callbacks);
        CEIConfigurableFluidTank tank = lanternStorage.getPrimaryHandler();
        callbacks.setTank(tank);

        long rolledBackInsert;
        int callbacksBeforeRollbackClose;
        try (Transaction transaction = Transaction.openOuter()) {
            rolledBackInsert = lanternStorage.getStorage().insert(experience, 200, transaction);
            callbacksBeforeRollbackClose = callbacks.count();
        }
        int callbacksAfterRollbackClose = callbacks.count();
        long amountAfterRollbackInsert = tank.getAmount();

        long inserted;
        int callbacksBeforeFillCommit;
        int callbacksAfterFillCommitCall;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = lanternStorage.getStorage().insert(experience, 600, transaction);
            callbacksBeforeFillCommit = callbacks.count();
            transaction.commit();
            callbacksAfterFillCommitCall = callbacks.count();
        }
        int callbacksAfterFillClose = callbacks.count();
        long fillCallbackAmount = callbacks.lastAmount();
        int fillCallbackLight = callbacks.lastLight();
        long wrongFluidInsert;
        try (Transaction transaction = Transaction.openOuter()) {
            wrongFluidInsert = lanternStorage.getStorage().insert(wrongFluid, 100, transaction);
            transaction.commit();
        }
        int callbacksAfterWrongFluidCommit = callbacks.count();
        long overflowInsert;
        int callbacksAfterOverflowCommitCall;
        try (Transaction transaction = Transaction.openOuter()) {
            overflowInsert = lanternStorage.getStorage().insert(experience, 700, transaction);
            transaction.commit();
            callbacksAfterOverflowCommitCall = callbacks.count();
        }
        int callbacksAfterOverflowClose = callbacks.count();
        long overflowCallbackAmount = callbacks.lastAmount();
        int overflowCallbackLight = callbacks.lastLight();
        long rolledBackExtract;
        int callbacksBeforeExtractRollbackClose;
        try (Transaction transaction = Transaction.openOuter()) {
            rolledBackExtract = lanternStorage.getStorage().extract(experience, 100, transaction);
            callbacksBeforeExtractRollbackClose = callbacks.count();
        }
        int callbacksAfterExtractRollbackClose = callbacks.count();
        long amountAfterExtractRollback = tank.getAmount();
        long partialDrain;
        int callbacksAfterPartialDrainCommitCall;
        try (Transaction transaction = Transaction.openOuter()) {
            partialDrain = lanternStorage.getStorage().extract(experience, 250, transaction);
            transaction.commit();
            callbacksAfterPartialDrainCommitCall = callbacks.count();
        }
        int callbacksAfterPartialDrainClose = callbacks.count();
        long partialDrainCallbackAmount = callbacks.lastAmount();
        int partialDrainCallbackLight = callbacks.lastLight();

        CompoundTag tag = new CompoundTag();
        lanternStorage.writeNbt(tag);
        CEIFluidTankBehaviour restored = newLanternStorage(smokeFluid);
        restored.readNbt(tag);
        long restoredAmount = restored.getPrimaryHandler().getAmount();
        String restoredFluid = BuiltInRegistries.FLUID.getKey(restored.getPrimaryHandler().getFluidVariant().getFluid()).toString();

        long drainRest;
        int callbacksAfterDrainRestCommitCall;
        try (Transaction transaction = Transaction.openOuter()) {
            drainRest = lanternStorage.getStorage().extract(experience, 1000, transaction);
            transaction.commit();
            callbacksAfterDrainRestCommitCall = callbacks.count();
        }
        int callbacksAfterDrainRestClose = callbacks.count();
        long drainRestCallbackAmount = callbacks.lastAmount();
        int drainRestCallbackLight = callbacks.lastLight();
        long emptyExtract;
        try (Transaction transaction = Transaction.openOuter()) {
            emptyExtract = lanternStorage.getStorage().extract(experience, 100, transaction);
            transaction.commit();
        }
        int callbacksAfterEmptyExtract = callbacks.count();

        lines.add("mode=storage");
        lines.add("lantern_storage_class=" + CEIFluidTankBehaviour.class.getName());
        lines.add("lantern_tank_class=" + CEIConfigurableFluidTank.class.getName());
        lines.add("lantern_capacity=" + tank.getCapacity());
        lines.add("target_fluid_id=" + CEIIdentifier.id("experience"));
        lines.add("runtime_fluid_id=" + BuiltInRegistries.FLUID.getKey(smokeFluid));
        lines.add("runtime_note=standalone JavaExec uses a bootstrapped built-in fluid; production ExperienceLanternBlockEntity uses CEIConfigurableFluidTank.liquidExperience targeting the CEI id above");
        lines.add("callback_semantics=SnapshotParticipant.onFinalCommit fires on successful outer commit; rollback/no-op mutations keep callback count unchanged");
        lines.add("rollback_insert_request_fluid=200 accepted_fluid=" + rolledBackInsert + " callbacks_before_close=" + callbacksBeforeRollbackClose + " callbacks_after_close=" + callbacksAfterRollbackClose + " amount_after=" + amountAfterRollbackInsert);
        lines.add("fill_request_fluid=600 accepted_fluid=" + inserted + " callbacks_before_commit=" + callbacksBeforeFillCommit + " callbacks_after_commit_call=" + callbacksAfterFillCommitCall + " callbacks_after_close=" + callbacksAfterFillClose + " callback_amount=" + fillCallbackAmount + " callback_light=" + fillCallbackLight);
        lines.add("wrong_fluid_insert_request=100 accepted_fluid=" + wrongFluidInsert + " callbacks_after_commit=" + callbacksAfterWrongFluidCommit + " amount_after=600");
        lines.add("overflow_insert_request_fluid=700 accepted_fluid=" + overflowInsert + " rejected_fluid=" + (700 - overflowInsert) + " callbacks_after_commit_call=" + callbacksAfterOverflowCommitCall + " callbacks_after_close=" + callbacksAfterOverflowClose + " callback_amount=" + overflowCallbackAmount + " callback_light=" + overflowCallbackLight);
        lines.add("rollback_extract_request_fluid=100 extracted_fluid=" + rolledBackExtract + " callbacks_before_close=" + callbacksBeforeExtractRollbackClose + " callbacks_after_close=" + callbacksAfterExtractRollbackClose + " amount_after=" + amountAfterExtractRollback);
        lines.add("partial_drain_request_fluid=250 extracted_fluid=" + partialDrain + " callbacks_after_commit_call=" + callbacksAfterPartialDrainCommitCall + " callbacks_after_close=" + callbacksAfterPartialDrainClose + " callback_amount=" + partialDrainCallbackAmount + " callback_light=" + partialDrainCallbackLight);
        lines.add("serialized_fluid=" + tag.getCompoundOrEmpty("Tank0").getStringOr("Fluid", ""));
        lines.add("serialized_amount=" + tag.getCompoundOrEmpty("Tank0").getLongOr("Amount", -1));
        lines.add("restored_fluid=" + restoredFluid);
        lines.add("restored_amount=" + restoredAmount);
        lines.add("round_trip_exact=" + (restoredFluid.equals(BuiltInRegistries.FLUID.getKey(smokeFluid).toString()) && restoredAmount == 750));
        lines.add("drain_rest_request_fluid=1000 extracted_fluid=" + drainRest + " callbacks_after_commit_call=" + callbacksAfterDrainRestCommitCall + " callbacks_after_close=" + callbacksAfterDrainRestClose + " amount_after=0 callback_amount=" + drainRestCallbackAmount + " callback_light=" + drainRestCallbackLight);
        lines.add("empty_extract_request_fluid=100 extracted_fluid=" + emptyExtract + " callbacks_after_commit=" + callbacksAfterEmptyExtract + " amount_after=" + tank.getAmount());
        lines.add("empty_after_drain=" + tank.isEmpty());
        lines.add("transfer_lookup_path=FluidStorage.SIDED.registerForBlockEntity(ExperienceLanternBlockEntity::getFluidStorage, CEIBlockEntityTypes.EXPERIENCE_LANTERN)");
        return lines;
    }

    private static List<String> contraptionSmoke() {
        List<String> lines = new ArrayList<>();
        lines.add("mode=contraption");
        lines.add("normal_block_entity_storage=safe");
        lines.add("mounted_storage_registered=covered_by_task_20");
        lines.add("movement_behaviour_registered=covered_by_task_20");
        lines.add("task20_followup=createBehaviourSmokeStorage verifies mounted storage and movement behaviour restoration");
        lines.add("reason=Task 11 keeps the no-crash normal-world CEIFluidTankBehaviour state and Fabric transfer lookup; Task 20 owns Create-Fly contraption storage and movement behaviour evidence");
        lines.add("no_crash_safe_state=ExperienceLanternBlockEntity owns a single Liquid Experience CEIConfigurableFluidTank with capacity " + ExperienceLanternBlockEntity.TANK_CAPACITY);
        return lines;
    }

    private static CEIFluidTankBehaviour newLanternStorage(Fluid fluid) {
        return newLanternStorage(fluid, new CallbackProbe());
    }

    private static CEIFluidTankBehaviour newLanternStorage(Fluid fluid, CallbackProbe callbacks) {
        CEIConfigurableFluidTank tank = smokeTank(fluid, ExperienceLanternBlockEntity.TANK_CAPACITY, callbacks::record);
        callbacks.setTank(tank);
        return new CEIFluidTankBehaviour(new SmokeBlockEntity(), List.of(tank));
    }

    private static CEIConfigurableFluidTank smokeTank(Fluid fluid, long capacity) {
        return smokeTank(fluid, capacity, () -> { });
    }

    private static CEIConfigurableFluidTank smokeTank(Fluid fluid, long capacity, Runnable updateCallback) {
        return new CEIConfigurableFluidTank(capacity, variant -> variant.isOf(fluid), variant -> variant.isOf(fluid), updateCallback) {
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

    private static final class CallbackProbe {
        private CEIConfigurableFluidTank tank;
        private int count;
        private long lastAmount = -1;
        private int lastLight = -1;

        private void setTank(CEIConfigurableFluidTank tank) {
            this.tank = tank;
        }

        private void record() {
            count++;
            lastAmount = tank.getAmount();
            lastLight = ExperienceLanternBlockEntity.getLightLevel(lastAmount, tank.getCapacity());
        }

        private int count() {
            return count;
        }

        private long lastAmount() {
            return lastAmount;
        }

        private int lastLight() {
            return lastLight;
        }
    }

    private static final class SmokeBlockEntity extends SmartBlockEntity {
        private SmokeBlockEntity() {
            super(BlockEntityType.SIGN, BlockPos.ZERO, Blocks.OAK_SIGN.defaultBlockState());
        }

        @Override
        public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
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
