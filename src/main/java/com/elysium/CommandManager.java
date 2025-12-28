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

    public static class HelpCommand implements ICommand {
        private final CommandManager manager;

        public HelpCommand(CommandManager manager) {
            this.manager = manager;
        }

        @Override
        public SlashCommandData getData() {
            return Commands.slash("help", "See a list of all bot commands");
        }

        @Override
        public void execute(SlashCommandInteractionEvent event) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🤖 Bot Help Menu")
                    .setDescription("Here is a list of all available slash commands:")
                    .setColor(Color.CYAN)
                    .setThumbnail(event.getJDA().getSelfUser().getEffectiveAvatarUrl());

            for (ICommand cmd : manager.getCommands()) {
                embed.addField("/" + cmd.getData().getName(), cmd.getData().getDescription(), false);
            }

            event.replyEmbeds(embed.build()).queue();
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
                event.reply("User not found in this server.").setEphemeral(true).queue();
                return;
            }

            target.kick().reason(reason).queue(
                    success -> event.reply("Successfully kicked " + target.getUser().getName()).queue(),
                    error -> event.reply("Failed to kick: " + error.getMessage()).setEphemeral(true).queue()
            );
        }
    }
}