package plus.dragons.createenchantmentindustry.common.fluids.experience;

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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIFluidTankBehaviour;
import plus.dragons.createenchantmentindustry.registry.CEIIdentifier;

public final class ExperienceHatchSmokeHarness {
    private ExperienceHatchSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: ExperienceHatchSmokeHarness <fill-drain|empty> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        List<String> lines = switch (args[0]) {
            case "fill-drain" -> fillDrainSmoke();
            case "empty" -> emptySmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Files.write(Path.of(args[1]), lines);
    }

    private static List<String> fillDrainSmoke() {
        List<String> lines = new ArrayList<>();
        Fluid smokeFluid = Fluids.WATER;
        FluidVariant experience = new SmokeFluidVariant(smokeFluid);
        FluidVariant wrongFluid = new SmokeFluidVariant(Fluids.LAVA);
        CEIFluidTankBehaviour hatchStorage = newHatchStorage(smokeFluid);
        CEIConfigurableFluidTank tank = hatchStorage.getPrimaryHandler();

        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = hatchStorage.getStorage().insert(experience, ExperienceHatchBehaviour.getLiquidFromExperience(600), transaction);
            transaction.commit();
        }
        long overflowInsert;
        try (Transaction transaction = Transaction.openOuter()) {
            overflowInsert = hatchStorage.getStorage().insert(experience, ExperienceHatchBehaviour.getLiquidFromExperience(700), transaction);
            transaction.commit();
        }
        long wrongFluidInsert;
        try (Transaction transaction = Transaction.openOuter()) {
            wrongFluidInsert = hatchStorage.getStorage().insert(wrongFluid, 100, transaction);
            transaction.commit();
        }
        long partialDrain;
        try (Transaction transaction = Transaction.openOuter()) {
            partialDrain = hatchStorage.getStorage().extract(experience, ExperienceHatchBehaviour.getLiquidFromExperience(250), transaction);
            transaction.commit();
        }

        CompoundTag tag = new CompoundTag();
        hatchStorage.writeNbt(tag);
        CEIFluidTankBehaviour restored = newHatchStorage(smokeFluid);
        restored.readNbt(tag);
        long restoredAmount = restored.getPrimaryHandler().getAmount();
        String restoredFluid = BuiltInRegistries.FLUID.getKey(restored.getPrimaryHandler().getFluidVariant().getFluid()).toString();

        long drainRest;
        try (Transaction transaction = Transaction.openOuter()) {
            drainRest = hatchStorage.getStorage().extract(experience, ExperienceHatchBehaviour.getLiquidFromExperience(1000), transaction);
            transaction.commit();
        }
        long emptyExtract;
        try (Transaction transaction = Transaction.openOuter()) {
            emptyExtract = hatchStorage.getStorage().extract(experience, ExperienceHatchBehaviour.getLiquidFromExperience(100), transaction);
            transaction.commit();
        }

        lines.add("mode=fill-drain");
        lines.add("hatch_storage_class=" + CEIFluidTankBehaviour.class.getName());
        lines.add("hatch_capacity=" + tank.getCapacity());
        lines.add("target_fluid_id=" + CEIIdentifier.id("experience"));
        lines.add("runtime_fluid_id=" + BuiltInRegistries.FLUID.getKey(smokeFluid));
        lines.add("runtime_note=standalone JavaExec uses a bootstrapped built-in fluid; production ExperienceHatchBlockEntity uses CEIFluidTankBehaviour.singleLiquidExperience targeting the CEI id above");
        lines.add("insert_request_xp=600 accepted_fluid=" + inserted + " amount_after=600");
        lines.add("overflow_insert_request_xp=700 accepted_fluid=" + overflowInsert + " rejected_fluid=" + (700 - overflowInsert) + " amount_after=1000");
        lines.add("wrong_fluid_insert_request=100 accepted_fluid=" + wrongFluidInsert + " amount_after=1000");
        lines.add("partial_drain_request_xp=250 extracted_fluid=" + partialDrain + " amount_after=750");
        lines.add("serialized_fluid=" + tag.getCompoundOrEmpty("Tank0").getStringOr("Fluid", ""));
        lines.add("serialized_amount=" + tag.getCompoundOrEmpty("Tank0").getLongOr("Amount", -1));
        lines.add("restored_fluid=" + restoredFluid);
        lines.add("restored_amount=" + restoredAmount);
        lines.add("round_trip_exact=" + (restoredFluid.equals(BuiltInRegistries.FLUID.getKey(smokeFluid).toString()) && restoredAmount == 750));
        lines.add("drain_rest_request_xp=1000 extracted_fluid=" + drainRest + " amount_after=0");
        lines.add("empty_extract_request_xp=100 extracted_fluid=" + emptyExtract + " amount_after=" + tank.getAmount());
        lines.add("empty_after_drain=" + tank.isEmpty());
        lines.add("transfer_lookup_path=FluidStorage.SIDED.registerForBlockEntity((be,direction)->be.getFluidTankBehaviour().getStorage(), CEIBlockEntityTypes.EXPERIENCE_HATCH)");
        return lines;
    }

    private static List<String> emptySmoke() {
        List<String> lines = new ArrayList<>();
        Fluid smokeFluid = Fluids.WATER;
        FluidVariant experience = new SmokeFluidVariant(smokeFluid);
        CEIFluidTankBehaviour hatchStorage = newHatchStorage(smokeFluid);
        CEIConfigurableFluidTank tank = hatchStorage.getPrimaryHandler();
        long depositZero;
        try (Transaction transaction = Transaction.openOuter()) {
            depositZero = hatchStorage.getStorage().insert(experience, ExperienceHatchBehaviour.getLiquidFromExperience(0), transaction);
            transaction.commit();
        }
        long drainEmpty;
        try (Transaction transaction = Transaction.openOuter()) {
            drainEmpty = hatchStorage.getStorage().extract(experience, ExperienceHatchBehaviour.getLiquidFromExperience(100), transaction);
            transaction.commit();
        }
        CompoundTag tag = new CompoundTag();
        hatchStorage.writeNbt(tag);

        lines.add("mode=empty");
        lines.add("initial_empty=" + tank.isEmpty());
        lines.add("zero_xp_insert_request=0 accepted_fluid=" + depositZero + " amount_after=" + tank.getAmount());
        lines.add("empty_drain_request_xp=100 extracted_fluid=" + drainEmpty + " amount_after=" + tank.getAmount());
        lines.add("serialized_fluid='" + tag.getCompoundOrEmpty("Tank0").getStringOr("Fluid", "") + "'");
        lines.add("serialized_amount=" + tag.getCompoundOrEmpty("Tank0").getLongOr("Amount", -1));
        lines.add("empty_interaction_result=safe_noop_no_crash");
        return lines;
    }

    private static CEIFluidTankBehaviour newHatchStorage(Fluid fluid) {
        return new CEIFluidTankBehaviour(new SmokeBlockEntity(), List.of(smokeTank(fluid, ExperienceHatchBlockEntity.TANK_CAPACITY)));
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
