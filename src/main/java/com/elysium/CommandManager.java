package com.elysium;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
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
            event.reply("❌ Command not recognized.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            event.reply("❌ You must be in the voice channel to do that.").setEphemeral(true).queue();
            return;
        }

        VoiceChannel vc = member.getVoiceState().getChannel().asVoiceChannel();

        if (event.getModalId().equals("vc:rename_modal")) {
            String newName = event.getValue("new_name").getAsString();
            vc.getManager().setName(newName).queue(s ->
                    event.reply("✅ Channel renamed to: **" + newName + "**").setEphemeral(true).queue());
        }

        if (event.getModalId().equals("vc:limit_modal")) {
            try {
                int limit = Integer.parseInt(event.getValue("limit_value").getAsString());
                if (limit < 0 || limit > 99) {
                    event.reply("❌ Limit must be 0-99.").setEphemeral(true).queue();
                    return;
                }
                vc.getManager().setUserLimit(limit).queue(s ->
                        event.reply("👥 User limit set to: **" + (limit == 0 ? "Unlimited" : limit) + "**").setEphemeral(true).queue());
            } catch (NumberFormatException e) {
                event.reply("❌ That isn't a valid number.").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("vc:")) return;

        Member member = event.getMember();
        if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
            event.reply("❌ You aren't in a voice channel.").setEphemeral(true).queue();
            return;
        }

        VoiceChannel vc = member.getVoiceState().getChannel().asVoiceChannel();

        if (event.getComponentId().equals("vc:claim")) {
            boolean ownerInChannel = vc.getMembers().stream().anyMatch(m -> vc.getPermissionOverride(m) != null &&
                    vc.getPermissionOverride(m).getAllowed().contains(Permission.MANAGE_CHANNEL));

            if (ownerInChannel) {
                event.reply("❌ The owner is still here!").setEphemeral(true).queue();
                return;
            }
            vc.getManager().putMemberPermissionOverride(member.getIdLong(), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT), null).queue();
            event.reply("👑 You are now the owner!").queue();
            return;
        }

        boolean isOwner = vc.getPermissionOverride(member) != null &&
                vc.getPermissionOverride(member).getAllowed().contains(Permission.MANAGE_CHANNEL);

        if (!isOwner) {
            event.reply("❌ Only the owner can use these controls.").setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId()) {
            case "vc:lock" -> {
                vc.getManager().putRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
                event.reply("🔒 Channel locked.").setEphemeral(true).queue();
            }
            case "vc:unlock" -> {
                vc.getManager().putRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), EnumSet.of(Permission.VOICE_CONNECT), null).queue();
                event.reply("🔓 Channel unlocked.").setEphemeral(true).queue();
            }
            case "vc:rename" -> {
                TextInput nameInput = TextInput.create("new_name", "New Name", TextInputStyle.SHORT).build();
                event.replyModal(Modal.create("vc:rename_modal", "Rename Channel").addComponents(ActionRow.of(nameInput)).build()).queue();
            }
            case "vc:limit" -> {
                TextInput limitInput = TextInput.create("limit_value", "Limit (0-99)", TextInputStyle.SHORT).setPlaceholder("0 = Unlimited").build();
                event.replyModal(Modal.create("vc:limit_modal", "Set Limit").addComponents(ActionRow.of(limitInput)).build()).queue();
            }
            case "vc:hide" -> {
                vc.getManager().putRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                event.reply("👻 Channel hidden.").setEphemeral(true).queue();
            }
        }
    }

    public static class HelpCommand implements ICommand {
        private final CommandManager manager;
        public HelpCommand(CommandManager manager) { this.manager = manager; }
        @Override public SlashCommandData getData() { return Commands.slash("help", "View all commands"); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("Veyra Help").setColor(new Color(43, 45, 49));
            manager.getCommands().forEach(c -> eb.addField("/" + c.getData().getName(), c.getData().getDescription(), false));
            event.replyEmbeds(eb.build()).queue();
        }
    }

    public static class PingCommand implements ICommand {
        @Override public SlashCommandData getData() { return Commands.slash("ping", "Check bot speed"); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            event.reply("🏓 Pong! " + event.getJDA().getGatewayPing() + "ms").queue();
        }
    }

    public static class PurgeCommand implements ICommand {
        @Override public SlashCommandData getData() { return Commands.slash("purge", "Delete messages").addOption(OptionType.INTEGER, "amount", "1-100", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            int amount = event.getOption("amount").getAsInt();
            event.getChannel().getIterableHistory().takeAsync(amount).thenAccept(messages -> {
                event.getChannel().purgeMessages(messages);
                event.reply("🧹 Deleted " + messages.size() + " messages.").setEphemeral(true).queue();
            });
        }
    }

    public static class SetupLogsCommand implements ICommand {
        private final MongoCollection<Document> coll;
        public SetupLogsCommand(MongoCollection<Document> coll) { this.coll = coll; }
        @Override public SlashCommandData getData() { return Commands.slash("setup-logs", "Set log channel").addOption(OptionType.CHANNEL, "channel", "Channel", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            String id = event.getOption("channel").getAsChannel().getId();
            coll.replaceOne(new Document("guildId", event.getGuild().getId()), new Document("guildId", event.getGuild().getId()).append("logChannelId", id), new ReplaceOptions().upsert(true));
            event.reply("✅ Logs set to <#" + id + ">").queue();
        }
    }

    public static class SetupJTCCommand implements ICommand {
        private final MongoCollection<Document> coll;
        public SetupJTCCommand(MongoCollection<Document> coll) { this.coll = coll; }
        @Override public SlashCommandData getData() { return Commands.slash("setup-jtc", "Set JTC Hub").addOption(OptionType.CHANNEL, "channel", "Channel", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            String id = event.getOption("channel").getAsChannel().getId();
            coll.replaceOne(new Document("guildId", event.getGuild().getId()), new Document("guildId", event.getGuild().getId()).append("jtcHubId", id), new ReplaceOptions().upsert(true));
            event.reply("✅ JTC Hub set to <#" + id + ">").queue();
        }
    }
}