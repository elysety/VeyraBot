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
            event.reply("Command not recognized.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("vc:rename_modal")) {
            VoiceChannel vc = (VoiceChannel) event.getChannel();
            String newName = Objects.requireNonNull(event.getValue("new_name")).getAsString();
            vc.getManager().setName(newName).queue(s ->
                    event.reply("✅ Channel renamed to: **" + newName + "**").setEphemeral(true).queue());
        }

        if (event.getModalId().equals("vc:limit_modal")) {
            VoiceChannel vc = (VoiceChannel) event.getChannel();
            try {
                int limit = Integer.parseInt(Objects.requireNonNull(event.getValue("limit_value")).getAsString());
                if (limit < 0 || limit > 99) {
                    event.reply("❌ Limit must be 0-99.").setEphemeral(true).queue();
                    return;
                }
                vc.getManager().setUserLimit(limit).queue(s ->
                        event.reply("👥 User limit set to: **" + (limit == 0 ? "Unlimited" : limit) + "**").setEphemeral(true).queue());
            } catch (Exception e) {
                event.reply("❌ Invalid number.").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("vc:")) return;
        VoiceChannel vc = (VoiceChannel) event.getChannel();

        if (event.getComponentId().equals("vc:claim")) {
            boolean ownerInChannel = vc.getMembers().stream().anyMatch(m -> vc.getPermissionOverride(m) != null && Objects.requireNonNull(vc.getPermissionOverride(m)).getAllowed().contains(Permission.MANAGE_CHANNEL));
            if (ownerInChannel) {
                event.reply("❌ The owner is still in the channel!").setEphemeral(true).queue();
                return;
            }
            vc.getManager().putMemberPermissionOverride(event.getMember().getIdLong(), EnumSet.of(Permission.MANAGE_CHANNEL, Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS), null).queue();
            event.reply("👑 You are now the owner!").queue();
            return;
        }

        boolean isOwner = vc.getPermissionOverride(event.getMember()) != null &&
                Objects.requireNonNull(vc.getPermissionOverride(event.getMember())).getAllowed().contains(Permission.MANAGE_CHANNEL);

        if (!isOwner) {
            event.reply("❌ Only the owner can manage this channel.").setEphemeral(true).queue();
            return;
        }

        switch (event.getComponentId()) {
            case "vc:lock" -> {
                vc.getManager().putRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, EnumSet.of(Permission.VOICE_CONNECT)).queue();
                event.reply("🔒 Locked").setEphemeral(true).queue();
            }
            case "vc:unlock" -> {
                vc.getManager().putRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), EnumSet.of(Permission.VOICE_CONNECT), null).queue();
                event.reply("🔓 Unlocked").setEphemeral(true).queue();
            }
            case "vc:rename" -> {
                TextInput nameInput = TextInput.create("new_name", "New Name", TextInputStyle.SHORT).build();
                event.replyModal(Modal.create("vc:rename_modal", "Rename").addComponents(ActionRow.of(nameInput)).build()).queue();
            }
            case "vc:limit" -> {
                TextInput limitInput = TextInput.create("limit_value", "Limit (0-99)", TextInputStyle.SHORT).setPlaceholder("0 for unlimited").build();
                event.replyModal(Modal.create("vc:limit_modal", "Set Limit").addComponents(ActionRow.of(limitInput)).build()).queue();
            }
            case "vc:hide" -> {
                vc.getManager().putRolePermissionOverride(event.getGuild().getPublicRole().getIdLong(), null, EnumSet.of(Permission.VIEW_CHANNEL)).queue();
                event.reply("👻 Hidden").setEphemeral(true).queue();
            }
        }
    }

    public static class PingCommand implements ICommand {
        @Override public SlashCommandData getData() { return Commands.slash("ping", "Check latency"); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            event.reply("Pinging...").queue(m -> m.editOriginal("Gateway: " + event.getJDA().getGatewayPing() + "ms").queue());
        }
    }

    public static class PurgeCommand implements ICommand {
        @Override public SlashCommandData getData() { return Commands.slash("purge", "Clear messages").addOption(OptionType.INTEGER, "amount", "1-100", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            int amount = event.getOption("amount").getAsInt();
            event.getChannel().getIterableHistory().takeAsync(amount).thenAccept(messages -> {
                event.getChannel().purgeMessages(messages);
                event.reply("🧹 Deleted " + messages.size()).setEphemeral(true).queue();
            });
        }
    }

    public static class TimeoutCommand implements ICommand {
        @Override public SlashCommandData getData() { return Commands.slash("timeout", "Timeout user").addOption(OptionType.USER, "target", "User", true).addOption(OptionType.INTEGER, "minutes", "Mins", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            Member target = event.getOption("target").getAsMember();
            int mins = event.getOption("minutes").getAsInt();
            if (target != null) target.timeoutFor(Duration.ofMinutes(mins)).queue(s -> event.reply("🔇 Done").queue());
        }
    }

    public static class HelpCommand implements ICommand {
        private final CommandManager manager;
        public HelpCommand(CommandManager manager) { this.manager = manager; }
        @Override public SlashCommandData getData() { return Commands.slash("help", "Help menu"); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            EmbedBuilder eb = new EmbedBuilder().setTitle("Help").setColor(Color.DARK_GRAY);
            manager.getCommands().forEach(c -> eb.addField("/" + c.getData().getName(), c.getData().getDescription(), false));
            event.replyEmbeds(eb.build()).queue();
        }
    }

    public static class KickCommand implements ICommand {
        @Override public SlashCommandData getData() { return Commands.slash("kick", "Kick").addOption(OptionType.USER, "target", "User", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.KICK_MEMBERS)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            Member t = event.getOption("target").getAsMember();
            if (t != null) t.kick().queue(s -> event.reply("✅ Kicked").queue());
        }
    }

    public static class SetupLogsCommand implements ICommand {
        private final MongoCollection<Document> coll;
        public SetupLogsCommand(MongoCollection<Document> coll) { this.coll = coll; }
        @Override public SlashCommandData getData() { return Commands.slash("setup-logs", "Logs").addOption(OptionType.CHANNEL, "channel", "Ch", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            String id = event.getOption("channel").getAsChannel().getId();
            coll.replaceOne(new Document("guildId", event.getGuild().getId()), new Document("guildId", event.getGuild().getId()).append("logChannelId", id), new ReplaceOptions().upsert(true));
            event.reply("✅ Set").queue();
        }
    }

    public static class SetupJTCCommand implements ICommand {
        private final MongoCollection<Document> coll;
        public SetupJTCCommand(MongoCollection<Document> coll) { this.coll = coll; }
        @Override public SlashCommandData getData() { return Commands.slash("setup-jtc", "JTC").addOption(OptionType.CHANNEL, "channel", "Ch", true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)); }
        @Override public void execute(SlashCommandInteractionEvent event) {
            String id = event.getOption("channel").getAsChannel().getId();
            coll.replaceOne(new Document("guildId", event.getGuild().getId()), new Document("guildId", event.getGuild().getId()).append("jtcHubId", id), new ReplaceOptions().upsert(true));
            event.reply("✅ Set").queue();
        }
    }
}