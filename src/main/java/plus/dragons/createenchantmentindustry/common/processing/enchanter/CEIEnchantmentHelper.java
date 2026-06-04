package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.core.component.DataComponents;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;

public final class CEIEnchantmentHelper {
    private CEIEnchantmentHelper() {
    }

    public static int getEnchantmentCost(Holder<Enchantment> holder, int level) {
        Enchantment enchantment = holder.value();
        int cost = ExperienceHatchBehaviour.getExperienceForNextLevel(enchantment.getMinCost(level));
        if (level == 1) {
            return cost;
        }
        return cost + getEnchantmentCost(holder, level - 1);
    }

    public static int getEnchantmentCost(ItemEnchantments enchantments) {
        return enchantments.entrySet().stream()
                .mapToInt(entry -> getEnchantmentCost(entry.getKey(), entry.getIntValue()))
                .sum();
    }

    public static int getAdjustedLevel(ItemStack stack, int level) {
        int adjusted = level;
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        if (enchantable != null && enchantable.value() > 0) {
            adjusted += 1 + enchantable.value() / 4;
        }
        adjusted = Mth.clamp(Math.round(adjusted + adjusted * 0.15F), 1, Integer.MAX_VALUE);
        return adjusted;
    }

    public static List<EnchantmentInstance> getAvailableEnchantmentResults(
            int level,
            Stream<Holder<Enchantment>> possibleEnchantments) {
        List<EnchantmentInstance> available = new ArrayList<>();
        possibleEnchantments.forEach(holder -> {
            Enchantment enchantment = holder.value();
            for (int enchantmentLevel = enchantment.getMaxLevel(); enchantmentLevel >= enchantment.getMinLevel(); enchantmentLevel--) {
                if (level >= enchantment.getMinCost(enchantmentLevel) && level <= enchantment.getMaxCost(enchantmentLevel)) {
                    available.add(new EnchantmentInstance(holder, enchantmentLevel));
                    break;
                }
            }
        });
        return available;
    }

    public static List<EnchantmentInstance> selectEnchantments(
            RandomSource random,
            int adjustedLevel,
            List<EnchantmentInstance> available) {
        List<EnchantmentInstance> remaining = new ArrayList<>(available);
        List<EnchantmentInstance> selected = new ArrayList<>();
        if (!remaining.isEmpty()) {
            selected.add(remaining.get(random.nextInt(remaining.size())));
        }
        while (random.nextInt(50) <= adjustedLevel) {
            if (!selected.isEmpty()) {
                EnchantmentHelper.filterCompatibleEnchantments(remaining, selected.getLast());
            }
            if (remaining.isEmpty()) {
                break;
            }
            selected.add(remaining.get(random.nextInt(remaining.size())));
            adjustedLevel /= 2;
        }
        return selected;
    }
}
