package decanoe.brewery.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {

	@Inject(at = @At("HEAD"), method = "tick", cancellable = true)
	private static void tick(World world, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
		BrewingStandBlockEntityAccessor accessor = ((BrewingStandBlockEntityAccessor)blockEntity);
		ItemStack itemStack = (ItemStack)accessor.getInventory().get(4);

		if (accessor.getFuel() <= 0 && itemStack.isOf(Items.BLAZE_POWDER)) return; // refuel will be done by minecraft

		boolean can_craft = BrewingStandBlockEntityAccessor.invokeCanCraft(accessor.getInventory());
		boolean is_crafting = accessor.getBrewTime() > 0;

		if (is_crafting && accessor.getFuel() > 0) {
			// brew 3 times as fast if fuel provided
			accessor.setBrewTime(Math.max(1, accessor.getBrewTime() - 2));
		}
		else if (!is_crafting && can_craft && accessor.getFuel() <= 0) {
			accessor.setBrewTime(400);
			accessor.setItemBrewing(accessor.getInventory().get(3).getItem());
			BlockEntityAccessor.invokeMarkDirty(world, pos, state);
		}
	}
}