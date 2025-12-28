package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class MemberCountCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("mc", "Show detailed member count");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long total = event.getGuild().getMemberCount();
        long bots = event.getGuild().getMembers().stream().filter(m -> m.getUser().isBot()).count();
        long humans = total - bots;

        event.reply("📊 **Member Stats for " + event.getGuild().getName() + "**\n" +
                "👥 Total: " + total + "\n" +
                "👤 Humans: " + humans + "\n" +
                "🤖 Bots: " + bots).queue();
    }
}