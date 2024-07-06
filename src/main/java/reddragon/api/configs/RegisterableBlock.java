package reddragon.api.configs;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;

/**
 * Interface for an enumeration of all blocks that are added to the game by the
 * mod.
 * <p>
 * All block should be registered during the mods initialization phase by
 * calling {@link #register()} on each enum value.
 */
public interface RegisterableBlock {
	/**
	 * Requests the identifier name of this block. Usually you should not need to
	 * implement this when this interface is implemented by an enum.
	 */
	String name();

	/**
	 * Requests the item group this block should be added to.
	 */
    RegistryKey<ItemGroup> getItemGroup();

	/**
	 * Requests the identifier namespace of this block. This is usually the mod ID.
	 */
	String getNamespace();

	/**
	 * Requests the configuration of this block. The configuration is used
	 * internally when registering the block to the game.
	 */
	ModBlockConfig getConfig();

	default void register() {
		getConfig().register(getNamespace(), getItemGroup(), name());
	}
}