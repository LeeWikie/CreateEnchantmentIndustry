package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import net.minecraft.world.item.Item;

public class EnchantingTemplateItem extends Item {
    private final boolean special;

    public EnchantingTemplateItem(Properties properties, boolean special) {
        super(properties);
        this.special = special;
    }

    public static EnchantingTemplateItem normal(Properties properties) {
        return new EnchantingTemplateItem(properties, false);
    }

    public static EnchantingTemplateItem special(Properties properties) {
        return new EnchantingTemplateItem(properties, true);
    }

    public boolean isSpecial() {
        return special;
    }

}
