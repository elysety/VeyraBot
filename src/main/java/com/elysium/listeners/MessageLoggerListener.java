package com.elysium.listeners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.awt.Color;

public class MessageLoggerListener extends ListenerAdapter {
    // You can set this via a command later, or hardcode it for now
    private final String logChannelId = "YOUR_LOG_CHANNEL_ID";

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        TextChannel logChannel = event.getGuild().getTextChannelById(logChannelId);
        if (logChannel == null) return;

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🗑️ Message Deleted")
                .setDescription("A message was deleted in " + event.getChannel().getAsMention())
                .addField("Message ID", event.getMessageId(), false)
                .setColor(Color.RED)
                .setTimestamp(java.time.Instant.now());

        logChannel.sendMessageEmbeds(eb.build()).queue();
    }
}