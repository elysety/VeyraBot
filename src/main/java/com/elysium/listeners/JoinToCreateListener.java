package com.elysium.listeners;

import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JoinToCreateListener extends ListenerAdapter {
    private final MongoCollection<Document> logColl;
    private final Map<Long, Long> channelOwners = new ConcurrentHashMap<>();

    public JoinToCreateListener(MongoCollection<Document> logColl) {
        this.logColl = logColl;
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        Document doc = logColl.find(new Document("guildId", event.getGuild().getId())).first();
        if (doc == null || !doc.containsKey("jtcHubId")) return;

        String hubId = doc.getString("jtcHubId");
        Member member = event.getMember();

        if (event.getChannelJoined() != null && event.getChannelJoined().getId().equals(hubId)) {
            event.getGuild().createVoiceChannel(member.getUser().getName() + "'s Room", event.getChannelJoined().getParentCategory())
                    .addMemberPermissionOverride(member.getIdLong(), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS), null)
                    .queue(newChannel -> {
                        channelOwners.put(newChannel.getIdLong(), member.getIdLong());
                        event.getGuild().moveVoiceMember(member, newChannel).queue();
                        sendControlPanel(newChannel, member);
                    });
        }

        if (event.getChannelLeft() != null && channelOwners.containsKey(event.getChannelLeft().getIdLong())) {
            VoiceChannel vc = (VoiceChannel) event.getChannelLeft();
            if (vc.getMembers().isEmpty()) {
                vc.delete().queue();
                channelOwners.remove(vc.getIdLong());
            }
        }
    }

    private void sendControlPanel(VoiceChannel channel, Member owner) {
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🔊 Voice Control Panel")
                .setDescription("Manage settings or use Claim if the owner left.")
                .setColor(new Color(43, 45, 49))
                .addField("Owner", owner.getAsMention(), true)
                .addField("Limit", channel.getUserLimit() == 0 ? "Unlimited" : String.valueOf(channel.getUserLimit()), true);

        channel.sendMessageEmbeds(eb.build())
                .addActionRow(
                        Button.secondary("vc:lock", "🔒 Lock"),
                        Button.secondary("vc:unlock", "🔓 Unlock"),
                        Button.secondary("vc:limit", "👥 Limit"),
                        Button.secondary("vc:claim", "👑 Claim"),
                        Button.danger("vc:hide", "👻 Hide")
                ).queue();
    }
}