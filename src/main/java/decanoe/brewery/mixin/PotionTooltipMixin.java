package decanoe.brewery.mixin;

import decanoe.brewery.brewing_utils.ModPotionUtils;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionTooltipMixin extends ItemMixin {
	@Inject(at = @At("TAIL"), method = "appendTooltip", cancellable = true)
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context, CallbackInfo callback_info) {
		if (stack.hasNbt() && stack.getNbt().contains("show_recipe")) {
			tooltip.add(ScreenTexts.EMPTY);
			tooltip.add(Text.translatable("brewery.ingredient_list_header").formatted(Formatting.DARK_AQUA));

			for (Item item : ModPotionUtils.getIngredients(stack)) {
				tooltip.add(Text.translatable(item.getTranslationKey()).formatted(Formatting.DARK_AQUA));
			}
		}
   	}

	
	@Override
	public void hasGlint(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) return;
		if (!stack.hasNbt()) return;

		if (stack.getNbt().contains("ench_glint")) cir.setReturnValue(true);
	}
}