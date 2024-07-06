package reddragon.api.configs;

import java.util.Locale;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import reddragon.api.content.BlockHolder;

public class ModBlockConfig implements BlockHolder {

	private Block block = null;

	public ModBlockConfig(final Block block) {
		this.block = block;
	}

	public ModBlockConfig(final AbstractBlock.Settings settings) {
		this(new Block(settings));
	}

    public void register(String namespace, RegistryKey<ItemGroup> itemGroup, String name) {
        var identifier = new Identifier(namespace, name.toLowerCase(Locale.ROOT));
        var blockItem = new BlockItem(block, new Item.Settings());

        Registry.register(Registries.BLOCK, identifier, block);
        Registry.register(Registries.ITEM, identifier, blockItem);

        ItemGroupEvents.modifyEntriesEvent(itemGroup).register(entries -> entries.add(blockItem));
	}

	@Override
	public Block getBlock() {
		return block;
	}
}
