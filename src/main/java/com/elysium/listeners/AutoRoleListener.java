package com.elysium.listeners;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class AutoRoleListener extends ListenerAdapter {
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        // Replace "Member" with the exact name of the role in your server
        Role role = event.getGuild().getRolesByName("Community Member", true).stream().findFirst().orElse(null);

        if (role != null) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
    }
}