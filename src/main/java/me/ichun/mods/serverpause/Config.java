package me.ichun.mods.serverpause;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean pauseWhenAllPlayersPaused = true;
    public boolean pauseWhenNoPlayers = false;
    public boolean sendChatMessageWhenPauseStateChanges = false;
    public boolean sendChatMessageWhenPlayerPauseStateChanges = false;

    public static Config load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("multiplayerserverpause.json");
        Config config = new Config();
        try {
            if (Files.exists(path)) {
                Config read = GSON.fromJson(Files.readString(path), Config.class);
                if (read != null) {
                    config = read;
                }
            }
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException | RuntimeException e) {
            ServerPause.LOGGER.warn("Could not read or write {}", path, e);
        }
        return config;
    }
}
