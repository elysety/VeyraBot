package com.elysium.commands;
import com.elysium.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Duration;

public class TimeoutCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("timeout", "Timeout a member to prevent them from chatting")
                .addOption(OptionType.USER, "target", "The user to timeout", true)
                .addOptions(
                        new OptionData(OptionType.INTEGER, "duration", "How long they should be timed out", true)
                                .addChoice("60 Seconds", 60)
                                .addChoice("5 Minutes", 300)
                                .addChoice("1 Hour", 3600)
                                .addChoice("1 Day", 86400)
                                .addChoice("1 Week", 604800)
                )
                .addOption(OptionType.STRING, "reason", "Reason for the timeout", false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member target = event.getOption("target").getAsMember();
        int seconds = event.getOption("duration").getAsInt();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

        if (target == null) {
            event.reply("User not found in this server.").setEphemeral(true).queue();
            return;
        }

        target.timeoutFor(Duration.ofSeconds(seconds)).reason(reason).queue(
                success -> event.reply("Successfully timed out " + target.getUser().getName() + " for " + seconds + " seconds.").queue(),
                error -> event.reply("Failed to timeout: " + error.getMessage()).setEphemeral(true).queue()
        );
    }
}