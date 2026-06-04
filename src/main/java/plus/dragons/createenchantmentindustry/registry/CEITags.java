package plus.dragons.createenchantmentindustry.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import plus.dragons.createenchantmentindustry.foundation.tag.CEITagHelper;

public final class CEITags {
    public static final TagKey<Fluid> EXPERIENCE_FLUIDS = fluid("experience");
    public static final TagKey<Enchantment> BLAZE_ENCHANTER_ENCHANTING = enchantment("blaze_enchanter/enchanting");
    public static final TagKey<Enchantment> BLAZE_ENCHANTER_SUPER_ENCHANTING = enchantment("blaze_enchanter/super_enchanting");
    public static final TagKey<Enchantment> BLAZE_ENCHANTER_DENY = enchantment("blaze_enchanter/deny");

    private CEITags() {
    }

    public static TagKey<Block> block(String path) {
        return TagKey.create(Registries.BLOCK, CEIIdentifier.id(path));
    }

    public static TagKey<Item> item(String path) {
        return TagKey.create(Registries.ITEM, CEIIdentifier.id(path));
    }

    public static TagKey<Fluid> fluid(String path) {
        return TagKey.create(Registries.FLUID, CEIIdentifier.id(path));
    }

    public static TagKey<Enchantment> enchantment(String path) {
        return TagKey.create(Registries.ENCHANTMENT, CEIIdentifier.id(path));
    }

    public static TagKey<Block> commonBlock(String path) {
        return CEITagHelper.common(Registries.BLOCK, path);
    }

    public static TagKey<Item> commonItem(String path) {
        return CEITagHelper.common(Registries.ITEM, path);
    }

    public static TagKey<Fluid> commonFluid(String path) {
        return CEITagHelper.common(Registries.FLUID, path);
    }

    public static void register() {
    }
}
