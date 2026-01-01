// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pl.molot.nip.NamedItemPreserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance;

    private ConfigManager() {}

    public static void load(Path configDir) {
        try {
            Files.createDirectories(configDir);
            Path configFile = configDir.resolve("named-item-preserver.json");

            if (Files.exists(configFile)) {
                String json = Files.readString(configFile, StandardCharsets.UTF_8);
                instance = GSON.fromJson(json, Config.class);
                NamedItemPreserver.LOGGER.info("Loaded config from {}", configFile);
            } else {
                instance = new Config();
                save(configFile);
                NamedItemPreserver.LOGGER.info("Created default config at {}", configFile);
            }
        } catch (IOException e) {
            NamedItemPreserver.LOGGER.error("Failed to load config", e);
            instance = new Config();
        }

        NamedItemPreserver.LOGGER.info("Config: {}", instance);
    }

    private static void save(Path configFile) {
        try {
            String json = GSON.toJson(instance);
            Files.writeString(configFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            NamedItemPreserver.LOGGER.error("Failed to save config", e);
        }
    }

    public static Config get() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }
}
