package com.elysium.listeners;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;

import java.awt.Color;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Instant;

public class MessageLoggerListener extends ListenerAdapter {
    private final MongoCollection<Document> logColl;
    // High-performance thread-safe map for caching
    private final Map<Long, String> cache = new ConcurrentHashMap<>();

    public MessageLoggerListener(MongoCollection<Document> logColl) {
        this.logColl = logColl;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        // Asynchronous caching to keep message processing fast
        CompletableFuture.runAsync(() -> {
            cache.put(event.getMessageIdLong(), event.getMessage().getContentRaw());
            if (cache.size() > 5000) cache.clear();
        });
    }

    @Override
    public void onMessageDelete(MessageDeleteEvent event) {

        CompletableFuture.runAsync(() -> {
            Document doc = logColl.find(new Document("guildId", event.getGuild().getId())).first();
            if (doc == null) return;

            String channelId = doc.getString("logChannelId");
            TextChannel logChannel = event.getGuild().getTextChannelById(channelId);
            if (logChannel == null) return;

            String content = cache.getOrDefault(event.getMessageIdLong(), "*Content not found in cache*");

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(event.getGuild().getName(), null, event.getGuild().getIconUrl())
                    .setDescription(String.format("🔴 | **%s** (%s) Message Deleted in %s",
                            "User", event.getMessageId(), event.getChannel().getAsMention()))
                    .addField("Content", content, false)
                    .setColor(new Color(43, 45, 49))
                    .setTimestamp(Instant.now())
                    .setFooter("Veyra Logs | Latency Optimized");

            logChannel.sendMessageEmbeds(eb.build()).queue();
        });
    }
}