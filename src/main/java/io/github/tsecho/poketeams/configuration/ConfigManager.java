package io.github.tsecho.poketeams.configuration;

import io.github.tsecho.poketeams.PokeTeams;
import io.github.tsecho.poketeams.utilities.WorldInfo;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

	private static Path dir, config, storage, censor, lang;
	private static ConfigurationLoader<CommentedConfigurationNode> storLoad, confLoad, censorLoad, langLoad;
	private static CommentedConfigurationNode confNode, storNode, censorNode, langNode;
	private static final String[] FILES = {"Configuration.conf", "Censor.conf", "Language.conf", "Teams.conf"};

	public static void setup(Path folder) {
		dir = folder;
		config = dir.resolve(FILES[0]);
		censor = dir.resolve(FILES[1]);
		lang = dir.resolve(FILES[2]);
		storage = dir.resolve(FILES[3]);
	}
	
	public static void load() {
		try {
			if(!Files.exists(dir))
				Files.createDirectory(dir);

			PokeTeams.getContainer().getAsset(FILES[0]).get().copyToFile(config, false, true);
			PokeTeams.getContainer().getAsset(FILES[1]).get().copyToFile(censor, false, true);
			PokeTeams.getContainer().getAsset(FILES[2]).get().copyToFile(lang, false, true);

			confLoad = HoconConfigurationLoader.builder().setPath(config).build();
			storLoad = HoconConfigurationLoader.builder().setPath(storage).build();
			censorLoad = HoconConfigurationLoader.builder().setPath(censor).build();
			langLoad = HoconConfigurationLoader.builder().setPath(lang).build();

			confNode = confLoad.load();
			storNode = storLoad.load();
			censorNode = censorLoad.load();
			langNode = langLoad.load();
				
		} catch(IOException e) {
			PokeTeams.getLogger().error("Error loading up PokeTeams Configuration"); e.printStackTrace();
		}

		save();
		WorldInfo.refreshPos();
	}
	
	public static void save() {
		try {
			confLoad.save(confNode);
			storLoad.save(storNode);
			censorLoad.save(censorNode);
			langLoad.save(langNode);
		} catch (IOException e) {
			PokeTeams.getLogger().error("Error saving PokeTeams Configuration"); e.printStackTrace();
		}
	}

	public static void update() {
		try {
			confNode.mergeValuesFrom(HoconConfigurationLoader.builder()
					.setURL(PokeTeams.getContainer().getAsset(FILES[0]).get().getUrl())
					.build()
					.load(ConfigurationOptions.defaults()));

			langNode.mergeValuesFrom(HoconConfigurationLoader.builder()
					.setURL(PokeTeams.getContainer().getAsset(FILES[2]).get().getUrl())
					.build()
					.load(ConfigurationOptions.defaults()));

			save();

		} catch (IOException e) {
			PokeTeams.getLogger().error("Error updating up PokeTeams Configuration"); e.printStackTrace();
		}
	}

	public static CommentedConfigurationNode getConfNode(Object... node) {
		return confNode.getNode(node);
	}

	public static CommentedConfigurationNode getStorNode(Object... node) {
		return storNode.getNode(node);
	}

	public static CommentedConfigurationNode getLangNode(Object... node) {
		return langNode.getNode(node);
	}

	public static CommentedConfigurationNode getCensorNode(Object... node) {
		return censorNode.getNode(node);
	}

}
