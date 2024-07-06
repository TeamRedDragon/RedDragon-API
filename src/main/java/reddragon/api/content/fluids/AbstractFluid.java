package reddragon.api.content.fluids;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import reddragon.api.RedDragonApiMod;
import reddragon.api.mixin.FlowableFluidAccessor;

public abstract class AbstractFluid extends FlowableFluid {

	private final Supplier<Fluid> flowingFluidSuppler;
	private final Supplier<Fluid> stillFluidSuppler;
	private final Supplier<FluidBlock> fluidBlockSupplier;
	private final Supplier<BucketItem> bucketItemSuppler;

	private final int levelDecreasePerBlock;
	private final int tickRate;

	public AbstractFluid(
			final Supplier<Fluid> stillFluidSuppler,
			final Supplier<Fluid> flowingFluidSuppler,
			final Supplier<FluidBlock> fluidBlockSupplier,
			final Supplier<BucketItem> bucketItemSuppler,
			final int levelDecreasePerBlock,
			final int tickRate) {
		this.stillFluidSuppler = stillFluidSuppler;
		this.flowingFluidSuppler = flowingFluidSuppler;
		this.fluidBlockSupplier = fluidBlockSupplier;
		this.bucketItemSuppler = bucketItemSuppler;
		this.levelDecreasePerBlock = levelDecreasePerBlock;
		this.tickRate = tickRate;
	}

	@Override
	public Fluid getStill() {
		return stillFluidSuppler.get();
	}

	@Override
	public Fluid getFlowing() {
		return flowingFluidSuppler.get();
	}

	@Override
    protected boolean isInfinite(World world) {
		return false;
	}

    /**
     * Perform actions when the fluid flows into a replaceable block. Water drops
     * the block's loot table. Lava plays the "block.lava.extinguish" sound.
     */
    @Override
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        final var blockEntity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
        Block.dropStacks(state, world, pos, blockEntity);
    }

	@Override
	protected int getFlowSpeed(final WorldView world) {
		return 4;
	}

	@Override
	public int getLevelDecreasePerBlock(final WorldView world) {
		return levelDecreasePerBlock;
	}

	@Override
	public Item getBucketItem() {
		return bucketItemSuppler.get();
	}

	@Override
	protected boolean canBeReplacedWith(final FluidState state, final BlockView world, final BlockPos pos, final Fluid fluid, final Direction direction) {
		if (isStill(null)) {
			return false;
		}

		final var sourcePosition = pos.offset(direction.getOpposite());
		final var targetPosition = pos;

		final var sourceState = world.getFluidState(sourcePosition);
		final var targetState = world.getFluidState(targetPosition);

		final int assumedNewBlockLevel;

        if (sourceState.getFluid() instanceof FlowableFluidAccessor sourceFluid && world instanceof WorldView worldView) {
			if (direction.getAxis().isVertical()) {
				assumedNewBlockLevel = 8;
			} else {
                assumedNewBlockLevel = sourceState.getLevel() - sourceFluid.callGetLevelDecreasePerBlock(worldView);
			}
		} else {
			RedDragonApiMod.LOG.error("Assuming decrease of 1");

			// Cannot access getter. Assume a decrease of 1.

			assumedNewBlockLevel = sourceState.getLevel() - 1;
		}

		return assumedNewBlockLevel > targetState.getLevel();
	}

	@Override
	public int getTickRate(final WorldView world) {
		return tickRate;
	}

	@Override
	protected float getBlastResistance() {
		return 100;
	}

    @Override
    protected BlockState toBlockState(final FluidState fluidState) {
        return fluidBlockSupplier.get().getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(fluidState));
    }

	@Override
	public boolean matchesType(final Fluid fluid) {
		return getFlowing() == fluid || getStill() == fluid;
	}

	@Override
	public String toString() {
		return fluidBlockSupplier.get().getTranslationKey();
	}

	public static class Still extends AbstractFluid {
		public Still(final Supplier<Fluid> stillFluidSuppler, final Supplier<Fluid> flowingFluidSuppler, final Supplier<FluidBlock> fluidBlockSupplier,
				final Supplier<BucketItem> bucketItemSuppler, final int levelDecreasePerBlock, final int flowSpeed) {
			super(stillFluidSuppler, flowingFluidSuppler, fluidBlockSupplier, bucketItemSuppler, levelDecreasePerBlock, flowSpeed);
		}

		@Override
		public boolean isStill(final FluidState fluidState) {
			return true;
		}

        @Override
        public int getLevel(FluidState fluidState) {
            return 8;
        }
	}

	public static class Flowing extends AbstractFluid {
		public Flowing(final Supplier<Fluid> stillFluidSuppler, final Supplier<Fluid> flowingFluidSuppler, final Supplier<FluidBlock> fluidBlockSupplier,
				final Supplier<BucketItem> bucketItemSuppler, final int levelDecreasePerBlock, final int flowSpeed) {
			super(stillFluidSuppler, flowingFluidSuppler, fluidBlockSupplier, bucketItemSuppler, levelDecreasePerBlock, flowSpeed);
		}

		@Override
		protected void appendProperties(final StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		@Override
		public boolean isStill(final FluidState fluidState) {
			return false;
		}

        @Override
        public int getLevel(FluidState fluidState) {
            return fluidState.get(LEVEL);
        }
	}
}
