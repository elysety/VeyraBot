package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class RoleCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("role", "Add a role to a member")
                .addOption(OptionType.USER, "target", "The user to give the role to", true)
                .addOption(OptionType.ROLE, "role", "The role to add", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
                .setContexts(InteractionContextType.GUILD);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member target = event.getOption("target").getAsMember();
        Role role = event.getOption("role").getAsRole();

        if (target == null) {
            event.reply("Could not find that member.").setEphemeral(true).queue();
            return;
        }

        //Hierarchy check
        if (!event.getGuild().getSelfMember().canInteract(role)) {
            event.reply("I cannot give this role because it is higher than my highest role!")
                    .setEphemeral(true).queue();
            return;
        }

        event.getGuild().addRoleToMember(target, role).queue(
                success -> event.reply("Successfully added the role **" + role.getName() + "** to " + target.getAsMention()).queue(),
                error -> event.reply("Failed to add role: " + error.getMessage()).setEphemeral(true).queue()
        );
    }
}