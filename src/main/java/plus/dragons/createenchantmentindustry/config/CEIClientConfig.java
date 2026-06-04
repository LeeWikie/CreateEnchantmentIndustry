package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIClientConfig {
    private static final float DEFAULT_EXPERIENCE_VISION_MULTIPLIER = 1.0f;

    private final float experienceVisionMultiplier;

    private CEIClientConfig(float experienceVisionMultiplier) {
        this.experienceVisionMultiplier = experienceVisionMultiplier;
    }

    static CEIClientConfig defaults() {
        return new CEIClientConfig(DEFAULT_EXPERIENCE_VISION_MULTIPLIER);
    }

    static CEIClientConfig load(CEIConfig.ConfigReader reader) {
        return new CEIClientConfig(reader.readFloat("experienceVisionMultiplier", DEFAULT_EXPERIENCE_VISION_MULTIPLIER, 1.0f, 256.0f));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("experienceVisionMultiplier", experienceVisionMultiplier);
        return json;
    }

    public float experienceVisionMultiplier() {
        return experienceVisionMultiplier;
    }
}
