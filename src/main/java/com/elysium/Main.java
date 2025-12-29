package com.elysium;

import com.elysium.commands.*;
import com.elysium.listeners.*;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.bson.Document;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.conversions.Bson;

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
        String mongoUri;

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            prop.load(fis);
            token = prop.getProperty("token");
            testGuildId = prop.getProperty("guild_id");
            mongoUri = prop.getProperty("mongo_uri");
        } catch (IOException e) {
            System.err.println("Could not load config.properties!");
            return;
        }

        MongoClient mongoClient = MongoClients.create(mongoUri);
        MongoDatabase database = mongoClient.getDatabase("VeyraBot");
        MongoCollection<Document> logColl = database.getCollection("logs");

        try {
            Bson ping = new BsonDocument("ping", new BsonInt64(1));
            database.runCommand(ping);
            System.out.println("✅ MongoDB: Connected successfully.");
        } catch (MongoException e) {
            System.err.println("❌ MongoDB: Connection failed! " + e.getMessage());
        }

        CommandManager manager = new CommandManager();
        initCommands(manager, logColl);

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_VOICE_STATES
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(
                        manager,
                        new AutoRoleListener(),
                        new WelcomeLeaveListener(),
                        new MessageLoggerListener(logColl),
                        new VoiceLoggerListener(logColl),
                        new JoinToCreateListener(logColl)
                )
                .build()
                .awaitReady();

        List<SlashCommandData> commandData = manager.getCommands().stream()
                .map(ICommand::getData)
                .collect(Collectors.toList());

        if (testGuildId != null && !testGuildId.isEmpty()) {
            Guild devGuild = jda.getGuildById(testGuildId);
            if (devGuild != null) {
                devGuild.updateCommands().addCommands(commandData).queue();
            }
        }

        runStatusCycle(jda);
        System.out.println("Veyra 1.0 is now online.");
    }

    private static void initCommands(CommandManager manager, MongoCollection<Document> logColl) {
        manager.addCommand(new CommandManager.PingCommand());
        manager.addCommand(new CommandManager.PurgeCommand());
        manager.addCommand(new CommandManager.TimeoutCommand());
        manager.addCommand(new CommandManager.KickCommand());
        manager.addCommand(new CommandManager.HelpCommand(manager));
        manager.addCommand(new CommandManager.SetupLogsCommand(logColl));
        manager.addCommand(new CommandManager.SetupJTCCommand(logColl));

        manager.addCommand(new com.elysium.LookupCommand());
        manager.addCommand(new BanCommand());
        manager.addCommand(new RoleCommand());
        manager.addCommand(new ServerInfoCommand());
        manager.addCommand(new UserInfoCommand());
        manager.addCommand(new BotInfoCommand());
        manager.addCommand(new SetWelcomeCommand());
        manager.addCommand(new SetLeaveCommand());
        manager.addCommand(new EmbedBuilderCommand());
        manager.addCommand(new TempBan());

        manager.addCommand(new RockPaperScissorsCommand());
        manager.addCommand(new CoinflipCommand());
        manager.addCommand(new RollCommand());
        manager.addCommand(new EightBallCommand());
        manager.addCommand(new PollCommand());
        manager.addCommand(new TranslateCommand());
        manager.addCommand(new MemberCountCommand());
    }

    private static void runStatusCycle(JDA jda) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Random rand = new Random();
            String[] phrases = {
                    "Veyra 1.0 | Made by elysety",
                    "Monitoring Elysium 🌌",
                    "Developed with ❤️ by elysety"
            };
            String status = phrases[rand.nextInt(phrases.length)];
            jda.getPresence().setActivity(Activity.customStatus(status));
        }, 0, 1, TimeUnit.MINUTES);
    }
}