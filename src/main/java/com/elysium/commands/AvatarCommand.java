package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.awt.Color;

public class AvatarCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("avatar", "View a user's avatar")
                .addOption(OptionType.USER, "target", "The user to see", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        User user = event.getOption("target") != null ? event.getOption("target").getAsUser() : event.getUser();

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(user.getName() + "'s Avatar")
                .setImage(user.getEffectiveAvatarUrl() + "?size=1024")
                .setColor(Color.BLACK);

        event.replyEmbeds(eb.build()).queue();
    }
}