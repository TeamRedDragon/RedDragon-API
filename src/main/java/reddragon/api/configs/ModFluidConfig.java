package reddragon.api.configs;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import reddragon.api.content.BlockHolder;
import reddragon.api.content.fluids.AbstractFluid;
import reddragon.api.content.fluids.DryingFluidBlock;
import reddragon.api.utils.EnvironmentUtils;
import reddragon.api.utils.FluidUtils;

public final class ModFluidConfig implements BlockHolder {
	private DryingFluidBlock fluidBlock = null;

	// Builder parameters:

	private int tintColor = 0xFFFFFF;
	private int levelDecreasePerBlock = 1;
	private int flowSpeed = 5;
	private boolean ticksRandomly = false;
	private final Map<Supplier<Block>, Float> dryResults = new HashMap<>();

	// Set during registration:

	private BucketItem bucketItem;
	private FlowableFluid flowingFluid;
	private FlowableFluid stillFluid;

	public ModFluidConfig() {
		// Use default values.
	}

	/**
	 * Defines the tint color of this fluid.
	 * <p>
	 * The sprite texture for the fluid is multiplied with the tint color to get the
	 * final appearance.
	 * <p>
	 * Defaults to white (0xFFFFFF) if unspecified.
	 */
	public ModFluidConfig color(final int tintColor) {
		this.tintColor = tintColor;
		return this;
	}

	/**
	 * Defines the decrease of the fluid level per block in horizontal direction.
	 * <p>
	 * Defaults to 1 if unspecified.
	 */
	public ModFluidConfig levelDecreasePerBlock(final int levelDecreasePerBlock) {
		this.levelDecreasePerBlock = levelDecreasePerBlock;
		return this;
	}

	/**
	 * Allows this fluid to receive random ticks.
	 * <p>
	 * When one or more drying results are specified this method will be called
	 * implicitly.
	 */
	public ModFluidConfig ticksRandomly() {
		ticksRandomly = true;
		return this;
	}

	/**
	 * Defines the flow speed, that is the speed at which the fluid will spread
	 * horizontally and vertically.
	 * <p>
	 * Defaults to 5 if unspecified.
	 */
	public ModFluidConfig flowSpeed(final int flowSpeed) {
		this.flowSpeed = flowSpeed;
		return this;
	}

	/**
	 * Adds a possibility to the list of drying result blocks.
	 * <p>
	 * When the fluid dries due to a a random tick, one of the given block
	 * possibilities is picked. The weight of each possibility determines the chance
	 * of this block to be picked.
	 *
	 * @param blockHolder The result block of this drying possibility.
	 * @param weight      The chance of this possibility in relation to all other
	 *                    registered possibilities. Higher values in comparison will
	 *                    make this block more likely to be picked.
	 */
	public ModFluidConfig driesTo(final BlockHolder blockHolder, final float weight) {
		dryResults.put(() -> blockHolder.getBlock(), weight);
		return ticksRandomly();
	}

	/**
	 * Adds a possibility to the list of drying result blocks.
	 * <p>
	 * When the fluid dries due to a a random tick, one of the given block
	 * possibilities is picked. The weight of each possibility determines the chance
	 * of this block to be picked.
	 *
	 * @param blockHolder The result block of this drying possibility.
	 * @param weight      The chance of this possibility in relation to all other
	 *                    registered possibilities. Higher values in comparison will
	 *                    make this block more likely to be picked.
	 */
	public ModFluidConfig driesTo(final Block block, final float weight) {
		dryResults.put(() -> block, weight);
		return ticksRandomly();
	}

	public void register(final String namespace, final ItemGroup itemGroup, final String name) {
		// Once we have all data, we can create required objects.

		stillFluid = new AbstractFluid.Still(
				() -> stillFluid,
				() -> flowingFluid,
				() -> fluidBlock,
				() -> bucketItem,
				levelDecreasePerBlock,
				flowSpeed);

		flowingFluid = new AbstractFluid.Flowing(
				() -> stillFluid,
				() -> flowingFluid,
				() -> fluidBlock,
				() -> bucketItem,
				levelDecreasePerBlock,
				flowSpeed);

		final var blockSettings = Settings.copy(Blocks.WATER);
		if (ticksRandomly) {
			blockSettings.ticksRandomly();
		}

		fluidBlock = new DryingFluidBlock(stillFluid, blockSettings);

		for (final Entry<Supplier<Block>, Float> chance : dryResults.entrySet()) {
			fluidBlock.addDriedResult(chance.getKey(), chance.getValue());
		}

        bucketItem = new BucketItem(stillFluid, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

		// Begin registering all data.

		final var identifier = new Identifier(namespace, name.toLowerCase(Locale.ROOT));

        Registry.register(Registries.FLUID, identifier, stillFluid);
        Registry.register(Registries.FLUID, new Identifier(namespace, identifier.getPath() + "_flowing"), flowingFluid);

        Registry.register(Registries.BLOCK, identifier, fluidBlock);
        Registry.register(Registries.ITEM,
				new Identifier(namespace, identifier.getPath() + "_bucket"), bucketItem);

		EnvironmentUtils.clientOnly(() -> () -> {
			FluidUtils.setupFluidRendering(stillFluid, flowingFluid, identifier, tintColor);
		});
	}

	@Override
	public DryingFluidBlock getBlock() {
		return fluidBlock;
	}

	public FlowableFluid getStillFluid() {
		return stillFluid;
	}
}
