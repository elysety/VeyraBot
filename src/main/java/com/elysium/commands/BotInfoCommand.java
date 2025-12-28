package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.awt.Color;

public class BotInfoCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("about", "Information about this bot");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("Veyra 2.0")
                .setDescription("A high-performance Discord utility bot.")
                .addField("Developer", "elysety", true)
                .addField("Library", "JDA 5.3.0", true)
                .addField("Status", "Operational ✅", true)
                .setFooter("Powered by Elysium Infrastructure")
                .setColor(Color.CYAN);

        event.replyEmbeds(eb.build()).queue();
    }
}