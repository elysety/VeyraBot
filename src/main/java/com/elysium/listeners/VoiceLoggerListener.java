package com.elysium.listeners;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class VoiceLoggerListener extends ListenerAdapter {
    private final MongoCollection<Document> logColl;

    public VoiceLoggerListener(MongoCollection<Document> logColl) {
        this.logColl = logColl;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        CompletableFuture.runAsync(() -> {
            Document doc = logColl.find(new Document("guildId", event.getGuild().getId())).first();
            if (doc == null) return;

            String channelId = doc.getString("logChannelId");
            var logChannel = event.getGuild().getTextChannelById(channelId);
            if (logChannel == null) return;

            AudioChannelUnion joined = event.getChannelJoined();
            AudioChannelUnion left = event.getChannelLeft();
            String userTag = event.getMember().getUser().getAsTag();
            String userId = event.getMember().getId();

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(userTag + " (" + userId + ")", null, event.getMember().getUser().getEffectiveAvatarUrl())
                    .setColor(new Color(43, 45, 49))
                    .setTimestamp(Instant.now())
                    .setFooter("Veyra Voice Logs");

            if (joined != null && left == null) {
                eb.setDescription("📥 **Joined Voice Channel**\nChannel: " + joined.getAsMention());
            } else if (joined == null && left != null) {
                eb.setDescription("📤 **Left Voice Channel**\nChannel: " + left.getAsMention());
            } else if (joined != null && left != null) {
                eb.setDescription("🔄 **Moved Voice Channel**\nFrom: " + left.getAsMention() + "\nTo: " + joined.getAsMention());
            } else {
                return;
            }

            logChannel.sendMessageEmbeds(eb.build()).queue();
        });
    }
}