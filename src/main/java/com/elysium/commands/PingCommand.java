package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("ping", "Check the bot's current response time.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        event.reply("Pong! 🏓 Gateway: " + gatewayPing + "ms").queue();
    }
}