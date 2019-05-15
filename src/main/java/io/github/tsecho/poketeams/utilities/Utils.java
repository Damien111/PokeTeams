package io.github.tsecho.poketeams.utilities;

import io.github.tsecho.poketeams.configuration.ConfigManager;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.ArrayList;
import java.util.Map;

public class Utils {

	private static ArrayList<String> inQueueSystem = new ArrayList();

	public static void removeQueue(String player) {
		inQueueSystem.remove(player);
	}
	
	public static void addQueue(String player) {
		inQueueSystem.add(player);
	}
	
	public static boolean inQueue(String player) {
		return inQueueSystem.contains(player);
	}

	public static boolean teamExists(String team) {
		return !ConfigManager.getStorNode("Teams", team).isVirtual();
	}

	public static void moveToUUID() {
		UserStorageService service = Sponge.getServiceManager().provide(UserStorageService.class).get();

		for (Map.Entry<Object, ? extends CommentedConfigurationNode> teams : ConfigManager.getStorNode("Teams").getChildrenMap().entrySet()) {

			String team = teams.getKey().toString();

			for (Map.Entry<Object, ? extends CommentedConfigurationNode> players : ConfigManager.getStorNode("Teams", team, "Members").getChildrenMap().entrySet()) {

				String name = players.getKey().toString();
				String rank = ConfigManager.getStorNode("Teams", team, "Members", name).getString();

				if (!name.matches("[a-f0-9]{8}-[a-f0-9]{4}-4[0-9]{3}-[89ab][a-f0-9]{3}-[0-9a-f]{12}") && service.get(name).isPresent()) {
					String uuid = service.get(name).get().getUniqueId().toString();

					if (!uuid.equals(name)) {
						ConfigManager.getStorNode("Teams", team, "Members", uuid).setValue(rank);
						ConfigManager.getStorNode("Teams", team, "Members", name).setValue(null);
					}
				}
			}
		}
		ConfigManager.save();
	}
}
