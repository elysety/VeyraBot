package com.elysium;

import com.elysium.commands.*;
import com.elysium.listeners.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Properties prop = new Properties();
        String token;
        String testGuildId;

        // Load config.properties
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            prop.load(fis);
            token = prop.getProperty("token");
            testGuildId = prop.getProperty("guild_id");
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not find config.properties in project root!");
            return;
        }

        if (token == null || token.isEmpty()) {
            System.err.println("ERROR: Token is missing in config.properties!");
            return;
        }

        CommandManager manager = new CommandManager();
        initCommands(manager);

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(manager, new AutoRoleListener(), new WelcomeLeaveListener(), new MessageLoggerListener())
                .build()
                .awaitReady();

        List<SlashCommandData> commandData = manager.getCommands().stream()
                .map(ICommand::getData)
                .collect(Collectors.toList());

        // Sync commands to your dev guild
        if (testGuildId != null && !testGuildId.isEmpty()) {
            Guild devGuild = jda.getGuildById(testGuildId);
            if (devGuild != null) {
                devGuild.updateCommands().addCommands(commandData).queue();
            }
        }

        runStatusCycle(jda);
        System.out.println(">>> Veyra 1.0 is now active | Dev: elysety");
    }

    private static void initCommands(CommandManager manager) {
        manager.addCommand(new PingCommand());
        manager.addCommand(new CommandManager.KickCommand());
        manager.addCommand(new BanCommand());
        manager.addCommand(new TimeoutCommand());
        manager.addCommand(new CommandManager.HelpCommand(manager   ));
        manager.addCommand(new RoleCommand());
        manager.addCommand(new PurgeCommand());
        manager.addCommand(new ServerInfoCommand());
        manager.addCommand(new UserInfoCommand());
        manager.addCommand(new RockPaperScissorsCommand());
        manager.addCommand(new CoinflipCommand());
        manager.addCommand(new RollCommand());
        manager.addCommand(new EightBallCommand());
        manager.addCommand(new TranslateCommand());
        manager.addCommand(new BotInfoCommand());
        manager.addCommand(new SetWelcomeCommand());
        manager.addCommand(new PollCommand());
        manager.addCommand(new MemberCountCommand());
        manager.addCommand(new SetLeaveCommand());
    }

    private static void runStatusCycle(JDA jda) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Random rand = new Random();
            String[] phrases = {
                    "Veyra 1.0 | Made by elysety",
                    "Monitoring Elysium 🌌",
                    "Is %s actually a secret agent? 🕵️",
                    "Developed with ❤️ by elysety"
            };

            jda.getGuilds().forEach(guild -> {
                List<Member> users = guild.getMembers().stream()
                        .filter(m -> !m.getUser().isBot())
                        .collect(Collectors.toList());

                if (!users.isEmpty()) {
                    Member target = users.get(rand.nextInt(users.size()));
                    String raw = phrases[rand.nextInt(phrases.length)];
                    String status = raw.contains("%s") ? String.format(raw, target.getEffectiveName()) : raw;
                    jda.getPresence().setActivity(Activity.customStatus(status));
                }
            });
        }, 0, 1, TimeUnit.MINUTES);
    }
}