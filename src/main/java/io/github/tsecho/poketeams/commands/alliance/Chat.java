package io.github.tsecho.poketeams.commands.alliance;

import io.github.tsecho.poketeams.apis.AllianceAPI;
import io.github.tsecho.poketeams.apis.PokeTeamsAPI;
import io.github.tsecho.poketeams.configuration.ConfigManager;
import io.github.tsecho.poketeams.enums.ChatTypes;
import io.github.tsecho.poketeams.enums.Messages.ErrorMessages;
import io.github.tsecho.poketeams.enums.Messages.SuccessMessages;
import io.github.tsecho.poketeams.enums.Messages.TechnicalMessages;
import io.github.tsecho.poketeams.language.ChatUtils;
import io.github.tsecho.poketeams.language.Texts;
import io.github.tsecho.poketeams.utilities.ErrorCheck;
import io.github.tsecho.poketeams.utilities.Permissions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import static io.github.tsecho.poketeams.configuration.ConfigManager.*;

public class Chat implements CommandExecutor {

    private PokeTeamsAPI role;
    private AllianceAPI ogAlliance;
    private CommandSource src;

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        this.src = src;

        if(!(src instanceof Player))
            return ErrorCheck.test(src, TechnicalMessages.NOT_PLAYER);

        role = new PokeTeamsAPI(src);

        if(!role.inTeam())
           return ErrorCheck.test(src, ErrorMessages.NOT_IN_TEAM);

        ogAlliance = new AllianceAPI(role);

        if(!ogAlliance.inAlliance())
            return ErrorCheck.test(src, ErrorMessages.NOT_IN_ALLIANCE);
        if(args.getOne(Text.of("message")).isPresent())
            return sendMessage(args);

        if(ChatUtils.getChatType(src.getName()) != ChatTypes.PUBLIC) {
            ChatUtils.setChat(src.getName(), ChatTypes.PUBLIC);
            src.sendMessage(SuccessMessages.ALLY_REMOVED_CHAT.getText(src));
        } else {
            ChatUtils.setChat(src.getName(), ChatTypes.ALLIANCE);
            src.sendMessage(SuccessMessages.ALLY_ADDED_CHAT.getText(src));
        }

        return CommandResult.success();
    }

    private CommandResult sendMessage(CommandContext args) {

        String prefix = getConfNode("Ally-Settings", "Chat-Settings", "Prefix").getString();
        String chatColor = getConfNode("Ally-Settings", "Chat-Settings", "Chat-Color").getString();
        String message = args.<String>getOne(Text.of("message")).get();

        Text newMessage = Texts.of((prefix + chatColor + message), src);
        Text staffMessage = Texts.of(getConfNode("Chat-Settings", "SocialSpy-Message").getString() + message, src);

        for(Player members : Sponge.getServer().getOnlinePlayers())
            if(inAlliance(members))
                members.sendMessage(newMessage);
            else if(isStaff(members) && !ChatUtils.inSocialSpyOff(members.getName()))
                members.sendMessage(staffMessage);

        if(getConfNode("Language-Settings", "Console-SocialSpy").getBoolean())
            MessageChannel.TO_CONSOLE.send(newMessage);

        return CommandResult.success();
    }

    private boolean inAlliance(Player members) {
        AllianceAPI alliance = new AllianceAPI(new PokeTeamsAPI(members));
        return alliance.inAlliance() && alliance.getAlliance().equals(ogAlliance.getAlliance());
    }

    private boolean isStaff(Player members) {
        return getConfNode("Chat-Settings", "Players-SocialSpy").getBoolean() && members.hasPermission(Permissions.SOCIAL_SPY);
    }


    public static CommandSpec build() {
        return CommandSpec.builder()
                .permission(Permissions.ALLY_CHAT)
                .arguments(GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("message"))))
                .executor(new Chat())
                .build();
    }
}
