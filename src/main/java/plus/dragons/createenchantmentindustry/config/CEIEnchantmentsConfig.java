package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIEnchantmentsConfig {
    private final int blazeEnchanterMaxEnchantLevel;
    private final int blazeEnchanterMaxSuperEnchantLevel;
    private final int enchantmentMaxLevelExtension;
    private final boolean ignoreEnchantmentCompatibility;
    private final boolean splitEnchantmentRespectLevelExtension;

    private CEIEnchantmentsConfig(
            int blazeEnchanterMaxEnchantLevel,
            int blazeEnchanterMaxSuperEnchantLevel,
            int enchantmentMaxLevelExtension,
            boolean ignoreEnchantmentCompatibility,
            boolean splitEnchantmentRespectLevelExtension) {
        this.blazeEnchanterMaxEnchantLevel = blazeEnchanterMaxEnchantLevel;
        this.blazeEnchanterMaxSuperEnchantLevel = blazeEnchanterMaxSuperEnchantLevel;
        this.enchantmentMaxLevelExtension = enchantmentMaxLevelExtension;
        this.ignoreEnchantmentCompatibility = ignoreEnchantmentCompatibility;
        this.splitEnchantmentRespectLevelExtension = splitEnchantmentRespectLevelExtension;
    }

    static CEIEnchantmentsConfig defaults() {
        return new CEIEnchantmentsConfig(30, 60, 1, true, false);
    }

    static CEIEnchantmentsConfig load(CEIConfig.ConfigReader reader) {
        return new CEIEnchantmentsConfig(
                reader.readInt("blazeEnchanterMaxEnchantLevel", 30, 0, Integer.MAX_VALUE),
                reader.readInt("blazeEnchanterMaxSuperEnchantLevel", 60, 0, Integer.MAX_VALUE),
                reader.readInt("enchantmentMaxLevelExtension", 1, 0, 255),
                reader.readBoolean("ignoreEnchantmentCompatibility", true),
                reader.readBoolean("splitEnchantmentRespectLevelExtension", false));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("blazeEnchanterMaxEnchantLevel", blazeEnchanterMaxEnchantLevel);
        json.addProperty("blazeEnchanterMaxSuperEnchantLevel", blazeEnchanterMaxSuperEnchantLevel);
        json.addProperty("enchantmentMaxLevelExtension", enchantmentMaxLevelExtension);
        json.addProperty("ignoreEnchantmentCompatibility", ignoreEnchantmentCompatibility);
        json.addProperty("splitEnchantmentRespectLevelExtension", splitEnchantmentRespectLevelExtension);
        return json;
    }

    public int blazeEnchanterMaxEnchantLevel() {
        return blazeEnchanterMaxEnchantLevel;
    }

    public int blazeEnchanterMaxSuperEnchantLevel() {
        return blazeEnchanterMaxSuperEnchantLevel;
    }

    public int enchantmentMaxLevelExtension() {
        return enchantmentMaxLevelExtension;
    }

    public boolean ignoreEnchantmentCompatibility() {
        return ignoreEnchantmentCompatibility;
    }

    public boolean splitEnchantmentRespectLevelExtension() {
        return splitEnchantmentRespectLevelExtension;
    }
}
