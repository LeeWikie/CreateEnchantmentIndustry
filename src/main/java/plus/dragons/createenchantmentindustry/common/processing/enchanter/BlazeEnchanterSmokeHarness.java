package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceHatchBehaviour;
import plus.dragons.createenchantmentindustry.foundation.fluid.CEIConfigurableFluidTank;
import plus.dragons.createenchantmentindustry.registry.CEITags;

public final class BlazeEnchanterSmokeHarness {
    private BlazeEnchanterSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: BlazeEnchanterSmokeHarness <normal|denied> <output-log>");
        }
        SharedConstants.setVersion(DetectedVersion.BUILT_IN);
        Bootstrap.bootStrap();
        EnchantingTemplateItem templateItem = registerSmokeTemplate();
        bindItemComponents(templateItem, Items.DIAMOND_SWORD);
        List<String> lines = switch (args[0]) {
            case "normal" -> normalSmoke(templateItem);
            case "denied" -> deniedSmoke(templateItem);
            default -> throw new IllegalArgumentException("Unknown smoke mode: " + args[0]);
        };
        Files.write(Path.of(args[1]), lines);
    }

    private static List<String> normalSmoke(EnchantingTemplateItem templateItem) {
        CEIConfigurableFluidTank tank = fillTank(Fluids.WATER, 1000);
        ItemStack template = new ItemStack(templateItem);
        Holder<Enchantment> enchantment = Holder.direct(smokeEnchantment("normal_smoke", Items.DIAMOND_SWORD, 1, 30));
        List<EnchantmentInstance> available = CEIEnchantmentHelper.getAvailableEnchantmentResults(30, Stream.of(enchantment));
        List<EnchantmentInstance> selected = CEIEnchantmentHelper.selectEnchantments(RandomSource.create(4L), 30, available);
        int cost = getExperienceCost(30, selected);
        ItemStack result = EnchanterBehaviour.applySelectedEnchantments(template, selected);
        extract(tank, cost);
        ItemEnchantments stored = result.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        List<String> lines = new ArrayList<>();
        lines.add("mode=normal");
        lines.add("surrogate_liquid_experience=minecraft:water");
        lines.add("available_count=" + available.size());
        lines.add("selected_count=" + selected.size());
        lines.add("result_item=create_enchantment_industry:smoke_enchanting_template");
        lines.add("template_item_class=" + templateItem.getClass().getSimpleName());
        lines.add("template_item_is_cei=" + (templateItem instanceof EnchantingTemplateItem));
        lines.add("result_item_class=" + result.getItem().getClass().getSimpleName());
        lines.add("result_is_enchanting_template=" + (result.getItem() instanceof EnchantingTemplateItem));
        lines.add("stored_enchantment_count=" + stored.size());
        lines.add("experience_cost=" + cost);
        lines.add("remaining_fluid=" + tank.getAmount());
        boolean normalTemplateFlowPass = !selected.isEmpty()
                && !stored.isEmpty()
                && templateItem instanceof EnchantingTemplateItem
                && result.getItem() instanceof EnchantingTemplateItem
                && tank.getAmount() == 1000 - cost;
        lines.add("normal_template_flow_pass=" + normalTemplateFlowPass);
        lines.add("normal_enchanting_pass=" + normalTemplateFlowPass);
        return lines;
    }

    private static List<String> deniedSmoke(EnchantingTemplateItem templateItem) {
        CEIConfigurableFluidTank tank = fillTank(Fluids.WATER, 1000);
        ItemStack template = new ItemStack(templateItem);
        Holder<Enchantment> denied = new DeniedSmokeHolder(smokeEnchantment("denied_smoke", Items.DIAMOND_SWORD, 1, 30));
        boolean deniedFilterExercised = EnchanterBehaviour.isDeniedByBlazeEnchanter(denied);
        List<EnchantmentInstance> filtered = CEIEnchantmentHelper.getAvailableEnchantmentResults(30,
                EnchanterBehaviour.filterDenied(Stream.of(denied), EnchanterBehaviour::isDeniedByBlazeEnchanter));
        ItemStack result = filtered.isEmpty() ? template.copy() : EnchanterBehaviour.applySelectedEnchantments(template, filtered);
        ItemEnchantments stored = result.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);

        List<String> lines = new ArrayList<>();
        lines.add("mode=denied");
        lines.add("surrogate_liquid_experience=minecraft:water");
        lines.add("denied_filter_exercised=" + deniedFilterExercised);
        lines.add("filtered_available_count=" + filtered.size());
        lines.add("denied_filter_removed=" + (filtered.isEmpty()));
        lines.add("result_item=create_enchantment_industry:smoke_enchanting_template");
        lines.add("result_item_class=" + result.getItem().getClass().getSimpleName());
        lines.add("result_is_enchanting_template=" + (result.getItem() instanceof EnchantingTemplateItem));
        lines.add("stored_enchantment_count=" + stored.size());
        lines.add("remaining_fluid=" + tank.getAmount());
        lines.add("denied_enchantment_pass=" + (deniedFilterExercised && filtered.isEmpty() && stored.isEmpty() && result.getItem() instanceof EnchantingTemplateItem && tank.getAmount() == 1000));
        return lines;
    }

    @SuppressWarnings("unchecked")
    private static EnchantingTemplateItem registerSmokeTemplate() throws ReflectiveOperationException {
        Identifier id = Identifier.fromNamespaceAndPath("create_enchantment_industry", "smoke_enchanting_template");
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Object registry = BuiltInRegistries.ITEM;
        Field frozen = registryField(registry.getClass(), "frozen");
        Field intrusive = registryField(registry.getClass(), "unregisteredIntrusiveHolders");
        boolean wasFrozen = frozen.getBoolean(registry);
        Object previousIntrusive = intrusive.get(registry);
        frozen.setBoolean(registry, false);
        if (previousIntrusive == null) {
            intrusive.set(registry, new IdentityHashMap<Item, Holder.Reference<Item>>());
        }
        try {
            return Registry.register(
                    BuiltInRegistries.ITEM,
                    key,
                    new EnchantingTemplateItem(new Item.Properties().setId(key), false));
        } finally {
            frozen.setBoolean(registry, wasFrozen);
            intrusive.set(registry, (Map<Item, Holder.Reference<Item>>) previousIntrusive);
        }
    }

    private static Field registryField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static int getExperienceCost(int enchantingLevel, List<EnchantmentInstance> selected) {
        if (selected.isEmpty()) {
            return 0;
        }
        int cost = 0;
        for (int index = 0; index < Math.ceilDiv(enchantingLevel, 10); index++) {
            cost += ExperienceHatchBehaviour.getExperienceForNextLevel(enchantingLevel - index);
        }
        return cost;
    }

    private static Enchantment smokeEnchantment(String path, Item item, int minCost, int maxCost) {
        return new Enchantment(
                net.minecraft.network.chat.Component.literal(path),
                Enchantment.definition(
                        HolderSet.direct(item.builtInRegistryHolder()),
                        1,
                        1,
                        Enchantment.constantCost(minCost),
                        Enchantment.constantCost(maxCost),
                        1,
                        EquipmentSlotGroup.ANY),
                HolderSet.empty(),
                DataComponentMap.EMPTY);
    }

    private static CEIConfigurableFluidTank fillTank(Fluid fluid, long amount) {
        CEIConfigurableFluidTank tank = smokeTank(fluid, 1000);
        try (Transaction transaction = Transaction.openOuter()) {
            tank.insert(new SmokeFluidVariant(fluid), amount, transaction);
            transaction.commit();
        }
        return tank;
    }

    private static void extract(CEIConfigurableFluidTank tank, long amount) {
        try (Transaction transaction = Transaction.openOuter()) {
            tank.extract(new SmokeFluidVariant(Fluids.WATER), amount, transaction);
            transaction.commit();
        }
    }

    private static CEIConfigurableFluidTank smokeTank(Fluid fluid, long capacity) {
        return new CEIConfigurableFluidTank(capacity, variant -> variant.isOf(fluid), variant -> variant.isOf(fluid), () -> { }) {
            @Override
            protected FluidVariant createVariant(Fluid fluid) {
                return new SmokeFluidVariant(fluid);
            }

            @Override
            protected FluidVariant createBlankVariant() {
                return new SmokeFluidVariant(Fluids.EMPTY);
            }
        };
    }

    private static void bindItemComponents(Item... items) {
        for (Item item : items) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
            }
        }
    }

    private record SmokeFluidVariant(Fluid fluid) implements FluidVariant {
        @Override
        public boolean isBlank() {
            return fluid == Fluids.EMPTY;
        }

        @Override
        public Fluid getObject() {
            return fluid;
        }

        @Override
        public DataComponentPatch getComponentsPatch() {
            return DataComponentPatch.EMPTY;
        }

        @Override
        public DataComponentMap getComponents() {
            return DataComponentMap.EMPTY;
        }

        @Override
        public FluidVariant withComponents(DataComponentPatch changes) {
            if (!changes.isEmpty()) {
                throw new IllegalArgumentException("Smoke fluid variant does not support components");
            }
            return this;
        }
    }

    private record DeniedSmokeHolder(Enchantment value) implements Holder<Enchantment> {
        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public boolean areComponentsBound() {
            return true;
        }

        @Override
        public boolean is(Identifier id) {
            return false;
        }

        @Override
        public boolean is(ResourceKey<Enchantment> key) {
            return false;
        }

        @Override
        public boolean is(Predicate<ResourceKey<Enchantment>> predicate) {
            return false;
        }

        @Override
        public boolean is(TagKey<Enchantment> tag) {
            return CEITags.BLAZE_ENCHANTER_DENY.equals(tag);
        }

        @Override
        public boolean is(Holder<Enchantment> holder) {
            return this == holder;
        }

        @Override
        public Stream<TagKey<Enchantment>> tags() {
            return Stream.of(CEITags.BLAZE_ENCHANTER_DENY);
        }

        @Override
        public DataComponentMap components() {
            return DataComponentMap.EMPTY;
        }

        @Override
        public Either<ResourceKey<Enchantment>, Enchantment> unwrap() {
            return Either.right(value);
        }

        @Override
        public Optional<ResourceKey<Enchantment>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public Kind kind() {
            return Kind.DIRECT;
        }

        @Override
        public boolean canSerializeIn(HolderOwner<Enchantment> owner) {
            return false;
        }
    }
}
