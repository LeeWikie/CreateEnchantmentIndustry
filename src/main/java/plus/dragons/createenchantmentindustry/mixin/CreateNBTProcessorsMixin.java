package plus.dragons.createenchantmentindustry.mixin;

import com.zurrtum.create.foundation.CreateNBTProcessors;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreateNBTProcessors.class)
public class CreateNBTProcessorsMixin {
    private static final String PRINTER_TEMPLATE_KEY = "Template";
    private static final String ENCHANTER_TEMPLATE_KEY = "EnchantingTemplate";

    @Inject(method = "clipboardProcessor", at = @At("HEAD"))
    private static void create_enchantment_industry$removeTemplates(CompoundTag data, CallbackInfoReturnable<CompoundTag> cir) {
        data.remove(PRINTER_TEMPLATE_KEY);
        data.remove(ENCHANTER_TEMPLATE_KEY);
    }
}
