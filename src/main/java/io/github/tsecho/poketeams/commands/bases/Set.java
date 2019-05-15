package io.github.tsecho.poketeams.commands.bases;

import io.github.tsecho.poketeams.enums.Messages.ErrorMessages;
import io.github.tsecho.poketeams.enums.Messages.SuccessMessages;
import io.github.tsecho.poketeams.enums.Messages.TechnicalMessages;
import io.github.tsecho.poketeams.utilities.ErrorCheck;
import io.github.tsecho.poketeams.utilities.WorldInfo;
import io.github.tsecho.poketeams.configuration.ConfigManager;
import io.github.tsecho.poketeams.utilities.Permissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;

import io.github.tsecho.poketeams.apis.PokeTeamsAPI;

public class Set implements CommandExecutor {

	private PokeTeamsAPI role;
	private Player player;

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

		if(!(src instanceof Player))
			return ErrorCheck.test(src, TechnicalMessages.NOT_PLAYER);

		player = (Player) src;
		role = new PokeTeamsAPI(src);

		if(!role.inTeam())
			return ErrorCheck.test(src, ErrorMessages.NOT_IN_TEAM);
		if(!role.canSetBase())
			return ErrorCheck.test(src, ErrorMessages.INSUFFICIENT_RANK);
		if(isBadWorld())
			return ErrorCheck.test(src, ErrorMessages.BAD_WORLD);

		role.setBase(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getWorld());
		src.sendMessage(SuccessMessages.SET_BASE.getText(src));

		return CommandResult.success();
	}

	private boolean isBadWorld() {
		if(ConfigManager.getConfNode("Base-Settings", "Only-Default-World").getBoolean())
			if(!player.getWorldUniqueId().get().toString().equalsIgnoreCase(WorldInfo.getWorldUUID().toString()))
				return true;
		return false;
	}
	
	public static CommandSpec build() {
		return CommandSpec.builder()
				.permission(Permissions.BASE_SET)
				.executor(new Set())
				.build();
	}
}
