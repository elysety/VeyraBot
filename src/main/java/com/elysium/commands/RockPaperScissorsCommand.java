package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.Random;

public class RockPaperScissorsCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("rps", "Play Rock Paper Scissors against the bot")
                .addOptions(new OptionData(OptionType.STRING, "choice", "Your move", true)
                        .addChoice("Rock", "rock")
                        .addChoice("Paper", "paper")
                        .addChoice("Scissors", "scissors"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String userChoice = event.getOption("choice").getAsString();
        String[] botOptions = {"rock", "paper", "scissors"};
        String botChoice = botOptions[new Random().nextInt(3)];

        String result;
        if (userChoice.equals(botChoice)) {
            result = "It's a tie! 🤝";
        } else if ((userChoice.equals("rock") && botChoice.equals("scissors")) ||
                (userChoice.equals("paper") && botChoice.equals("rock")) ||
                (userChoice.equals("scissors") && botChoice.equals("paper"))) {
            result = "You won! 🎉";
        } else {
            result = "You lost! 💀";
        }

        event.reply("You chose: **" + userChoice + "**\nI chose: **" + botChoice + "**\n\n" + result).queue();
    }
}