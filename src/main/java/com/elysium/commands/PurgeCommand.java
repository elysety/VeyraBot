package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PurgeCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("purge", "Delete multiple messages at once")
                .addOption(OptionType.INTEGER, "amount", "How many messages to delete (1-100)", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int amount = event.getOption("amount").getAsInt();

        if (amount < 1 || amount > 100) {
            event.reply("Please choose a number between 1 and 100.").setEphemeral(true).queue();
            return;
        }

        event.getChannel().getIterableHistory().takeAsync(amount).thenAccept(messages -> {
            event.getChannel().purgeMessages(messages);
            event.reply("Successfully deleted " + messages.size() + " messages.").setEphemeral(true).queue();
        });
    }
}