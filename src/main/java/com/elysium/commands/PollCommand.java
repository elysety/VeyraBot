package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.awt.Color;

public class PollCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("poll", "Create a simple yes/no poll")
                .addOption(OptionType.STRING, "question", "What are we voting on?", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String question = event.getOption("question").getAsString();

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("📊 Community Poll")
                .setDescription(question)
                .addField("How to vote:", "React with 1️⃣ for Yes or 2️⃣ for No", false)
                .setColor(Color.ORANGE)
                .setFooter("Poll started by " + event.getUser().getName());

        event.replyEmbeds(eb.build()).queue(interactionHook -> {
            interactionHook.retrieveOriginal().queue(message -> {
                message.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromFormatted("1️⃣")).queue();
                message.addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromFormatted("2️⃣")).queue();
            });
        });
    }
}