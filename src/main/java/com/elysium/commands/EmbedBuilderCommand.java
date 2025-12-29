package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.components.ActionRow;

public class EmbedBuilderCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("embed", "Create a custom announcement embed")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("Enter the embed title")
                .setRequired(true)
                .build();

        TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Enter the main message")
                .setRequired(true)
                .build();

        TextInput color = TextInput.create("color", "Hex Color (Optional)", TextInputStyle.SHORT)
                .setPlaceholder("#2B2D31")
                .setRequired(false)
                .build();

        Modal modal = Modal.create("custom_embed", "Veyra Embed Builder")
                .addComponents(ActionRow.of(title), ActionRow.of(description), ActionRow.of(color))
                .build();

        event.replyModal(modal).queue();
    }
}