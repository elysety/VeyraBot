package com.elysium.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import java.awt.Color;

public class WelcomeLeaveListener extends ListenerAdapter {
    private static String welcomeChannelId = null;
    private static String leaveChannelId = null; // New variable

    public static void setWelcomeChannelId(String id) { welcomeChannelId = id; }
    public static void setLeaveChannelId(String id) { leaveChannelId = id; }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (welcomeChannelId == null) return;
        TextChannel channel = event.getGuild().getTextChannelById(welcomeChannelId);
        if (channel == null) return;

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🌸 New Member! 🌸")
                .setDescription("Welcome " + event.getMember().getAsMention() + "! You are member **#" + event.getGuild().getMemberCount() + "**!")
                .setImage("https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNHJndnZ3ZzRyeHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4JmVwPXYxX2ludGVybmFsX2dpZl9ieV9pZCZjdD1n/m8sST6E1m9L9pXW50S/giphy.gif")
                .setColor(Color.PINK);

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (leaveChannelId == null) return; // Uses the new ID
        TextChannel channel = event.getGuild().getTextChannelById(leaveChannelId);
        if (channel == null) return;

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("💔 Someone Left...")
                .setDescription("**" + event.getUser().getName() + "** just left the server. We'll miss you!")
                .setImage("https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExNHJndnZ3ZzRyeHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4JmVwPXYxX2ludGVybmFsX2dpZl9ieV9pZCZjdD1n/OPU6wUKARAyWVmCyLs/giphy.gif")
                .setColor(Color.DARK_GRAY);

        channel.sendMessageEmbeds(eb.build()).queue();
    }
}