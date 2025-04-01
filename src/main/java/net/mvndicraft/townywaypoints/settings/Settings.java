package net.mvndicraft.townywaypoints.settings;

import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import net.mvndicraft.townywaypoints.TownyWaypoints;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Settings {
    private static CommentedConfiguration config, newConfig;

    public static void loadConfigAndLang() {
        final Path path = TownyWaypoints.getInstance().getDataFolder().toPath().resolve("config.yml");

        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                TownyWaypoints.getInstance().getLogger().warning("Failed to create config.yml!");
                e.printStackTrace();
            }
        }

        // read the config.yml into memory
        config = new CommentedConfiguration(path);
        if (!config.load())
            TownyWaypoints.getInstance().getLogger().warning("Failed to load config.yml!");

        setDefaults(path);
        config.save();

        TownyWaypoints instance = TownyWaypoints.getInstance();
        try {
            Path langFolderPath = Paths.get(instance.getDataFolder().getPath()).resolve("lang");
            TranslationLoader loader = new TranslationLoader(langFolderPath, instance, TownyWaypoints.class);
            loader.load();
            TownyAPI.getInstance().addTranslations(instance, loader.getTranslations());
        } catch (Exception e) {
            instance.getLogger().severe("Language file failed to load! Disabling!");
        }
    }

    public static void addComment(String root, String... comments) {
        newConfig.addComment(root.toLowerCase(), comments);
    }

    private static void setNewProperty(String root, Object value) {
        if (value == null)
            value = "";

        newConfig.set(root.toLowerCase(), value.toString());
    }

    private static void setProperty(String root, Object value) {
        config.set(root.toLowerCase(), value.toString());
    }

    /**
     * Builds a new config reading old config data.
     */
    private static void setDefaults(Path configPath) {

        newConfig = new CommentedConfiguration(configPath);
        newConfig.load();

        for (ConfigNodes root : ConfigNodes.values()) {
            if (root.getComments().length > 0)
                addComment(root.getRoot(), root.getComments());

            if (root == ConfigNodes.VERSION)
                setNewProperty(root.getRoot(), TownyWaypoints.getInstance().getVersion());
            else
                setNewProperty(root.getRoot(), (config.get(root.getRoot().toLowerCase()) != null) ? config.get(root.getRoot().toLowerCase()) : root.getDefault());
        }

        config = newConfig;
        newConfig = null;
    }

    public static String getString(String root, String def) {
        String data = config.getString(root.toLowerCase(), def);

        if (data == null) {
            TownyWaypoints.getInstance().getLogger().warning("Failed to read " + root.toLowerCase() + " from config.yml");
            return "";
        }

        return data;
    }

    public static boolean getBoolean(ConfigNodes node) {
        return Boolean.parseBoolean(config.getString(node.getRoot().toLowerCase(), node.getDefault()));
    }

    public static double getDouble(ConfigNodes node) {
        try {
            return Double.parseDouble(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
        } catch (NumberFormatException e) {
            TownyWaypoints.getInstance().getLogger().warning("Failed to read " + node.getRoot().toLowerCase() + " from config.yml");
            return 0.0;
        }
    }

    public static int getInt(ConfigNodes node) {
        try {
            return Integer.parseInt(config.getString(node.getRoot().toLowerCase(), node.getDefault()).trim());
        } catch (NumberFormatException e) {
            TownyWaypoints.getInstance().getLogger().warning("Failed to read " + node.getRoot().toLowerCase() + " from config.yml");
            return 0;
        }
    }

    public static String getString(ConfigNodes node) {
        return config.getString(node.getRoot().toLowerCase(), node.getDefault());
    }
}
