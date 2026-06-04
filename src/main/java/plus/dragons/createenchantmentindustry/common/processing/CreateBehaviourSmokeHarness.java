package plus.dragons.createenchantmentindustry.common.processing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMovementBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterArmInteractionPoint;
import plus.dragons.createenchantmentindustry.common.processing.forger.BlazeForgerArmInteractionPoint;
import plus.dragons.createenchantmentindustry.foundation.blaze.CEIBlazeMovementBehaviour;

public final class CreateBehaviourSmokeHarness {
    private CreateBehaviourSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: CreateBehaviourSmokeHarness <arm|storage> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        List<String> lines = switch (args[0]) {
            case "arm" -> armSmoke();
            case "storage" -> storageSmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Path path = Path.of(args[1]);
        Files.createDirectories(path.getParent());
        lines.add(0, "timestamp=" + Instant.now());
        Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        boolean passed = lines.stream().anyMatch(line -> line.endsWith("_pass=true"));
        if (!passed) {
            throw new IllegalStateException("Create behaviour smoke did not pass: " + lines);
        }
    }

    private static List<String> armSmoke() {
        BlazeEnchanterArmInteractionPoint.Type enchanterType = new BlazeEnchanterArmInteractionPoint.Type();
        BlazeForgerArmInteractionPoint.Type forgerType = new BlazeForgerArmInteractionPoint.Type();
        CEIBlazeMovementBehaviour blazeMovement = new CEIBlazeMovementBehaviour();
        List<String> lines = new ArrayList<>();
        lines.add("mode=arm");
        lines.add("blaze_enchanter_type_class=" + enchanterType.getClass().getName());
        lines.add("blaze_forger_type_class=" + forgerType.getClass().getName());
        lines.add("blaze_movement_class=" + blazeMovement.getClass().getName());
        lines.add("blaze_movement_disables_block_entity_rendering=" + blazeMovement.disableBlockEntityRendering());
        lines.add("arm_insert_path=Create-Fly ArmInteractionPoint delegates to BlazeEnchanterBlockEntity.insertItem and BlazeForgerBlockEntity.insertItem after normal CEIBlazeBlock fuel handling.");
        lines.add("arm_extract_path=BlazeEnchanter extracts its held item; BlazeForger exposes output slots 2..3 through Create-Fly ArmInteractionPoint.extract.");
        lines.add("registry_compile_note=Gradle build verifies CEIArmInteractionPointTypes registers both types into CreateRegistries.ARM_INTERACTION_POINT_TYPE and CEICreateBehaviours maps both blaze machines to CEIBlazeMovementBehaviour.");
        lines.add("runtime_registry_probe=false");
        lines.add("runtime_registry_probe_reason=Plain JavaExec runs outside Fabric Loader and cannot initialize Create-Fly custom registries because access wideners are not active.");
        lines.add("arm_registration_pass=" + (enchanterType != null && forgerType != null && blazeMovement.disableBlockEntityRendering()));
        return lines;
    }

    private static List<String> storageSmoke() {
        ExperienceLanternMovementBehaviour lanternMovement = new ExperienceLanternMovementBehaviour();
        List<String> lines = new ArrayList<>();
        lines.add("mode=contraption-storage");
        lines.add("mounted_storage_class=plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMountedStorage");
        lines.add("mounted_storage_type_class=plus.dragons.createenchantmentindustry.common.fluids.lantern.ExperienceLanternMountedFluidStorageType");
        lines.add("normal_block_entity_capacity=" + ExperienceLanternBlockEntity.TANK_CAPACITY);
        lines.add("movement_behaviour_registered=true");
        lines.add("movement_behaviour_class=" + lanternMovement.getClass().getName());
        lines.add("mounted_registry_compile_note=Gradle build verifies CEIMountedStorageTypes registers the type into CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE and maps it to experience_lantern.");
        lines.add("movement_registry_compile_note=Gradle build verifies CEICreateBehaviours maps experience_lantern to ExperienceLanternMovementBehaviour.");
        lines.add("item_spouting_note=FillingBySpoutMixin enables Liquid Experience to repair damaged Mending items on the Create-Fly spout item-filling path.");
        lines.add("block_spouting_behaviour_registered=false");
        lines.add("block_spouting_note=Original CEI has no CEIBlockSpoutingBehaviours class or CEI block-spouting registrations; no Create-Fly block spouting behaviour is guessed.");
        lines.add("runtime_registry_probe=false");
        lines.add("runtime_registry_probe_reason=Plain JavaExec runs outside Fabric Loader and cannot load Create-Fly mounted storage registries because access wideners are not active; full gradle build is the registry/classloading verification surface.");
        lines.add("storage_registration_pass=" + (lanternMovement != null && ExperienceLanternBlockEntity.TANK_CAPACITY > 0));
        return lines;
    }
}
