package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import java.util.Random;

public class CoinflipCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("coinflip", "Flip a coin");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        boolean isHeads = new Random().nextBoolean();
        String result = isHeads ? "Heads" : "Tails";
        String icon = isHeads ? "🟡" : "⚪";

        event.reply("The coin landed on... **" + result + "** " + icon).queue();
    }
}