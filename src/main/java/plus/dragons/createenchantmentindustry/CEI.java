package plus.dragons.createenchantmentindustry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.registry.CEIRegistration;

public final class CEI implements ModInitializer {
    public static final String MOD_ID = "create_enchantment_industry";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        CEIConfig.init();
        CEIRegistration.register();
        LOGGER.info("Create Enchantment Industry initialized with Fabric Liquid Experience support.");
    }
}
