package co.greasygang.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

// Thanks to tuxinal for the ConfigParser class
// https://github.com/tuxinal/girlbossed/blob/1.20/src/main/java/xyz/tuxinal/girlbossed/utils/ConfigParser.java
class Config {
    Boolean enabled;
    String login_url;
    String api_base;
    String api_key;
}

public class WhitelistLinkConfig {
    private static Config config;

    public static void init() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        File configFile = getConfigPath().toFile();

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
                FileWriter writer = new FileWriter(configFile);
                Config configTemplate = new Config();

                configTemplate.enabled = true;
                configTemplate.login_url = "greasygang.co/whitelist";
                configTemplate.api_base = "https://api.greasygang.co";
                configTemplate.api_key = "";

                writer.write(gson.toJson(configTemplate));
                writer.close();
            }

            FileReader reader = new FileReader(configFile);
            config = gson.fromJson(IOUtils.toString(reader), Config.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    public static String getApiKey() {
        return config.api_key;
    }

    public static String getApiBase() {
        return config.api_base;
    }

    public static String getLoginUrl() {
        return config.login_url;
    }

    public static boolean isEnabled() {
        return config.enabled;
    }

    public static void toggleEnabled() {
        config.enabled = !config.enabled;
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("whitelistlink.json");
    }
}
