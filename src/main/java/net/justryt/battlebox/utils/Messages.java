package net.justryt.battlebox.utils;

import lombok.Getter;
import net.justryt.battlebox.BattleBox;
import net.justryt.battlebox.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class Messages {
    private static final Map<String, Properties> messages = new HashMap<>();
    @Getter private static String defaultLanguage;

    public static boolean initialize(BattleBox plugin){
        defaultLanguage = plugin.getConfig().getString("default-lang", "english").toLowerCase();
        var localeDir = plugin.getDataPath().resolve("messages");

        try {
            Files.createDirectories(localeDir);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create messages directory!", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }

        if (!Files.exists(localeDir.resolve("english.properties"))) {
            try {
                plugin.saveResource("messages/english.properties", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "Default english.properties missing from JAR!", e);
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                return false;
            }
        }

        try (var stream = Files.newDirectoryStream(localeDir, "*.properties")) {
            stream.forEach(path -> {
                if (!Files.isRegularFile(path)) return;

                var fileName = path.getFileName().toString();
                var langKey = fileName.substring(0, fileName.lastIndexOf(".")).toLowerCase();

                var properties = new Properties();

                try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                    messages.put(langKey, properties);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load language file: %s".formatted(fileName), e);
                }
            });
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Error occurred when loading languages.", e);
            return true;
        }

        if (!messages.containsKey(defaultLanguage)) {
            plugin.getLogger().log(Level.SEVERE, "Default language '%s' not found in messages directory.".formatted(defaultLanguage));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }

        return true;
    }

    public static String getMessage(@NotNull String language, @NotNull String messageKey) {
        language = language.toLowerCase();

        var pack = messages.get(language);
        var message = (pack != null) ? pack.getProperty(messageKey) : null;

        if (message == null) {
            BattleBox.getInstance().getLogger().log(Level.WARNING, "Message '%s' for language '%s' missing.".formatted(messageKey, language));

            var defaultPack = messages.get(defaultLanguage);

            if (defaultPack != null) {
                message = defaultPack.getProperty(language);
            }

            if (message == null) {
                return "<red>Missing Key: %s".formatted(language);
            }
        }

        return message;
    }

    public static Component getComponent(@NotNull String language, @NotNull String messageKey) {
        return MiniMessage
                .miniMessage()
                .deserialize(getMessage(language, messageKey));
    }

    public static Component getComponent(@NotNull String language, @NotNull String messageKey, TagResolver... resolvers) {
        return MiniMessage
                .miniMessage()
                .deserialize(getMessage(language, messageKey), resolvers);
    }

    public static void sendMessage(@NotNull Player player, @NotNull String messageKey) {
        UserManager
                .getUser(player.getUniqueId())
                .thenAccept(user -> player.sendMessage(getComponent(user.getLanguage(), messageKey)));
    }

    public static void sendMessage(@NotNull Player player, @NotNull String messageKey, TagResolver... resolvers) {
        UserManager
                .getUser(player.getUniqueId())
                .thenAccept(user -> player.sendMessage(getComponent(user.getLanguage(), messageKey, resolvers)));
    }

    public static Set<String> getAvailableLanguages() {
        return messages.keySet();
    }
}
