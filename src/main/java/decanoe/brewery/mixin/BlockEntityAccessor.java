package decanoe.brewery.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntity.class)
public interface BlockEntityAccessor {
	@Invoker("markDirty")
	public static void invokeMarkDirty(World world, BlockPos pos, BlockState state) { throw new AssertionError(); }
}