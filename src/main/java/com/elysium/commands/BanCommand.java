package com.elysium.commands;
import com.elysium.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType; // Correct import
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.concurrent.TimeUnit;

public class BanCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("ban", "Ban a user from the server")
                .addOption(OptionType.USER, "target", "The user to ban", true)
                .addOption(OptionType.STRING, "reason", "Reason for the ban", false)
                // Restricts command to users with Ban permission
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                // Ensures command only works in servers, not DMs
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User target = event.getOption("target").getAsUser();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

        // Perform the ban (0 days of message deletion)
        event.getGuild().ban(target, 0, TimeUnit.DAYS).reason(reason).queue(
                success -> event.reply("Banned " + target.getName() + " | Reason: " + reason).queue(),
                error -> event.reply("Failed to ban: " + error.getMessage()).setEphemeral(true).queue()
        );
    }
}