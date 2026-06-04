package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIServerConfig {
    private final CEIKineticsConfig kinetics;
    private final CEIFluidsConfig fluids;
    private final CEIEnchantmentsConfig enchantments;
    private final CEIProcessingConfig processing;

    private CEIServerConfig(
            CEIKineticsConfig kinetics,
            CEIFluidsConfig fluids,
            CEIEnchantmentsConfig enchantments,
            CEIProcessingConfig processing) {
        this.kinetics = kinetics;
        this.fluids = fluids;
        this.enchantments = enchantments;
        this.processing = processing;
    }

    static CEIServerConfig defaults() {
        return new CEIServerConfig(
                CEIKineticsConfig.defaults(),
                CEIFluidsConfig.defaults(),
                CEIEnchantmentsConfig.defaults(),
                CEIProcessingConfig.defaults());
    }

    static CEIServerConfig load(CEIConfig.ConfigReader reader) {
        return new CEIServerConfig(
                CEIKineticsConfig.load(reader.section("kinetics")),
                CEIFluidsConfig.load(reader.section("fluids")),
                CEIEnchantmentsConfig.load(reader.section("enchantments")),
                CEIProcessingConfig.load(reader.section("processing")));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.add("kinetics", kinetics.toJson());
        json.add("fluids", fluids.toJson());
        json.add("enchantments", enchantments.toJson());
        json.add("processing", processing.toJson());
        return json;
    }

    public CEIKineticsConfig kinetics() {
        return kinetics;
    }

    public CEIFluidsConfig fluids() {
        return fluids;
    }

    public CEIEnchantmentsConfig enchantments() {
        return enchantments;
    }

    public CEIProcessingConfig processing() {
        return processing;
    }
}
