package com.elysium;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

public class DatabaseManager {
    private final MongoCollection<Document> settings;

    public DatabaseManager(String uri) {
        MongoClient client = MongoClients.create(uri);
        MongoDatabase database = client.getDatabase("VeyraDB");
        this.settings = database.getCollection("guild_settings");
    }

    // Saves the channel ID for a specific server
    public void setChannel(String guildId, String type, String channelId) {
        Document doc = new Document("guildId", guildId)
                .append("type", type)
                .append("channelId", channelId);

        // Replace the old setting if it exists, otherwise insert new one
        settings.replaceOne(new Document("guildId", guildId).append("type", type), doc, new ReplaceOptions().upsert(true));
    }

    public String getChannel(String guildId, String type) {
        Document found = settings.find(new Document("guildId", guildId).append("type", type)).first();
        return found != null ? found.getString("channelId") : null;
    }
}