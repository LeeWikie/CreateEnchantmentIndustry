package plus.dragons.createenchantmentindustry.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import plus.dragons.createenchantmentindustry.CEI;

public final class CEICreativeTabs {
    public static final ResourceKey<CreativeModeTab> BASE = CEIIdentifier.key(Registries.CREATIVE_MODE_TAB, "base");

    private CEICreativeTabs() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BASE,
                CreativeModeTab.builder(null, -1)
                        .title(Component.translatable("itemGroup." + CEI.MOD_ID + ".base"))
                        .icon(() -> new ItemStack(CEIItems.SUPER_EXPERIENCE_NUGGET))
                        .displayItems(CEICreativeTabs::displayItems)
                        .build());
    }

    private static void displayItems(ItemDisplayParameters parameters, Output output) {
        for (ItemLike item : CEIItems.BASE_CONTENT) {
            output.accept(item);
        }
    }
}
