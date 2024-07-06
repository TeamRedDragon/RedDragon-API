package reddragon.api.content.fluids;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import reddragon.api.random.RandomPicker;

public class DryingFluidBlock extends FluidBlock {

	public static final int MIN_LIGHT_LEVEL_FOR_DRYING = 13;

	private RandomPicker<Block> driedResultPicker = new RandomPicker<>();

	public DryingFluidBlock(final FlowableFluid fluid, final Settings properties) {
		super(fluid, properties);
	}

	public void addDriedResult(final Supplier<Block> block, final float weight) {
		driedResultPicker = driedResultPicker.withChance(weight, block);
	}

	public Block getDriedResult(final World world) {
		return driedResultPicker.pick(world.random);
	}

	@Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (canDry(state, world, pos)) {
            dry(state, world, pos);
        }
	}

	private boolean canDry(final BlockState state, final ServerWorld world, final BlockPos pos) {
		if (!isSourceBlock(state) || (world.getLightLevel(pos.up(), 0) < MIN_LIGHT_LEVEL_FOR_DRYING) || isWaterNearby(world, pos, 1, 7) || world.hasRain(pos.up())) {
			return false;
		}

		return true;
	}

	private void dry(final BlockState state, final World world, final BlockPos pos) {

		final var resultBlock = getDriedResult(world);

		world.setBlockState(pos, resultBlock.getDefaultState(), 3);
		world.syncWorldEvent(1501, pos, 0); // ExtingushEvent
	}

	private boolean isSourceBlock(final BlockState state) {
		final var fluidState = getFluidState(state);
		return fluidState.isStill();
	}

	private boolean isWaterNearby(final WorldView world, final BlockPos pos, final int radius, final int sources) {
		var count = 0;
		final var var2 = BlockPos.iterate(pos.add(-radius, -1, -radius), pos.add(radius, 0, radius))
				.iterator();

		BlockPos blockPos;
		do {
			if (!var2.hasNext()) {
				return false;
			}
			blockPos = var2.next();
			final var fluidState = world.getFluidState(blockPos);
			if (fluidState.isIn(FluidTags.WATER) && fluidState.isStill()) {
				count++;
			}
		} while (count <= sources);

		return true;
	}

	@Override
	public boolean hasRandomTicks(final BlockState state) {
		return true;
	}

}
