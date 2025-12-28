package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.awt.Color;

public class ServerInfoCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("serverinfo", "See stats for this server");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (event.getGuild() == null) return;

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle(event.getGuild().getName())
                .setThumbnail(event.getGuild().getIconUrl())
                .setColor(Color.ORANGE)
                .addField("Owner", "<@" + event.getGuild().getOwnerId() + ">", true)
                .addField("Members", String.valueOf(event.getGuild().getMemberCount()), true)
                .addField("Boosts", String.valueOf(event.getGuild().getBoostCount()), true);

        event.replyEmbeds(eb.build()).queue();
    }
}