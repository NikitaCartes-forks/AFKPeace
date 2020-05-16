package amerebagatelle.github.io.afkpeace.settings;

import java.io.*;
import java.util.Properties;

public class SettingsManager {
    public static File settingsFile = new File("config/afkpeace.properties");

    public static void initSettings() {
        // Init settings file if it doesn't exist
        if (!settingsFile.exists()) {
            File configDir = new File("config/");
            if (!configDir.isDirectory()) {
                //noinspection ResultOfMethodCallIgnored
                configDir.mkdir();
            }
            try {
                boolean fileCreated = settingsFile.createNewFile();

                if (fileCreated) {
                    Properties prop = new Properties();
                    prop.put("reconnectEnabled", "false");
                    prop.put("secondsBetweenReconnectAttempts", "3");
                    prop.put("reconnectAttemptNumber", "10");
                    prop.put("damageLogoutEnabled", "false");

                    BufferedWriter writer = new BufferedWriter(new FileWriter(settingsFile));
                    prop.store(writer, null);
                    writer.flush();
                    writer.close();
                } else {
                    throw new IOException();
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not create settings file for AFKPeace!");
            }
            }
    }

    public static String loadSetting(String setting) {
        BufferedReader reader;
        Properties prop = new Properties();

        try {
            reader = new BufferedReader(new FileReader(settingsFile));
            prop.load(reader);
            reader.close();

            return prop.getProperty(setting);
        } catch (IOException e) {
            throw new RuntimeException("Can't read settings for AFKPeace!");
        }
    }

    public static void writeSetting(String key, String setpoint) {
        Properties prop = new Properties();
        BufferedReader reader;
        BufferedWriter writer;

        try {
            reader = new BufferedReader(new FileReader(settingsFile));
            prop.load(reader);
            reader.close();

            prop.setProperty(key, setpoint);

            writer = new BufferedWriter(new FileWriter(settingsFile));
            prop.store(writer, null);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Can't write setting...");
        }
    }

    public static void activateAFKMode() {
        writeSetting("reconnectEnabled", "true");
        writeSetting("damageLogoutEnabled", "true");
    }

    public static void disableAFKMode() {
        writeSetting("reconnectEnabled", "false");
        writeSetting("damageLogoutEnabled", "false");
    }
}