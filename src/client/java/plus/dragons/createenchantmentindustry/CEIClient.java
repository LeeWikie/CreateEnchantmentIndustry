package plus.dragons.createenchantmentindustry;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import plus.dragons.createenchantmentindustry.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.registry.CEIIdentifier;

public final class CEIClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerFluidRendering();
        CEI.LOGGER.info(CEIClientRendererCoverage.runtimeSummary());
        CEI.LOGGER.info("Create Enchantment Industry client initialized.");
    }

    private static void registerFluidRendering() {
        Material still = new Material(CEIIdentifier.id("fluid/experience_still"), true);
        Material flowing = new Material(CEIIdentifier.id("fluid/experience_flow"), true);
        BlockTintSource tint = state -> 0xFFFFFFFF;
        FluidModel.Unbaked model = new FluidModel.Unbaked(still, flowing, null, tint);
        FluidRenderingRegistry.register(CEIFluids.EXPERIENCE, CEIFluids.EXPERIENCE_FLOWING, model);
        FluidRenderingRegistry.setBlockTransparency(CEIBlocks.EXPERIENCE, true);
    }
}
