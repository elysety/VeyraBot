package com.elysium;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager extends ListenerAdapter {
    private final Map<String, ICommand> commands = new ConcurrentHashMap<>();

    public void addCommand(ICommand cmd) {
        commands.put(cmd.getData().getName(), cmd);
    }

    public List<ICommand> getCommands() {
        return new ArrayList<>(commands.values());
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        ICommand cmd = commands.get(event.getName());
        if (cmd != null) {
            cmd.execute(event);
        } else {
            event.reply("Command not recognized.").setEphemeral(true).queue();
        }
    }

    // --- LOG SETUP COMMAND ---
    public static class SetupLogsCommand implements ICommand {
        private final MongoCollection<Document> collection;

        public SetupLogsCommand(MongoCollection<Document> collection) {
            this.collection = collection;
        }

        @Override
        public SlashCommandData getData() {
            return Commands.slash("setup-logs", "Set the channel for detailed server logs")
                    .addOption(OptionType.CHANNEL, "channel", "The log channel", true)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .setContexts(InteractionContextType.GUILD);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            String channelId = event.getOption("channel").getAsChannel().getId();
            String guildId = event.getGuild().getId();

            Document query = new Document("guildId", guildId);
            Document update = new Document("guildId", guildId).append("logChannelId", channelId);

            collection.replaceOne(query, update, new ReplaceOptions().upsert(true));

            event.reply("✅ Detailed logs will now be sent to <#" + channelId + ">").queue();
        }
    }

    public static class KickCommand implements ICommand {
        @Override
        public SlashCommandData getData() {
            return Commands.slash("kick", "Kick a member from the server")
                    .addOption(OptionType.USER, "target", "The user to kick", true)
                    .addOption(OptionType.STRING, "reason", "The reason for kicking", false)
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS))
                    .setContexts(InteractionContextType.GUILD);
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            Member target = event.getOption("target").getAsMember();
            String reason = event.getOption("reason") != null ? event.getOption("reason").getAsString() : "No reason provided";

            if (target == null) {
                event.reply("User not found.").setEphemeral(true).queue();
                return;
            }

            target.kick().reason(reason).queue(
                    success -> event.reply("Successfully kicked " + target.getUser().getName()).queue(),
                    error -> event.reply("Failed: " + error.getMessage()).setEphemeral(true).queue()
            );
        }
    }
}