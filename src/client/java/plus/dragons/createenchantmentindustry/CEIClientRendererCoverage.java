package plus.dragons.createenchantmentindustry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class CEIClientRendererCoverage {
    private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath().normalize();
    private static final Path ASSETS_ROOT = PROJECT_ROOT.resolve("src/main/resources/assets/create_enchantment_industry");

    private static final String[] BLOCKSTATES = {
            "blockstates/printer.json",
            "blockstates/grindstone_drain.json",
            "blockstates/blaze_enchanter.json",
            "blockstates/blaze_forger.json",
            "blockstates/mechanical_grindstone.json"
    };

    private static final String[] BLOCK_MODELS = {
            "models/block/printer/block.json",
            "models/block/printer/item.json",
            "models/block/printer/head.json",
            "models/block/printer/nozzle_top.json",
            "models/block/printer/nozzle_bottom.json",
            "models/block/printer/piston.json",
            "models/block/grindstone_drain/block.json",
            "models/block/grindstone_drain/item.json",
            "models/block/blaze_enchanter.json",
            "models/block/blaze_forger.json",
            "models/block/blaze/enchanter_hat.json",
            "models/block/blaze/enchanter_hat_small.json",
            "models/block/blaze/forger_hat.json",
            "models/block/blaze/forger_hat_small.json",
            "models/block/mechanical_grindstone.json"
    };

    private static final String[] ITEM_MODELS = {
            "models/item/printer.json",
            "models/item/grindstone_drain.json",
            "models/item/blaze_enchanter.json",
            "models/item/blaze_forger.json",
            "models/item/mechanical_grindstone.json",
            "models/item/enchanting_template.json",
            "models/item/super_enchanting_template.json",
            "models/item/experience_bucket.json"
    };

    private static final String[] LOCAL_TEXTURES = {
            "textures/block/mechanical_grinder_front.png",
            "textures/block/mechanical_grinder_back.png",
            "textures/block/enchanter_hat.png",
            "textures/block/enchanter_hat_small.png",
            "textures/block/forger_hat.png",
            "textures/block/forger_hat_small.png",
            "textures/item/enchanting_template.png",
            "textures/item/super_enchanting_template.png",
            "textures/item/experience_bucket.png",
            "textures/fluid/experience_still.png",
            "textures/fluid/experience_flow.png",
            "textures/fluid/experience_still.png.mcmeta",
            "textures/fluid/experience_flow.png.mcmeta"
    };

    private static final String[] FORBIDDEN_COMMON_TOKENS = {
            "net.minecraft.client",
            "net.fabricmc.api.ClientModInitializer",
            "net.fabricmc.fabric.api.client",
            "FluidRenderingRegistry",
            "BlockEntityRenderer",
            "BuiltinItemRenderer",
            "CustomRenderedItemModelRenderer",
            "plus.dragons.createenchantmentindustry.CEIClient",
            "CEIClientRendererCoverage"
    };

    private CEIClientRendererCoverage() {
    }

    public static String runtimeSummary() {
        return "Client renderer fallback: Liquid Experience uses Fabric fluid rendering; CEI machines and items use baked JSON model coverage without custom animated BER/item renderer registration.";
    }

    public static void main(String[] args) throws IOException {
        Path evidencePath = args.length == 0
                ? PROJECT_ROOT.resolve("../.sisyphus/evidence/task-19-client-renderers.log").normalize()
                : Path.of(args[0]).toAbsolutePath().normalize();
        String report = buildReport();
        Path parent = evidencePath.getParent();
        if (parent != null)
            Files.createDirectories(parent);
        Files.writeString(evidencePath, report, StandardCharsets.UTF_8);
    }

    private static String buildReport() throws IOException {
        StringBuilder report = new StringBuilder();
        List<String> failures = new ArrayList<>();

        report.append("Task 19 client renderer/model coverage evidence\n");
        report.append("Project root: ").append(PROJECT_ROOT).append('\n');
        report.append("Mode: baked JSON model coverage with explicit custom renderer limitations\n\n");
        report.append(runtimeSummary()).append("\n\n");

        verifyFabricEntrypoints(report, failures);
        verifyClientFluidRendering(report, failures);
        verifyCommonSourceIsolation(report, failures);
        verifyRequiredAssets("Blockstates", BLOCKSTATES, report, failures);
        verifyRequiredAssets("Block and partial models", BLOCK_MODELS, report, failures);
        verifyRequiredAssets("Item models", ITEM_MODELS, report, failures);
        verifyRequiredAssets("Local CEI textures and fluid sprites", LOCAL_TEXTURES, report, failures);
        appendLimitations(report);

        if (failures.isEmpty()) {
            report.append("\nResult: PASS\n");
            return report.toString();
        }

        report.append("\nResult: FAIL\n");
        for (String failure : failures)
            report.append("[FAIL] ").append(failure).append('\n');
        throw new IllegalStateException(report.toString());
    }

    private static void verifyFabricEntrypoints(StringBuilder report, List<String> failures) throws IOException {
        Path modJson = PROJECT_ROOT.resolve("src/main/resources/fabric.mod.json");
        String content = Files.readString(modJson, StandardCharsets.UTF_8);
        boolean hasMainEntrypoint = content.contains("\"main\"")
                && content.contains("\"plus.dragons.createenchantmentindustry.CEI\"");
        boolean hasClientEntrypoint = content.contains("\"client\"")
                && content.contains("\"plus.dragons.createenchantmentindustry.CEIClient\"");
        boolean hasSingleClientEntrypointReference = countOccurrences(content, "\"plus.dragons.createenchantmentindustry.CEIClient\"") == 1;

        report.append("Fabric entrypoints\n");
        appendStatus(report, hasMainEntrypoint, "main entrypoint remains plus.dragons.createenchantmentindustry.CEI");
        appendStatus(report, hasClientEntrypoint, "client entrypoint remains plus.dragons.createenchantmentindustry.CEIClient");
        appendStatus(report, hasSingleClientEntrypointReference, "CEIClient is referenced exactly once in fabric.mod.json");
        report.append('\n');

        if (!hasMainEntrypoint)
            failures.add("fabric.mod.json main entrypoint is missing CEI");
        if (!hasClientEntrypoint)
            failures.add("fabric.mod.json client entrypoint is missing CEIClient");
        if (!hasSingleClientEntrypointReference)
            failures.add("fabric.mod.json should reference CEIClient exactly once");
    }

    private static void verifyClientFluidRendering(StringBuilder report, List<String> failures) throws IOException {
        Path clientSource = PROJECT_ROOT.resolve("src/client/java/plus/dragons/createenchantmentindustry/CEIClient.java");
        String content = Files.readString(clientSource, StandardCharsets.UTF_8);
        boolean registersFluidModel = content.contains("FluidRenderingRegistry.register")
                && content.contains("CEIFluids.EXPERIENCE")
                && content.contains("CEIFluids.EXPERIENCE_FLOWING");
        boolean keepsExperienceBlockTransparency = content.contains("FluidRenderingRegistry.setBlockTransparency")
                && content.contains("CEIBlocks.EXPERIENCE");

        report.append("Client fluid rendering\n");
        appendStatus(report, registersFluidModel, "Liquid Experience still and flowing fluids stay registered through Fabric fluid rendering");
        appendStatus(report, keepsExperienceBlockTransparency, "Liquid Experience block transparency stays registered");
        report.append('\n');

        if (!registersFluidModel)
            failures.add("CEIClient no longer registers Liquid Experience fluid rendering");
        if (!keepsExperienceBlockTransparency)
            failures.add("CEIClient no longer registers Liquid Experience block transparency");
    }

    private static void verifyCommonSourceIsolation(StringBuilder report, List<String> failures) throws IOException {
        Path mainSourceRoot = PROJECT_ROOT.resolve("src/main/java");
        List<Path> sourcePaths;
        try (Stream<Path> walkedPaths = Files.walk(mainSourceRoot)) {
            sourcePaths = walkedPaths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }

        List<String> hits = new ArrayList<>();
        for (Path sourcePath : sourcePaths) {
            String content = Files.readString(sourcePath, StandardCharsets.UTF_8);
            for (String token : FORBIDDEN_COMMON_TOKENS) {
                if (content.contains(token))
                    hits.add(PROJECT_ROOT.relativize(sourcePath) + " contains " + token);
            }
        }

        report.append("Client source isolation\n");
        report.append("[OK] Scanned ").append(sourcePaths.size()).append(" common Java files under src/main/java\n");
        appendStatus(report, hits.isEmpty(), "No common/server Java source imports or references client renderer APIs or CEI client helpers");
        report.append('\n');

        failures.addAll(hits);
    }

    private static void verifyRequiredAssets(String groupName, String[] relativePaths, StringBuilder report, List<String> failures) throws IOException {
        report.append(groupName).append('\n');
        for (String relativePath : relativePaths) {
            Path assetPath = ASSETS_ROOT.resolve(relativePath);
            boolean exists = Files.isRegularFile(assetPath);
            boolean hasContent = exists && Files.size(assetPath) > 0;
            appendStatus(report, hasContent, relativePath);
            if (!exists)
                failures.add("Missing asset " + relativePath);
            else if (!hasContent)
                failures.add("Empty asset " + relativePath);
        }
        report.append('\n');
    }

    private static void appendLimitations(StringBuilder report) {
        report.append("Custom renderer parity notes\n");
        report.append("[INFO] Original PrinterRenderer animated the tank fill, piston, and nozzle partials; Fabric fallback verifies the printer block/item and partial JSON resources only.\n");
        report.append("[INFO] Original GrindstoneDrainRenderer rendered moving items and fluid level; Fabric fallback verifies the baked drain block/item resources only.\n");
        report.append("[INFO] Original BlazeEnchanterRenderer and BlazeForgerRenderer rendered held/orbiting items and blaze hat partials; Fabric fallback verifies base block/item models and hat partial JSON resources only.\n");
        report.append("[INFO] Original blaze block item renderers composed custom hat partials; Fabric fallback uses baked JSON item models for blaze_enchanter and blaze_forger.\n");
        report.append("[INFO] Enchanting template items use generated JSON item models and textures; no Fabric custom item renderer is registered.\n");
    }

    private static void appendStatus(StringBuilder report, boolean ok, String message) {
        report.append(ok ? "[OK] " : "[FAIL] ").append(message).append('\n');
    }

    private static int countOccurrences(String content, String token) {
        int occurrences = 0;
        int searchFrom = 0;
        while (true) {
            int nextIndex = content.indexOf(token, searchFrom);
            if (nextIndex < 0)
                return occurrences;
            occurrences++;
            searchFrom = nextIndex + token.length();
        }
    }
}
