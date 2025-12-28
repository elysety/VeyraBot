package com.elysium.commands;

import com.elysium.ICommand;
import com.elysium.listeners.WelcomeLeaveListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SetWelcomeCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("set-welcome", "Set the channel for welcome/leave messages")
                .addOption(OptionType.CHANNEL, "channel", "The channel to send messages in", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String channelId = event.getOption("channel").getAsChannel().getId();
        WelcomeLeaveListener.setWelcomeChannelId(channelId);

        event.reply("✅ Welcome and Leave messages will now be sent in <#" + channelId + ">").queue();
    }
}