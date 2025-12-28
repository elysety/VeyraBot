package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.Random;

public class EightBallCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("8ball", "Ask the magic 8-ball a question")
                .addOption(OptionType.STRING, "question", "What do you want to ask?", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String[] responses = {
                "It is certain.", "Without a doubt.", "Yes – definitely.", "Most likely.", "Yes.",
                "Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
                "Don't count on it.", "My reply is no.", "My sources say no.", "Outlook not so good.", "Very doubtful."
        };

        String question = event.getOption("question").getAsString();
        String answer = responses[new Random().nextInt(responses.length)];

        event.reply("❓ **Question:** " + question + "\n🎱 **Answer:** " + answer).queue();
    }
}