package decanoe.brewery.mixin;

import decanoe.brewery.brewing_utils.ModPotionUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipeRegistry.class)
public class BrewingRecipeRegistryMixin {

	@Inject(at = @At("RETURN"), method = "isValidIngredient", cancellable = true)
	private static void isValidIngredient(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) return;
		if (ModPotionUtils.matchesBottle(stack)) return;

		cir.setReturnValue(true);
	}

	@Inject(at = @At("RETURN"), method = "hasRecipe", cancellable = true)
	private static void hasRecipe(ItemStack stack, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValue()) return;
		if (stack.isEmpty() || ModPotionUtils.matchesBottle(ingredient)) return;
		
		cir.setReturnValue(ModPotionUtils.Ingredients.isIngredient(ingredient));
	}

	@Inject(at = @At("RETURN"), method = "craft", cancellable = true)
	private static void craft(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
		if (input.isEmpty() || input.isOf(Items.GLASS_BOTTLE)) return;
		boolean has_changed = cir.getReturnValue() != input;

		boolean too_many_ingredient = ModPotionUtils.getNbIngredient(cir.getReturnValue()) == ModPotionUtils.MAX_INGREDIENT_COUNT;
		boolean has_ingredient = ModPotionUtils.hasIngredient(cir.getReturnValue(), ingredient.getItem());
		cir.setReturnValue(ModPotionUtils.pushIngredient(cir.getReturnValue(), ingredient.getItem()));

		if (has_changed) {  // to not interfer with vanilla brewing
			return;
		}
		
		// fail potion if duplicate ingredient or too many ingredients
		if (has_ingredient || too_many_ingredient) {
			cir.setReturnValue(ModPotionUtils.makeFailed(cir.getReturnValue()));
			return;
		}

		Potion input_potion = PotionUtil.getPotion(cir.getReturnValue());

		// potion base creation
		if (input_potion == Potions.WATER) {
			cir.setReturnValue(ModPotionUtils.PotionBases.turnToBase(cir.getReturnValue(), ingredient));
			return;
		}
		
		// change to new potion to avoid vanilla potion base override (like with awkward potion)
		if (ModPotionUtils.PotionBases.getVariant(input_potion) != input_potion) {
			cir.setReturnValue(PotionUtil.setPotion(cir.getReturnValue(), ModPotionUtils.PotionBases.getVariant(input_potion)));
			input_potion = PotionUtil.getPotion(cir.getReturnValue());
		}
		
		// fail potion if no base provided
		if (!ModPotionUtils.PotionBases.isModifiedBase(input_potion)) {
			cir.setReturnValue(ModPotionUtils.makeFailed(cir.getReturnValue()));
			return;
		}

		ModPotionUtils.Ingredients.applyIngredient(cir.getReturnValue(), ingredient);
	}
}