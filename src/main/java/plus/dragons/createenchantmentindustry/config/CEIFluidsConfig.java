package plus.dragons.createenchantmentindustry.config;

import com.google.gson.JsonObject;

public final class CEIFluidsConfig {
    private final boolean experienceVaporizeOnPlacement;
    private final int printerFluidCapacity;
    private final boolean enableWrittenBookPrinting;
    private final boolean enableEnchantedBookPrinting;
    private final boolean enableCreateCopiableItemPrinting;
    private final boolean enablePackagePatternPrinting;
    private final boolean enablePackageAddressPrinting;
    private final boolean enableCustomNamePrinting;
    private final boolean enableBannerPatternPrinting;
    private final boolean printingCustomNameAsItemName;
    private final int printingGenerationChange;
    private final float printingEnchantedBookCostMultiplier;
    private final boolean printingEnchantedBookDenylistStopCopying;
    private final int blazeEnchanterFluidCapacity;
    private final int blazeForgerFluidCapacity;
    private final int experienceLanternFluidCapacity;
    private final int experienceLanternDrainRate;
    private final boolean experienceLanternPullToggle;
    private final int experienceLanternPullRadius;
    private final float experienceLanternPullForceMultiplier;
    private final boolean experienceLanternDrainMaidExperience;
    private final int mechanicalGrindstoneFluidCapacity;

    private CEIFluidsConfig(
            boolean experienceVaporizeOnPlacement,
            int printerFluidCapacity,
            boolean enableWrittenBookPrinting,
            boolean enableEnchantedBookPrinting,
            boolean enableCreateCopiableItemPrinting,
            boolean enablePackagePatternPrinting,
            boolean enablePackageAddressPrinting,
            boolean enableCustomNamePrinting,
            boolean enableBannerPatternPrinting,
            boolean printingCustomNameAsItemName,
            int printingGenerationChange,
            float printingEnchantedBookCostMultiplier,
            boolean printingEnchantedBookDenylistStopCopying,
            int blazeEnchanterFluidCapacity,
            int blazeForgerFluidCapacity,
            int experienceLanternFluidCapacity,
            int experienceLanternDrainRate,
            boolean experienceLanternPullToggle,
            int experienceLanternPullRadius,
            float experienceLanternPullForceMultiplier,
            boolean experienceLanternDrainMaidExperience,
            int mechanicalGrindstoneFluidCapacity) {
        this.experienceVaporizeOnPlacement = experienceVaporizeOnPlacement;
        this.printerFluidCapacity = printerFluidCapacity;
        this.enableWrittenBookPrinting = enableWrittenBookPrinting;
        this.enableEnchantedBookPrinting = enableEnchantedBookPrinting;
        this.enableCreateCopiableItemPrinting = enableCreateCopiableItemPrinting;
        this.enablePackagePatternPrinting = enablePackagePatternPrinting;
        this.enablePackageAddressPrinting = enablePackageAddressPrinting;
        this.enableCustomNamePrinting = enableCustomNamePrinting;
        this.enableBannerPatternPrinting = enableBannerPatternPrinting;
        this.printingCustomNameAsItemName = printingCustomNameAsItemName;
        this.printingGenerationChange = printingGenerationChange;
        this.printingEnchantedBookCostMultiplier = printingEnchantedBookCostMultiplier;
        this.printingEnchantedBookDenylistStopCopying = printingEnchantedBookDenylistStopCopying;
        this.blazeEnchanterFluidCapacity = blazeEnchanterFluidCapacity;
        this.blazeForgerFluidCapacity = blazeForgerFluidCapacity;
        this.experienceLanternFluidCapacity = experienceLanternFluidCapacity;
        this.experienceLanternDrainRate = experienceLanternDrainRate;
        this.experienceLanternPullToggle = experienceLanternPullToggle;
        this.experienceLanternPullRadius = experienceLanternPullRadius;
        this.experienceLanternPullForceMultiplier = experienceLanternPullForceMultiplier;
        this.experienceLanternDrainMaidExperience = experienceLanternDrainMaidExperience;
        this.mechanicalGrindstoneFluidCapacity = mechanicalGrindstoneFluidCapacity;
    }

    static CEIFluidsConfig defaults() {
        return new CEIFluidsConfig(true, 4000, true, true, true, true, true, true, true, false, -3, 1.0f, true, 4000, 4000, 1000, 50, true, 10, 0.075f, true, 1000);
    }

    static CEIFluidsConfig load(CEIConfig.ConfigReader reader) {
        return new CEIFluidsConfig(
                reader.readBoolean("experienceVaporizeOnPlacement", true),
                reader.readInt("printerFluidCapacity", 4000, 1000, Integer.MAX_VALUE),
                reader.readBoolean("enableWrittenBookPrinting", true),
                reader.readBoolean("enableEnchantedBookPrinting", true),
                reader.readBoolean("enableCreateCopiableItemPrinting", true),
                reader.readBoolean("enablePackagePatternPrinting", true),
                reader.readBoolean("enablePackageAddressPrinting", true),
                reader.readBoolean("enableCustomNamePrinting", true),
                reader.readBoolean("enableBannerPatternPrinting", true),
                reader.readBoolean("printingCustomNameAsItemName", false),
                reader.readInt("printingGenerationChange", -3, -3, 1),
                reader.readFloat("printingEnchantedBookCostMultiplier", 1.0f, 0.01f, 100.0f),
                reader.readBoolean("printingEnchantedBookDenylistStopCopying", true),
                reader.readInt("blazeEnchanterFluidCapacity", 4000, 1000, Integer.MAX_VALUE),
                reader.readInt("blazeForgerFluidCapacity", 4000, 1000, Integer.MAX_VALUE),
                reader.readInt("experienceLanternFluidCapacity", 1000, 100, Integer.MAX_VALUE),
                reader.readInt("experienceLanternDrainRate", 50, 1, Integer.MAX_VALUE),
                reader.readBoolean("experienceLanternPullToggle", true),
                reader.readInt("experienceLanternPullRadius", 10, 0, Integer.MAX_VALUE),
                reader.readFloat("experienceLanternPullForceMultiplier", 0.075f, 0.0f, 0.5f),
                reader.readBoolean("experienceLanternDrainMaidExperience", true),
                reader.readInt("mechanicalGrindstoneFluidCapacity", 1000, 1, Integer.MAX_VALUE));
    }

    JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("experienceVaporizeOnPlacement", experienceVaporizeOnPlacement);
        json.addProperty("printerFluidCapacity", printerFluidCapacity);
        json.addProperty("enableWrittenBookPrinting", enableWrittenBookPrinting);
        json.addProperty("enableEnchantedBookPrinting", enableEnchantedBookPrinting);
        json.addProperty("enableCreateCopiableItemPrinting", enableCreateCopiableItemPrinting);
        json.addProperty("enablePackagePatternPrinting", enablePackagePatternPrinting);
        json.addProperty("enablePackageAddressPrinting", enablePackageAddressPrinting);
        json.addProperty("enableCustomNamePrinting", enableCustomNamePrinting);
        json.addProperty("enableBannerPatternPrinting", enableBannerPatternPrinting);
        json.addProperty("printingCustomNameAsItemName", printingCustomNameAsItemName);
        json.addProperty("printingGenerationChange", printingGenerationChange);
        json.addProperty("printingEnchantedBookCostMultiplier", printingEnchantedBookCostMultiplier);
        json.addProperty("printingEnchantedBookDenylistStopCopying", printingEnchantedBookDenylistStopCopying);
        json.addProperty("blazeEnchanterFluidCapacity", blazeEnchanterFluidCapacity);
        json.addProperty("blazeForgerFluidCapacity", blazeForgerFluidCapacity);
        json.addProperty("experienceLanternFluidCapacity", experienceLanternFluidCapacity);
        json.addProperty("experienceLanternDrainRate", experienceLanternDrainRate);
        json.addProperty("experienceLanternPullToggle", experienceLanternPullToggle);
        json.addProperty("experienceLanternPullRadius", experienceLanternPullRadius);
        json.addProperty("experienceLanternPullForceMultiplier", experienceLanternPullForceMultiplier);
        json.addProperty("experienceLanternDrainMaidExperience", experienceLanternDrainMaidExperience);
        json.addProperty("mechanicalGrindstoneFluidCapacity", mechanicalGrindstoneFluidCapacity);
        return json;
    }

    public boolean experienceVaporizeOnPlacement() {
        return experienceVaporizeOnPlacement;
    }

    public int printerFluidCapacity() {
        return printerFluidCapacity;
    }

    public boolean enableWrittenBookPrinting() {
        return enableWrittenBookPrinting;
    }

    public boolean enableEnchantedBookPrinting() {
        return enableEnchantedBookPrinting;
    }

    public boolean enableCreateCopiableItemPrinting() {
        return enableCreateCopiableItemPrinting;
    }

    public boolean enablePackagePatternPrinting() {
        return enablePackagePatternPrinting;
    }

    public boolean enablePackageAddressPrinting() {
        return enablePackageAddressPrinting;
    }

    public boolean enableCustomNamePrinting() {
        return enableCustomNamePrinting;
    }

    public boolean enableBannerPatternPrinting() {
        return enableBannerPatternPrinting;
    }

    public boolean printingCustomNameAsItemName() {
        return printingCustomNameAsItemName;
    }

    public int printingGenerationChange() {
        return printingGenerationChange;
    }

    public float printingEnchantedBookCostMultiplier() {
        return printingEnchantedBookCostMultiplier;
    }

    public boolean printingEnchantedBookDenylistStopCopying() {
        return printingEnchantedBookDenylistStopCopying;
    }

    public int blazeEnchanterFluidCapacity() {
        return blazeEnchanterFluidCapacity;
    }

    public int blazeForgerFluidCapacity() {
        return blazeForgerFluidCapacity;
    }

    public int experienceLanternFluidCapacity() {
        return experienceLanternFluidCapacity;
    }

    public int experienceLanternDrainRate() {
        return experienceLanternDrainRate;
    }

    public boolean experienceLanternPullToggle() {
        return experienceLanternPullToggle;
    }

    public int experienceLanternPullRadius() {
        return experienceLanternPullRadius;
    }

    public float experienceLanternPullForceMultiplier() {
        return experienceLanternPullForceMultiplier;
    }

    public boolean experienceLanternDrainMaidExperience() {
        return experienceLanternDrainMaidExperience;
    }

    public int mechanicalGrindstoneFluidCapacity() {
        return mechanicalGrindstoneFluidCapacity;
    }
}
