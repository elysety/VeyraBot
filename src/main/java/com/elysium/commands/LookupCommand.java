package com.elysium;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.awt.*;
import java.net.InetAddress;
import java.time.Instant;

public class LookupCommand implements ICommand {
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public SlashCommandData getData() {
        return Commands.slash("lookup", "Get detailed DNS and Geo-location info for an IP or Domain")
                .addOption(OptionType.STRING, "target", "The IP or Domain to lookup", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        String target = event.getOption("target").getAsString();

        try {
            long startTime = System.currentTimeMillis();
            InetAddress address = InetAddress.getByName(target);
            long endTime = System.currentTimeMillis();
            long ms = endTime - startTime;

            String ip = address.getHostAddress();

            Request request = new Request.Builder()
                    .url("http://ip-api.com/json/" + ip + "?fields=status,message,country,regionName,city,zip,lat,lon,timezone,isp,org,as,mobile,proxy,hosting")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new Exception();

                JSONObject json = new JSONObject(response.body().string());

                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("🌐 DNS Lookup: " + target)
                        .setColor(new Color(43, 45, 49))
                        .setTimestamp(Instant.now())
                        .addField("Resolved IP", "`" + ip + "`", true)
                        .addField("DNS Speed", "`" + ms + "ms`", true)
                        .addField("Location", json.getString("city") + ", " + json.getString("country"), true)
                        .addField("ISP", json.getString("isp"), true)
                        .addField("Organization", json.getString("org"), true)
                        .addField("Timezone", json.getString("timezone"), true)
                        .addField("Proxy/VPN", json.getBoolean("proxy") ? "✅ Yes" : "❌ No", true)
                        .addField("Hosting/DC", json.getBoolean("hosting") ? "✅ Yes" : "❌ No", true)
                        .setFooter("Requested by " + event.getUser().getAsTag());

                event.getHook().sendMessageEmbeds(eb.build()).queue();
            }
        } catch (Exception e) {
            event.getHook().sendMessage("❌ Failed to lookup target. Ensure the domain/IP is valid.").queue();
        }
    }
}