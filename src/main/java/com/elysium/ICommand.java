package com.elysium;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Every command class we create will implement this interface.
 */
public interface ICommand {

    SlashCommandData getData();
    void execute(SlashCommandInteractionEvent event);

}