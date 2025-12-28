package com.elysium.commands;

import com.elysium.ICommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TranslateCommand implements ICommand {
    @Override
    public SlashCommandData getData() {
        return Commands.slash("translate", "Translate any text to English")
                .addOption(OptionType.STRING, "text", "The text you want to translate", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String textToTranslate = event.getOption("text").getAsString();

        event.deferReply().queue();

        try {
            String translatedText = translate(textToTranslate);

            EmbedBuilder eb = new EmbedBuilder()
                    .setTitle("Translation")
                    .addField("Original", textToTranslate, false)
                    .addField("English", translatedText, false)
                    .setColor(Color.WHITE)
                    .setFooter("Translated via Google");

            event.getHook().sendMessageEmbeds(eb.build()).queue();
        } catch (Exception e) {
            event.getHook().sendMessage("Sorry, I couldn't translate that right now.").queue();
            e.printStackTrace();
        }
    }

    private String translate(String text) throws Exception {
        // This is a special unofficial endpoint that doesn't need a key
        String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=en&dt=t&q="
                + URLEncoder.encode(text, StandardCharsets.UTF_8);

        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // The response is a weird JSON array like [[["Translated","Original",...]]]
        // This simple parsing finds the first quoted string in the first array
        String raw = response.toString();
        return raw.substring(raw.indexOf("\"") + 1, raw.indexOf("\"", raw.indexOf("\"") + 1));
    }
}