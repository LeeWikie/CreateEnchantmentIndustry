package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIStressConfig {
    private static final double DEFAULT_MECHANICAL_GRINDSTONE_IMPACT = 4.0;
    private static final double DEFAULT_GRINDSTONE_DRAIN_IMPACT = 4.0;

    private final double mechanicalGrindstoneImpact;
    private final double grindstoneDrainImpact;

    private CEIStressConfig(double mechanicalGrindstoneImpact, double grindstoneDrainImpact) {
        this.mechanicalGrindstoneImpact = mechanicalGrindstoneImpact;
        this.grindstoneDrainImpact = grindstoneDrainImpact;
    }

    static CEIStressConfig defaults() {
        return new CEIStressConfig(DEFAULT_MECHANICAL_GRINDSTONE_IMPACT, DEFAULT_GRINDSTONE_DRAIN_IMPACT);
    }

    static CEIStressConfig load(CEIConfig.ConfigReader reader) {
        return new CEIStressConfig(
                reader.readDouble("mechanicalGrindstoneImpact", DEFAULT_MECHANICAL_GRINDSTONE_IMPACT, 0.0, Double.MAX_VALUE),
                reader.readDouble("grindstoneDrainImpact", DEFAULT_GRINDSTONE_DRAIN_IMPACT, 0.0, Double.MAX_VALUE));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("mechanicalGrindstoneImpact", mechanicalGrindstoneImpact);
        json.addProperty("grindstoneDrainImpact", grindstoneDrainImpact);
        return json;
    }

    public double mechanicalGrindstoneImpact() {
        return mechanicalGrindstoneImpact;
    }

    public double grindstoneDrainImpact() {
        return grindstoneDrainImpact;
    }
}
