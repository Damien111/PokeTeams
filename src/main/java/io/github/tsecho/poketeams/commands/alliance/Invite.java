package io.github.tsecho.poketeams.commands.alliance;

import io.github.tsecho.poketeams.apis.AllianceAPI;
import io.github.tsecho.poketeams.apis.PokeTeamsAPI;
import io.github.tsecho.poketeams.enums.AllyRanks;
import io.github.tsecho.poketeams.enums.messages.ErrorMessage;
import io.github.tsecho.poketeams.enums.messages.SuccessMessage;
import io.github.tsecho.poketeams.enums.messages.TechnicalMessage;
import io.github.tsecho.poketeams.language.Texts;
import io.github.tsecho.poketeams.utilities.ErrorCheck;
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
import org.spongepowered.api.text.action.TextActions;

public class Invite implements CommandExecutor {

    private CommandSource receiver, sender;
    private PokeTeamsAPI team, teamOther;
    private AllianceAPI alliance, allianceOther;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        sender = src;
        receiver = args.<Player>getOne(Text.of("player")).get();
        team = new PokeTeamsAPI(src);
        teamOther = new PokeTeamsAPI(receiver);

        if(!(src instanceof Player))
            return ErrorCheck.test(src, TechnicalMessage.NOT_PLAYER);
        if(!team.inTeam())
            return ErrorCheck.test(src, ErrorMessage.NOT_IN_TEAM);
        if(!teamOther.inTeam())
            return ErrorCheck.test(src, ErrorMessage.OTHER_NOT_IN_TEAM);

        alliance = new AllianceAPI(team);
        allianceOther = new AllianceAPI(teamOther);

        if(!team.canAllianceCommands() || !alliance.canInvite())
            return ErrorCheck.test(src, ErrorMessage.INSUFFICIENT_RANK);
        if(allianceOther.inAlliance())
            return ErrorCheck.test(src, ErrorMessage.ALLY_ALREADY_IN_TEAM);

        receiver.sendMessage(Text.builder()
                .append(SuccessMessage.ALLY_INVITED.getText(sender))
                .append(Texts.of("\n"))
                .append(SuccessMessage.INVITE_CLICK.getText(sender))
                .onClick(TextActions.executeCallback(callback -> joinTeam()))
                .build());

        sender.sendMessage(SuccessMessage.SEND_INVITE.getText(src));

        return CommandResult.success();
    }

    private void joinTeam() {
        alliance.addTeam(teamOther, AllyRanks.MEMBER.getHierarchyPlace());
        receiver.sendMessage(SuccessMessage.JOINED_ALLIANCE.getText(sender));
        sender.sendMessage(SuccessMessage.INVITE_ACCEPTED.getText(receiver));
    }

    public static CommandSpec build() {
        return CommandSpec.builder()
                .permission(Permissions.ALLY_INVITE)
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
                .executor(new Invite())
                .build();
    }
}

