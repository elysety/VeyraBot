package com.elysium.commands;

import com.elysium.ICommand;
import com.elysium.listeners.WelcomeLeaveListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SetLeaveCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("set-leave", "Set the channel for leave messages")
                .addOption(OptionType.CHANNEL, "channel", "The channel for goodbyes", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String channelId = event.getOption("channel").getAsChannel().getId();
        WelcomeLeaveListener.setLeaveChannelId(channelId);

        event.reply("✅ Goodbye messages will now be sent in <#" + channelId + ">").queue();
    }
}