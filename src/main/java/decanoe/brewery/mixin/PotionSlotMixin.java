package decanoe.brewery.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$PotionSlot")
public class PotionSlotMixin {
    @Inject(at = @At("RETURN"), method = "matches", cancellable = true)
    private static void matches(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        cir.setReturnValue(stack.isOf(Items.ARROW));
	}

    @Inject(at = @At("RETURN"), method = "getMaxItemCount", cancellable = true)
    private void getMaxItemCount(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Math.min(cir.getReturnValue(), 16));
	}
}