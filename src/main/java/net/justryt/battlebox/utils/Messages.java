package net.justryt.battlebox.utils;

import net.justryt.battlebox.BattleBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public class Messages {
    private static final Map<String, String> messages = new HashMap<>();

    public static boolean initialize(BattleBox plugin) {
        var properties = new Properties();

        try (InputStream inputStream = plugin.getResource("en.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Can't find en.properties, broken JAR?");
            }

            properties.load(inputStream);
            properties.forEach((id, message) -> messages.put((String) id, (String) message));

            return true;
        } catch (IOException | RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load default messages.", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
    }

    // It should be guaranteed that en.properties has all the messages that the plugin requires.
    public static String getMessage(@NotNull String key) {
        return messages.get(key);
    }

    public static Component getComponent(@NotNull String key) {
        return MiniMessage
                .miniMessage()
                .deserialize(getMessage(key));
    }
}
