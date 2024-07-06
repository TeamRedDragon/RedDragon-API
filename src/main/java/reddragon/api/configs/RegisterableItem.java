package reddragon.api.configs;

public interface RegisterableItem {
	String name();

	String getNamespace();

	ModItemConfig getConfig();
}