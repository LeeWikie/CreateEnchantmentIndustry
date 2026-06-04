package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIProcessingConfig {
    private final float regularLightningStrikeTransformXpBlockChance;

    private CEIProcessingConfig(float regularLightningStrikeTransformXpBlockChance) {
        this.regularLightningStrikeTransformXpBlockChance = regularLightningStrikeTransformXpBlockChance;
    }

    static CEIProcessingConfig defaults() {
        return new CEIProcessingConfig(1.0f);
    }

    static CEIProcessingConfig load(CEIConfig.ConfigReader reader) {
        return new CEIProcessingConfig(reader.readFloat("regularLightningStrikeTransformXpBlockChance", 1.0f, 0.0f, 1.0f));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("regularLightningStrikeTransformXpBlockChance", regularLightningStrikeTransformXpBlockChance);
        return json;
    }

    public float regularLightningStrikeTransformXpBlockChance() {
        return regularLightningStrikeTransformXpBlockChance;
    }
}
