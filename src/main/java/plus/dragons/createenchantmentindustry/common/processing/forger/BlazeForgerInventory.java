package plus.dragons.createenchantmentindustry.common.processing.forger;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.EnchantingTemplateItem;

public class BlazeForgerInventory {
    private ItemStack input0 = ItemStack.EMPTY;
    private ItemStack input1 = ItemStack.EMPTY;
    private ItemStack output0 = ItemStack.EMPTY;
    private ItemStack output1 = ItemStack.EMPTY;
    private ItemStack result0 = ItemStack.EMPTY;
    private ItemStack result1 = ItemStack.EMPTY;
    private int costLevels;
    private int mode;
    private boolean special;

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        if (this.special == special) {
            return;
        }
        this.special = special;
        updateResult();
    }

    public int getCostLevels() {
        return costLevels;
    }

    public int getExperienceCost() {
        return costLevels <= 0 ? 0 : ExperienceHatchBehaviour.getExperienceForTotalLevel(costLevels);
    }

    public int getMode() {
        return mode;
    }

    public boolean hasRemainingOutput() {
        return !output0.isEmpty() || !output1.isEmpty();
    }

    public boolean hasValidResult() {
        return costLevels > 0 && !result0.isEmpty();
    }

    public ItemStack getInput(int slot) {
        return switch (slot) {
            case 0 -> input0;
            case 1 -> input1;
            default -> throw new IllegalArgumentException("Input slot out of range: " + slot);
        };
    }

    public ItemStack getOutput(int slot) {
        return switch (slot) {
            case 0 -> output0;
            case 1 -> output1;
            default -> throw new IllegalArgumentException("Output slot out of range: " + slot);
        };
    }

    public ItemStack getPreview(int slot) {
        return switch (slot) {
            case 0 -> result0;
            case 1 -> result1;
            default -> throw new IllegalArgumentException("Preview slot out of range: " + slot);
        };
    }

    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || hasRemainingOutput()) {
            return stack;
        }
        if (input0.isEmpty()) {
            if (!isPotentialInput(stack)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            ItemStack inserted = remainder.split(1);
            if (!simulate) {
                input0 = inserted;
                updateResult();
            }
            return remainder;
        }
        if (input1.isEmpty()) {
            ItemStack inserted = stack.copyWithCount(1);
            if (!canAcceptSecondInput(inserted)) {
                return stack;
            }
            ItemStack remainder = stack.copy();
            inserted = remainder.split(1);
            if (!simulate) {
                input1 = inserted;
                updateResult();
            }
            return remainder;
        }
        return stack;
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = switch (slot) {
            case 0 -> input0;
            case 1 -> input1;
            case 2 -> output0;
            case 3 -> output1;
            default -> throw new IllegalArgumentException("Slot out of range: " + slot);
        };
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = stack.copyWithCount(Math.min(amount, stack.getCount()));
        if (!simulate) {
            ItemStack remainder = stack.copy();
            remainder.shrink(extracted.getCount());
            if (remainder.isEmpty()) {
                remainder = ItemStack.EMPTY;
            }
            switch (slot) {
                case 0 -> input0 = remainder;
                case 1 -> input1 = remainder;
                case 2 -> output0 = remainder;
                case 3 -> output1 = remainder;
                default -> throw new IllegalArgumentException("Slot out of range: " + slot);
            }
            if (slot < 2) {
                updateResult();
            }
        }
        return extracted;
    }

    public ItemStack extractFirstAvailable(boolean simulate) {
        for (int slot = 3; slot >= 0; slot--) {
            ItemStack extracted = extractItem(slot, 1, simulate);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean applyResult() {
        if (!hasValidResult() || hasRemainingOutput()) {
            return false;
        }
        output0 = result0.copy();
        output1 = result1.copy();
        input0 = ItemStack.EMPTY;
        input1 = ItemStack.EMPTY;
        result0 = ItemStack.EMPTY;
        result1 = ItemStack.EMPTY;
        costLevels = 0;
        return true;
    }

    public void clear() {
        input0 = ItemStack.EMPTY;
        input1 = ItemStack.EMPTY;
        output0 = ItemStack.EMPTY;
        output1 = ItemStack.EMPTY;
        result0 = ItemStack.EMPTY;
        result1 = ItemStack.EMPTY;
        costLevels = 0;
        mode = 0;
    }

    public void updateResult() {
        result0 = input0.copy();
        result1 = input1.copy();
        costLevels = 0;
        mode = 0;
        if (input0.isEmpty() || input1.isEmpty()) {
            result0 = ItemStack.EMPTY;
            result1 = ItemStack.EMPTY;
            return;
        }
        ForgeResult result = forge(input0, input1, special);
        if (!result.valid()) {
            result0 = ItemStack.EMPTY;
            result1 = ItemStack.EMPTY;
            return;
        }
        result0 = result.primary();
        result1 = result.secondary();
        costLevels = result.costLevels();
        mode = result.mode();
    }

    public void write(ValueOutput view) {
        view.store("Input0", ItemStack.OPTIONAL_CODEC, input0);
        view.store("Input1", ItemStack.OPTIONAL_CODEC, input1);
        view.store("Output0", ItemStack.OPTIONAL_CODEC, output0);
        view.store("Output1", ItemStack.OPTIONAL_CODEC, output1);
        view.putInt("CostLevels", costLevels);
        view.putInt("Mode", mode);
        view.putBoolean("Special", special);
    }

    public void read(ValueInput view) {
        input0 = view.read("Input0", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        input1 = view.read("Input1", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        output0 = view.read("Output0", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        output1 = view.read("Output1", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        costLevels = view.getIntOr("CostLevels", 0);
        mode = view.getIntOr("Mode", 0);
        special = view.getBooleanOr("Special", false);
        updateResult();
    }

    public static ForgeResult forge(ItemStack baseStack, ItemStack additionStack, boolean special) {
        ItemStack base = baseStack.copyWithCount(1);
        ItemStack addition = additionStack.copyWithCount(1);
        if (base.isEmpty() || addition.isEmpty()) {
            return ForgeResult.invalid();
        }
        boolean additionTemplate = addition.getItem() instanceof EnchantingTemplateItem;
        boolean additionBook = addition.is(Items.ENCHANTED_BOOK);
        if (base.is(Items.BOOK) && (additionTemplate || additionBook)) {
            ItemEnchantments additionEnchantments = enchantments(addition);
            if (!additionEnchantments.isEmpty()) {
                ItemStack book = Items.ENCHANTED_BOOK.getDefaultInstance();
                EnchantmentHelper.setEnchantments(book, additionEnchantments);
                int cost = costForApplying(additionEnchantments, special);
                return ForgeResult.valid(book, ItemStack.EMPTY, cost, 1);
            }
        }
        if (additionTemplate || additionBook) {
            ItemEnchantments additionEnchantments = enchantments(addition);
            if (!additionEnchantments.isEmpty() && applyEnchantments(base, enchantments(base), additionEnchantments, special)) {
                applyRepairCost(base, addition, special);
                int cost = costForApplying(additionEnchantments, special);
                return ForgeResult.valid(base, ItemStack.EMPTY, cost, 1);
            }
        }
        if (ItemStack.isSameItem(base, addition)) {
            ItemEnchantments additionEnchantments = enchantments(addition);
            boolean repaired = repair(base, addition);
            boolean enchanted = applyEnchantments(base, enchantments(base), additionEnchantments, special);
            if (repaired || enchanted) {
                applyRepairCost(base, addition, special);
                int cost = (repaired ? 2 : 0) + costForApplying(additionEnchantments, special);
                return ForgeResult.valid(base, ItemStack.EMPTY, Math.max(1, cost), 0);
            }
        }
        return ForgeResult.invalid();
    }

    private static boolean isPotentialInput(ItemStack stack) {
        return !stack.isEmpty();
    }

    private boolean canAcceptSecondInput(ItemStack stack) {
        if (!isPotentialInput(stack)) {
            return false;
        }
        ForgeResult result = forge(input0, stack, special);
        return result.valid();
    }

    private static ItemEnchantments enchantments(ItemStack stack) {
        ItemEnchantments stored = stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (!stored.isEmpty()) {
            return stored;
        }
        return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    private static boolean applyEnchantments(
            ItemStack base,
            ItemEnchantments baseEnchantments,
            ItemEnchantments additionEnchantments,
            boolean special) {
        if (additionEnchantments.isEmpty()) {
            return false;
        }
        ItemEnchantments.Mutable resultEnchantments = new ItemEnchantments.Mutable(baseEnchantments);
        boolean applied = false;
        for (var entry : additionEnchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            int baseLevel = resultEnchantments.getLevel(holder);
            int additionLevel = entry.getIntValue();
            int resultLevel = baseLevel == additionLevel ? additionLevel + 1 : Math.max(additionLevel, baseLevel);
            boolean applicable = holder.value().canEnchant(base);
            for (Holder<Enchantment> existing : resultEnchantments.keySet()) {
                if (!existing.equals(holder) && !Enchantment.areCompatible(holder, existing)) {
                    applicable = special;
                }
            }
            if (!applicable) {
                continue;
            }
            applied = true;
            resultEnchantments.set(holder, Math.min(resultLevel, holder.value().getMaxLevel()));
        }
        if (!applied) {
            return false;
        }
        EnchantmentHelper.setEnchantments(base, resultEnchantments.toImmutable());
        return true;
    }

    private static boolean repair(ItemStack base, ItemStack addition) {
        if (!base.isDamaged()) {
            return false;
        }
        int baseDurability = base.getMaxDamage() - base.getDamageValue();
        int additionDurability = addition.getMaxDamage() - addition.getDamageValue();
        int repairedDamage = base.getMaxDamage() - (baseDurability + additionDurability + base.getMaxDamage() * 12 / 100);
        repairedDamage = Math.max(0, repairedDamage);
        if (repairedDamage >= base.getDamageValue()) {
            return false;
        }
        base.setDamageValue(repairedDamage);
        return true;
    }

    private static int costForApplying(ItemEnchantments enchantments, boolean special) {
        int cost = 0;
        for (var entry : enchantments.entrySet()) {
            int anvilCost = Math.max(1, entry.getKey().value().getAnvilCost() / 2);
            cost += anvilCost * entry.getIntValue();
        }
        return Math.max(1, special ? cost * 2 : cost);
    }

    private static void applyRepairCost(ItemStack base, ItemStack addition, boolean special) {
        if (!special) {
            return;
        }
        int baseCost = base.getOrDefault(DataComponents.REPAIR_COST, 0);
        int additionCost = addition.getOrDefault(DataComponents.REPAIR_COST, 0);
        base.set(DataComponents.REPAIR_COST, AnvilMenu.calculateIncreasedRepairCost(Math.max(baseCost, additionCost)));
    }

    public record ForgeResult(boolean valid, ItemStack primary, ItemStack secondary, int costLevels, int mode) {
        public static ForgeResult invalid() {
            return new ForgeResult(false, ItemStack.EMPTY, ItemStack.EMPTY, 0, 0);
        }

        public static ForgeResult valid(ItemStack primary, ItemStack secondary, int costLevels, int mode) {
            return new ForgeResult(true, primary, secondary, Math.max(1, costLevels), mode);
        }
    }
}
