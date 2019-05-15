package io.github.tsecho.poketeams.commands;

import io.github.tsecho.poketeams.enums.Messages.ErrorMessages;
import io.github.tsecho.poketeams.enums.Messages.SuccessMessages;
import io.github.tsecho.poketeams.enums.Messages.TechnicalMessages;
import io.github.tsecho.poketeams.utilities.ErrorCheck;
import io.github.tsecho.poketeams.language.CensorCheck;
import io.github.tsecho.poketeams.utilities.Permissions;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import io.github.tsecho.poketeams.apis.PokeTeamsAPI;

public class Tag implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		
		String tag = args.<String>getOne(Text.of("tag")).get();

		if(!(src instanceof Player))
			return ErrorCheck.test(src, TechnicalMessages.NOT_PLAYER);

		PokeTeamsAPI role = new PokeTeamsAPI(src);
		CensorCheck check = new CensorCheck(tag);

		if(!role.inTeam())
			return ErrorCheck.test(src, ErrorMessages.NOT_IN_TEAM);
		if(!role.canSetTag())
			return ErrorCheck.test(src, ErrorMessages.INSUFFICIENT_RANK);
		if(check.failsCensor(true))
			return ErrorCheck.test(src, ErrorMessages.INNAPROPRIATE);

		role.setTag(tag);
		src.sendMessage(SuccessMessages.TAG_CHANGE.getText(src));

		return CommandResult.success();
	}

	public static CommandSpec build() {
		return CommandSpec.builder()
				.permission(Permissions.TAG)
				.arguments(GenericArguments.string(Text.of("tag")))
				.executor(new Tag())
				.build();
	}
}
