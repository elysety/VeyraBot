package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.awt.Color;
import java.time.format.DateTimeFormatter;

public class UserInfoCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("userinfo", "Get info about a member")
                .addOption(OptionType.USER, "target", "The user to look up", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getOption("target") != null ? event.getOption("target").getAsMember() : event.getMember();

        if (member == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("User Information: " + member.getUser().getName())
                .setThumbnail(member.getUser().getEffectiveAvatarUrl())
                .setColor(Color.CYAN)
                .addField("Joined Server", member.getTimeJoined().format(fmt), true)
                .addField("Account Created", member.getUser().getTimeCreated().format(fmt), true)
                .addField("Roles", String.valueOf(member.getRoles().size()), true);

        event.replyEmbeds(eb.build()).queue();
    }
}