package plus.dragons.createenchantmentindustry.registry;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.MechanicalGrindStoneItem;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;

public final class CEIItems {
    public static final Item SUPER_EXPERIENCE_NUGGET = register("super_experience_nugget");
    public static final Item ENCHANTING_TEMPLATE = register("enchanting_template", EnchantingTemplateItem::normal, new Item.Properties().stacksTo(16));
    public static final Item SUPER_ENCHANTING_TEMPLATE = register("super_enchanting_template", EnchantingTemplateItem::special, new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    public static final BucketItem EXPERIENCE_BUCKET = registerExperienceBucket();
    public static final Item EXPERIENCE_CAKE_BASE = register("experience_cake_base");
    public static final Item EXPERIENCE_CAKE = register("experience_cake");
    public static final Item EXPERIENCE_CAKE_SLICE = register("experience_cake_slice");
    public static final BlockItem SUPER_EXPERIENCE_BLOCK_ITEM = registerBlockItem(CEIBlocks.SUPER_EXPERIENCE_BLOCK);
    public static final BlockItem EXPERIENCE_HATCH_ITEM = registerBlockItem(CEIBlocks.EXPERIENCE_HATCH);
    public static final BlockItem EXPERIENCE_LANTERN_ITEM = registerBlockItem(CEIBlocks.EXPERIENCE_LANTERN);
    public static final BlockItem PRINTER_ITEM = registerBlockItem(CEIBlocks.PRINTER);
    public static final BlockItem MECHANICAL_GRINDSTONE_ITEM = registerBlockItem(CEIBlocks.MECHANICAL_GRINDSTONE, MechanicalGrindStoneItem::new, new Item.Properties());
    public static final BlockItem GRINDSTONE_DRAIN_ITEM = registerBlockItem(CEIBlocks.GRINDSTONE_DRAIN);
    public static final BlockItem BLAZE_ENCHANTER_ITEM = registerBlockItem(CEIBlocks.BLAZE_ENCHANTER);
    public static final BlockItem BLAZE_FORGER_ITEM = registerBlockItem(CEIBlocks.BLAZE_FORGER);
    public static final List<ItemLike> BASE_CONTENT = List.of(
            SUPER_EXPERIENCE_NUGGET,
            ENCHANTING_TEMPLATE,
            SUPER_ENCHANTING_TEMPLATE,
            EXPERIENCE_BUCKET,
            EXPERIENCE_CAKE_BASE,
            EXPERIENCE_CAKE,
            EXPERIENCE_CAKE_SLICE,
            SUPER_EXPERIENCE_BLOCK_ITEM,
            EXPERIENCE_HATCH_ITEM,
            EXPERIENCE_LANTERN_ITEM,
            PRINTER_ITEM,
            MECHANICAL_GRINDSTONE_ITEM,
            GRINDSTONE_DRAIN_ITEM,
            BLAZE_ENCHANTER_ITEM,
            BLAZE_FORGER_ITEM);

    private CEIItems() {
    }

    public static Item register(String path) {
        return register(path, Item::new, new Item.Properties());
    }

    public static <T extends Item> T register(String path, Function<Item.Properties, T> factory, Item.Properties properties) {
        return register(CEIIdentifier.id(path), factory, properties);
    }

    public static BlockItem registerBlockItem(Block block) {
        return registerBlockItem(block, BlockItem::new, new Item.Properties());
    }

    @SuppressWarnings("deprecation")
    public static <T extends Block, U extends Item> U registerBlockItem(
            T block,
            BiFunction<T, Item.Properties, U> factory,
            Item.Properties properties) {
        Identifier id = block.builtInRegistryHolder().key().identifier();
        return register(id, itemProperties -> factory.apply(block, itemProperties), properties.useBlockDescriptionPrefix());
    }

    public static <T extends Item> T register(Identifier id, Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = factory.apply(properties.setId(key));
        if (item instanceof BlockItem blockItem) {
            blockItem.registerBlocks(Item.BY_BLOCK, item);
        }
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    private static BucketItem registerExperienceBucket() {
        BucketItem bucket = register(
                "experience_bucket",
                properties -> new BucketItem(CEIFluids.EXPERIENCE, properties),
                new Item.Properties()
                        .craftRemainder(Items.BUCKET)
                        .stacksTo(1)
                        .rarity(Rarity.UNCOMMON)
                        .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
        CEIFluids.EXPERIENCE.getEntry().bucket = bucket;
        return bucket;
    }

    public static void register() {
    }
}
