package plus.dragons.createenchantmentindustry.common.processing.forger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class BlazeForgerSmokeHarness {
    private BlazeForgerSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: BlazeForgerSmokeHarness <valid|invalid> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        bindItemComponents(Items.DIAMOND_SWORD, Items.ENCHANTED_BOOK, Items.BOOK, Items.STONE);
        List<String> lines = switch (args[0]) {
            case "valid" -> validSmoke();
            case "invalid" -> invalidSmoke();
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Path path = Path.of(args[1]);
        Files.createDirectories(path.getParent());
        Files.write(path, lines);
        boolean passed = lines.stream().anyMatch(line -> line.endsWith("_pass=true"));
        if (!passed) {
            throw new IllegalStateException("Blaze Forger smoke did not pass: " + lines);
        }
    }

    private static List<String> validSmoke() {
        Holder<Enchantment> enchantment = Holder.direct(smokeEnchantment("forger_valid_smoke", Items.DIAMOND_SWORD));
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack book = enchantedBook(enchantment, 1);
        BlazeForgerInventory inventory = new BlazeForgerInventory();
        ItemStack firstRemainder = inventory.insertItem(sword.copy(), false);
        ItemStack secondRemainder = inventory.insertItem(book.copy(), false);
        BlazeForgerInventory.ForgeResult direct = BlazeForgerInventory.forge(sword, book, false);
        int cost = inventory.getExperienceCost();
        ItemStack preview = inventory.getPreview(0);
        boolean applied = inventory.applyResult();
        ItemStack output = inventory.extractItem(2, 1, false);
        ItemEnchantments outputEnchantments = output.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        List<String> lines = new ArrayList<>();
        lines.add("mode=valid");
        lines.add("input0=minecraft:diamond_sword");
        lines.add("input1=minecraft:enchanted_book");
        lines.add("first_remainder_count=" + firstRemainder.getCount());
        lines.add("second_remainder_count=" + secondRemainder.getCount());
        lines.add("direct_result_valid=" + direct.valid());
        lines.add("inventory_cost=" + cost);
        lines.add("preview_item=" + itemName(preview));
        lines.add("applied=" + applied);
        lines.add("output_item=" + itemName(output));
        lines.add("output_enchantment_count=" + outputEnchantments.size());
        lines.add("output_has_smoke_enchantment=" + (outputEnchantments.getLevel(enchantment) == 1));
        lines.add("valid_forging_pass=" + (firstRemainder.isEmpty()
                && secondRemainder.isEmpty()
                && direct.valid()
                && cost > 0
                && applied
                && output.is(Items.DIAMOND_SWORD)
                && outputEnchantments.getLevel(enchantment) == 1));
        return lines;
    }

    private static List<String> invalidSmoke() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        ItemStack stone = new ItemStack(Items.STONE);
        BlazeForgerInventory inventory = new BlazeForgerInventory();
        ItemStack firstRemainder = inventory.insertItem(sword.copy(), false);
        ItemStack secondRemainder = inventory.insertItem(stone.copy(), false);
        BlazeForgerInventory.ForgeResult direct = BlazeForgerInventory.forge(sword, stone, false);
        int cost = inventory.getExperienceCost();
        boolean applied = inventory.applyResult();
        ItemStack extractedInput = inventory.extractItem(0, 1, false);
        ItemStack extractedOutput = inventory.extractItem(2, 1, false);
        List<String> lines = new ArrayList<>();
        lines.add("mode=invalid");
        lines.add("input0=minecraft:diamond_sword");
        lines.add("input1=minecraft:stone");
        lines.add("first_remainder_count=" + firstRemainder.getCount());
        lines.add("second_remainder_count=" + secondRemainder.getCount());
        lines.add("direct_result_valid=" + direct.valid());
        lines.add("inventory_cost=" + cost);
        lines.add("applied=" + applied);
        lines.add("extracted_input_item=" + itemName(extractedInput));
        lines.add("extracted_output_empty=" + extractedOutput.isEmpty());
        lines.add("invalid_forging_pass=" + (firstRemainder.isEmpty()
                && ItemStack.isSameItemSameComponents(stone, secondRemainder)
                && secondRemainder.getCount() == stone.getCount()
                && !direct.valid()
                && cost == 0
                && !applied
                && extractedInput.is(Items.DIAMOND_SWORD)
                && extractedOutput.isEmpty()));
        return lines;
    }

    private static ItemStack enchantedBook(Holder<Enchantment> enchantment, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantment, level);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
        return book;
    }

    private static Enchantment smokeEnchantment(String path, Item item) {
        return new Enchantment(
                Component.literal(path),
                Enchantment.definition(
                        HolderSet.direct(item.builtInRegistryHolder()),
                        1,
                        1,
                        Enchantment.constantCost(1),
                        Enchantment.constantCost(30),
                        1,
                        EquipmentSlotGroup.ANY),
                HolderSet.empty(),
                DataComponentMap.EMPTY);
    }

    private static void bindItemComponents(Item... items) {
        for (Item item : items) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            }
        }
    }

    private static String itemName(ItemStack stack) {
        if (stack.isEmpty()) {
            return "minecraft:air";
        }
        return stack.getItem().builtInRegistryHolder().key().identifier().toString();
    }
}
