package decanoe.brewery.mixin;

import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BrewingStandBlockEntity.class)
public interface BrewingStandBlockEntityAccessor {
	@Accessor
	DefaultedList<ItemStack> getInventory();

	@Accessor
	int getFuel();

	@Accessor
	int getBrewTime();
	@Accessor("brewTime")
	public void setBrewTime(int brewTime);
	
	@Accessor
	Item getItemBrewing();
	@Accessor("itemBrewing")
	public void setItemBrewing(Item itemBrewing);

	@Invoker("canCraft")
	public static boolean invokeCanCraft(DefaultedList<ItemStack> slots) { throw new AssertionError(); }
}