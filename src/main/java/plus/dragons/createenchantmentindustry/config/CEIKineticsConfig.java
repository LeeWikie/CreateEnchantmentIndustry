package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIKineticsConfig {
    private final boolean deployerKillDropXp;
    private final float deployerKillXpScale;
    private final boolean deployerMineDropXp;
    private final float deployerMineXpScale;
    private final boolean deployerCollectXp;
    private final boolean deployerMendItem;
    private final boolean deployerSweepAttack;
    private final boolean crushingWheelKillDropXp;
    private final float crushingWheelKillDropXpChance;
    private final float crushingWheelKillDropXpScale;
    private final CEIStressConfig stress;

    private CEIKineticsConfig(
            boolean deployerKillDropXp,
            float deployerKillXpScale,
            boolean deployerMineDropXp,
            float deployerMineXpScale,
            boolean deployerCollectXp,
            boolean deployerMendItem,
            boolean deployerSweepAttack,
            boolean crushingWheelKillDropXp,
            float crushingWheelKillDropXpChance,
            float crushingWheelKillDropXpScale,
            CEIStressConfig stress) {
        this.deployerKillDropXp = deployerKillDropXp;
        this.deployerKillXpScale = deployerKillXpScale;
        this.deployerMineDropXp = deployerMineDropXp;
        this.deployerMineXpScale = deployerMineXpScale;
        this.deployerCollectXp = deployerCollectXp;
        this.deployerMendItem = deployerMendItem;
        this.deployerSweepAttack = deployerSweepAttack;
        this.crushingWheelKillDropXp = crushingWheelKillDropXp;
        this.crushingWheelKillDropXpChance = crushingWheelKillDropXpChance;
        this.crushingWheelKillDropXpScale = crushingWheelKillDropXpScale;
        this.stress = stress;
    }

    static CEIKineticsConfig defaults() {
        return new CEIKineticsConfig(true, 1.0f, true, 1.0f, true, true, true, true, 0.3f, 0.34f, CEIStressConfig.defaults());
    }

    static CEIKineticsConfig load(CEIConfig.ConfigReader reader) {
        return new CEIKineticsConfig(
                reader.readBoolean("deployerKillDropXp", true),
                reader.readFloat("deployerKillXpScale", 1.0f, 0.0f, 1.0f),
                reader.readBoolean("deployerMineDropXp", true),
                reader.readFloat("deployerMineXpScale", 1.0f, 0.0f, 1.0f),
                reader.readBoolean("deployerCollectXp", true),
                reader.readBoolean("deployerMendItem", true),
                reader.readBoolean("deployerSweepAttack", true),
                reader.readBoolean("crushingWheelKillDropXp", true),
                reader.readFloat("crushingWheelKillDropXpChance", 0.3f, 0.0f, 1.0f),
                reader.readFloat("crushingWheelKillDropXpScale", 0.34f, 0.0f, 1.0f),
                CEIStressConfig.load(reader.section("stress")));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("deployerKillDropXp", deployerKillDropXp);
        json.addProperty("deployerKillXpScale", deployerKillXpScale);
        json.addProperty("deployerMineDropXp", deployerMineDropXp);
        json.addProperty("deployerMineXpScale", deployerMineXpScale);
        json.addProperty("deployerCollectXp", deployerCollectXp);
        json.addProperty("deployerMendItem", deployerMendItem);
        json.addProperty("deployerSweepAttack", deployerSweepAttack);
        json.addProperty("crushingWheelKillDropXp", crushingWheelKillDropXp);
        json.addProperty("crushingWheelKillDropXpChance", crushingWheelKillDropXpChance);
        json.addProperty("crushingWheelKillDropXpScale", crushingWheelKillDropXpScale);
        json.add("stress", stress.toJson());
        return json;
    }

    public boolean deployerKillDropXp() {
        return deployerKillDropXp;
    }

    public float deployerKillXpScale() {
        return deployerKillXpScale;
    }

    public boolean deployerMineDropXp() {
        return deployerMineDropXp;
    }

    public float deployerMineXpScale() {
        return deployerMineXpScale;
    }

    public boolean deployerCollectXp() {
        return deployerCollectXp;
    }

    public boolean deployerMendItem() {
        return deployerMendItem;
    }

    public boolean deployerSweepAttack() {
        return deployerSweepAttack;
    }

    public boolean crushingWheelKillDropXp() {
        return crushingWheelKillDropXp;
    }

    public float crushingWheelKillDropXpChance() {
        return crushingWheelKillDropXpChance;
    }

    public float crushingWheelKillDropXpScale() {
        return crushingWheelKillDropXpScale;
    }

    public CEIStressConfig stress() {
        return stress;
    }
}
