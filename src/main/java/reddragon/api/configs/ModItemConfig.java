package reddragon.api.configs;

import java.util.Locale;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Item.Settings;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import reddragon.api.content.ItemHolder;

public class ModItemConfig implements ItemHolder {

    private final Item item;

    private ModItemConfig(Item.Settings settings) {
        item = new Item(settings);
	}

    public ModItemConfig(final FoodComponent foodComponent, int maxCount) {
        this(new Settings()
            .food(foodComponent)
            .maxCount(maxCount));
	}

    public ModItemConfig(final FoodComponent foodComponent, int maxCount, Item recipeRemainder) {
        this(new Settings()
            .food(foodComponent)
            .maxCount(maxCount)
            .recipeRemainder(recipeRemainder));
	}

    public void register(String namespace, RegistryKey<ItemGroup> itemGroup, String name) {
		final var identifier = new Identifier(namespace, name.toLowerCase(Locale.ROOT));

        Registry.register(Registries.ITEM, identifier, item);

        ItemGroupEvents.modifyEntriesEvent(itemGroup)
            .register(groupEntries -> groupEntries.add(item));
	}

	@Override
	public Item getItem() {
		return item;
	}
}
