package reddragon.api.utils;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemGroupUtils {

    public static RegistryKey<ItemGroup> createModItemGroup(final String namespace, final ItemConvertible icon) {

        // TODO: Use proper display name like namespace

        var itemGroup = FabricItemGroup.builder()
            .displayName(Text.literal("CHANGE ME"))
            .icon(() -> new ItemStack(icon))
            .build();

        var registryKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier(namespace, "item_group"));

        Registry.register(Registries.ITEM_GROUP, registryKey, itemGroup);

        return registryKey;
	}
}
