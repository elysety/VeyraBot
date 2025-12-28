package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.Random;

public class RollCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("roll", "Roll a die")
                .addOption(OptionType.INTEGER, "sides", "How many sides? (Default is 6)", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int sides = event.getOption("sides") != null ? event.getOption("sides").getAsInt() : 6;

        if (sides < 2) {
            event.reply("A die needs at least 2 sides!").setEphemeral(true).queue();
            return;
        }

        int result = new Random().nextInt(sides) + 1;
        event.reply("🎲 You rolled a **" + result + "** (1-" + sides + ")").queue();
    }
}