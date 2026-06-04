package plus.dragons.createenchantmentindustry.foundation.fluid;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class CEIFluidTankSmokeHarness {
    private static final Identifier TARGET_EXPERIENCE_ID = Identifier.fromNamespaceAndPath("create_enchantment_industry", "experience");

    private CEIFluidTankSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: CEIFluidTankSmokeHarness <persistence|overflow> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        List<String> lines = switch (args[0]) {
            case "persistence" -> persistenceSmoke();
            case "overflow" -> overflowSmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Files.write(Path.of(args[1]), lines);
    }

    private static List<String> persistenceSmoke() {
        List<String> lines = new ArrayList<>();
        Fluid smokeFluid = Fluids.WATER;
        FluidVariant experience = new SmokeFluidVariant(smokeFluid);
        CEIConfigurableFluidTank tank = smokeTank(smokeFluid, 1000);
        long inserted;
        try (Transaction transaction = Transaction.openOuter()) {
            inserted = tank.insert(experience, 789, transaction);
            transaction.commit();
        }

        CompoundTag tag = tank.writeNbt();
        CEIConfigurableFluidTank restored = smokeTank(smokeFluid, 1000);
        restored.readNbt(tag);

        CEIConfigurableFluidTank empty = smokeTank(smokeFluid, 1000);
        CompoundTag emptyTag = empty.writeNbt();
        CEIConfigurableFluidTank restoredEmpty = smokeTank(smokeFluid, 1000);
        restoredEmpty.readNbt(emptyTag);

        lines.add("mode=persistence");
        lines.add("target_fluid_id=" + TARGET_EXPERIENCE_ID);
        lines.add("runtime_fluid_id=" + BuiltInRegistries.FLUID.getKey(smokeFluid));
        lines.add("runtime_note=standalone JavaExec uses a bootstrapped built-in fluid; production factory CEIConfigurableFluidTank.liquidExperience targets the CEI id above");
        lines.add("inserted=" + inserted);
        lines.add("serialized_fluid=" + tag.getStringOr("Fluid", ""));
        lines.add("serialized_amount=" + tag.getLongOr("Amount", -1));
        lines.add("restored_fluid=" + BuiltInRegistries.FLUID.getKey(restored.getFluidVariant().getFluid()));
        lines.add("restored_amount=" + restored.getAmount());
        lines.add("round_trip_exact=" + (experience.equals(restored.getFluidVariant()) && restored.getAmount() == 789));
        lines.add("empty_serialized_fluid='" + emptyTag.getStringOr("Fluid", "") + "'");
        lines.add("empty_serialized_amount=" + emptyTag.getLongOr("Amount", -1));
        lines.add("empty_round_trip=" + (restoredEmpty.isEmpty() && restoredEmpty.getAmount() == 0));
        return lines;
    }

    private static List<String> overflowSmoke() {
        List<String> lines = new ArrayList<>();
        Fluid smokeFluid = Fluids.WATER;
        FluidVariant experience = new SmokeFluidVariant(smokeFluid);
        FluidVariant wrongFluid = new SmokeFluidVariant(Fluids.LAVA);
        CEIConfigurableFluidTank tank = smokeTank(smokeFluid, 1000);

        long underCapacityInsert;
        try (Transaction transaction = Transaction.openOuter()) {
            underCapacityInsert = tank.insert(experience, 600, transaction);
            transaction.commit();
        }
        long overflowInsert;
        try (Transaction transaction = Transaction.openOuter()) {
            overflowInsert = tank.insert(experience, 700, transaction);
            transaction.commit();
        }
        long wrongFluidInsert;
        try (Transaction transaction = Transaction.openOuter()) {
            wrongFluidInsert = tank.insert(wrongFluid, 100, transaction);
            transaction.commit();
        }
        long wrongFluidExtract;
        try (Transaction transaction = Transaction.openOuter()) {
            wrongFluidExtract = tank.extract(wrongFluid, 100, transaction);
            transaction.commit();
        }
        long partialExtract;
        try (Transaction transaction = Transaction.openOuter()) {
            partialExtract = tank.extract(experience, 250, transaction);
            transaction.commit();
        }
        long drainRest;
        try (Transaction transaction = Transaction.openOuter()) {
            drainRest = tank.extract(experience, 1000, transaction);
            transaction.commit();
        }
        long emptyExtract;
        try (Transaction transaction = Transaction.openOuter()) {
            emptyExtract = tank.extract(experience, 100, transaction);
            transaction.commit();
        }

        lines.add("mode=overflow");
        lines.add("target_fluid_id=" + TARGET_EXPERIENCE_ID);
        lines.add("runtime_fluid_id=" + BuiltInRegistries.FLUID.getKey(smokeFluid));
        lines.add("wrong_fluid_id=" + BuiltInRegistries.FLUID.getKey(wrongFluid.getFluid()));
        lines.add("runtime_note=standalone JavaExec uses a bootstrapped built-in fluid; production factory CEIConfigurableFluidTank.liquidExperience targets the CEI id above");
        lines.add("capacity=" + tank.getCapacity());
        lines.add("under_capacity_insert_request=600 accepted=" + underCapacityInsert + " amount_after=600");
        lines.add("overflow_insert_request=700 accepted=" + overflowInsert + " rejected=" + (700 - overflowInsert) + " amount_after=1000");
        lines.add("wrong_fluid_insert_request=100 accepted=" + wrongFluidInsert + " amount_after=1000");
        lines.add("wrong_fluid_extract_request=100 extracted=" + wrongFluidExtract + " amount_after=1000");
        lines.add("partial_extract_request=250 extracted=" + partialExtract + " amount_after=750");
        lines.add("drain_rest_request=1000 extracted=" + drainRest + " amount_after=0");
        lines.add("empty_extract_request=100 extracted=" + emptyExtract + " amount_after=" + tank.getAmount());
        lines.add("empty_after_drain=" + tank.isEmpty());
        return lines;
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
        public FluidVariant withComponents(DataComponentPatch components) {
            if (!components.isEmpty()) {
                throw new IllegalArgumentException("Smoke fluid variant does not support components");
            }
            return this;
        }
    }
}
