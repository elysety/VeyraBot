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

import java.util.concurrent.TimeUnit;

public class TempBan implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("ban", "Ban a member from the server")
                .addOption(OptionType.USER, "target", "The user to ban", true)
                .addOption(OptionType.STRING, "reason", "Reason for the ban", false)
                .addOption(OptionType.INTEGER, "days", "Number of days of messages to delete (0-7)", false)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.BAN_MEMBERS))
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member target = event.getOption("target").getAsMember();
        String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";
        int days = event.getOption("days") != null ? event.getOption("days").getAsInt() : 0;

        if (target == null) {
            event.reply("User not found.").setEphemeral(true).queue();
            return;
        }

        if (!event.getGuild().getSelfMember().canInteract(target)) {
            event.reply("❌ I cannot ban this user (Role Hierarchy).").setEphemeral(true).queue();
            return;
        }

        target.ban(days, TimeUnit.DAYS).reason(reason).queue(
                success -> event.reply("🔨 **" + target.getUser().getName() + "** has been banned. | Reason: " + reason).queue(),
                error -> event.reply("❌ Failed to ban: " + error.getMessage()).setEphemeral(true).queue()
        );
    }
}