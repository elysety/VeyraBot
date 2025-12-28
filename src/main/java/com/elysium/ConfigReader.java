package com.elysium;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private Properties properties = new Properties();

    public ConfigReader() {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Could not find config.properties!");
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}