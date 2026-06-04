package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.config.CEIConfig;
import plus.dragons.createenchantmentindustry.registry.CEITags;

public class EnchanterBehaviour extends BlockEntityBehaviour<BlazeEnchanterBlockEntity> {
    public static final BehaviourType<EnchanterBehaviour> TYPE = new BehaviourType<>("cei_enchanter");
    private static final String LEVEL_KEY = "EnchantingLevel";
    private static final String TEMPLATE_KEY = "EnchantingTemplate";

    private ItemStack template = ItemStack.EMPTY;
    private int enchantingLevel = CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel();
    private List<EnchantmentInstance> enchantments = List.of();

    public EnchanterBehaviour(BlazeEnchanterBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public boolean canProcess(ItemStack stack) {
        Level level = blockEntity.getLevel();
        if (level == null || stack.isEmpty()) {
            return false;
        }
        update(stack);
        return !enchantments.isEmpty() && (template.isEmpty() ? stack.isEnchantable() : canProcessTemplate(stack));
    }

    public void update(ItemStack stack) {
        Level level = blockEntity.getLevel();
        if (level == null || stack.isEmpty()) {
            enchantments = List.of();
            return;
        }
        ItemStack target = template.isEmpty() ? stack : template;
        TagKey<Enchantment> tag = blockEntity.isSpecialEnchanting()
                ? CEITags.BLAZE_ENCHANTER_SUPER_ENCHANTING
                : CEITags.BLAZE_ENCHANTER_ENCHANTING;
        int adjustedLevel = CEIEnchantmentHelper.getAdjustedLevel(target, enchantingLevel);
        Iterable<net.minecraft.core.Holder<Enchantment>> holders = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getTagOrEmpty(tag);
        enchantments = CEIEnchantmentHelper.getAvailableEnchantmentResults(adjustedLevel, filterDenied(StreamSupport.stream(holders.spliterator(), false)
                .filter(holder -> holder.value().canEnchant(target)), EnchanterBehaviour::isDeniedByBlazeEnchanter));
    }

    public ItemStack getResult(ItemStack stack, RandomSource random) {
        if (enchantments.isEmpty()) {
            return stack.copy();
        }
        ItemStack result = stack.copy();
        List<EnchantmentInstance> selected = CEIEnchantmentHelper.selectEnchantments(
                random,
                CEIEnchantmentHelper.getAdjustedLevel(template.isEmpty() ? stack : template, enchantingLevel),
                enchantments);
        if (template.isEmpty() && stack.is(Items.BOOK) && selected.size() > 1) {
            selected.remove(random.nextInt(selected.size()));
        }
        if (!template.isEmpty() && selected.size() > 1) {
            selected.remove(random.nextInt(selected.size()));
        }
        return applySelectedEnchantments(result, selected);
    }

    static Stream<Holder<Enchantment>> filterDenied(
            Stream<Holder<Enchantment>> holders,
            Predicate<Holder<Enchantment>> denied) {
        return holders.filter(denied.negate());
    }

    static boolean isDeniedByBlazeEnchanter(Holder<Enchantment> holder) {
        return holder.is(CEITags.BLAZE_ENCHANTER_DENY);
    }

    static ItemStack applySelectedEnchantments(ItemStack stack, List<EnchantmentInstance> selected) {
        ItemStack result = stack.copy();
        if (result.getItem() instanceof EnchantingTemplateItem) {
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (EnchantmentInstance instance : selected) {
                mutable.set(instance.enchantment(), instance.level());
            }
            result.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());
            return result;
        }
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (EnchantmentInstance instance : selected) {
            mutable.set(instance.enchantment(), instance.level());
        }
        EnchantmentHelper.setEnchantments(result, mutable.toImmutable());
        return result;
    }

    public int getExperienceCost() {
        if (enchantments.isEmpty()) {
            return 0;
        }
        int levelCost = Math.ceilDiv(enchantingLevel, 10);
        int experienceCost = 0;
        for (int index = 0; index < levelCost; index++) {
            experienceCost += ExperienceHatchBehaviour.getExperienceForNextLevel(enchantingLevel - index);
        }
        return experienceCost;
    }

    public ItemStack getTemplate() {
        return template;
    }

    public boolean setTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            template = ItemStack.EMPTY;
        } else if (stack.isEnchantable()) {
            template = stack.copyWithCount(1);
        } else {
            return false;
        }
        update(blockEntity.getHeldItem());
        blockEntity.setChanged();
        blockEntity.sendData();
        return true;
    }

    public int getEnchantingLevel() {
        return enchantingLevel;
    }

    public void setEnchantingLevel(int enchantingLevel) {
        this.enchantingLevel = Mth.clamp(enchantingLevel, 1, blockEntity.getMaxEnchantLevel());
        update(blockEntity.getHeldItem());
        blockEntity.setChanged();
        blockEntity.sendData();
    }

    private boolean canProcessTemplate(ItemStack stack) {
        if (!(stack.getItem() instanceof EnchantingTemplateItem templateItem)) {
            return false;
        }
        if (!stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
            return false;
        }
        return templateItem.isSpecial() == blockEntity.isSpecialEnchanting();
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putInt(LEVEL_KEY, enchantingLevel);
        view.store(TEMPLATE_KEY, ItemStack.OPTIONAL_CODEC, template);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        enchantingLevel = Mth.clamp(view.getIntOr(LEVEL_KEY, CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel()), 0, blockEntity.getMaxEnchantLevel());
        template = view.read(TEMPLATE_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }
}
