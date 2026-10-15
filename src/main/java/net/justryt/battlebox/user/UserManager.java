package net.justryt.battlebox.user;

import lombok.Setter;
import net.justryt.battlebox.BattleBox;
import net.justryt.battlebox.database.Database;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserManager {
    @Setter private static Database database;
    @Setter private static Logger logger;

    private static final Map<UUID, User> users = new ConcurrentHashMap<>();

    public static void startCleanupTask(@NotNull BattleBox plugin) {
        Bukkit.getServer()
                .getAsyncScheduler()
                .runAtFixedRate(plugin, _ -> users.keySet().removeIf(uuid -> {
                    var player = Bukkit.getPlayer(uuid);
                    return player == null || !player.isOnline();
                }), 5, 5, TimeUnit.MINUTES);
    }

    public static CompletableFuture<User> getUser(@NotNull UUID uuid) {
        if (users.containsKey(uuid)) return CompletableFuture.completedFuture(users.get(uuid));
        return database.ensureUser(uuid)
                .thenApply(user -> {
                    users.put(uuid, user);
                    return user;
                })
                .exceptionally(throwable -> {
                    logger.log(Level.SEVERE, "Failed to load user for UUID: " + uuid, throwable);
                    throw new RuntimeException(throwable);
                });
    }

    public static CompletableFuture<Void> modifyOfflineUser(@NotNull UUID uuid, @NotNull Consumer<User> modifier) {
        if (users.containsKey(uuid)) {
            modifier.accept(users.get(uuid));
            return CompletableFuture.completedFuture(null);
        }

        return database.ensureUser(uuid)
                .thenCompose(user -> {
                    if (users.containsKey(uuid)) {
                        modifier.accept(users.get(uuid));
                        return CompletableFuture.completedFuture(null);
                    }

                    modifier.accept(user);
                    return database.saveUser(user);
                })
                .exceptionally(throwable -> {
                    logger.log(Level.SEVERE, "Failed to modify offline user data for UUID: " + uuid, throwable);
                    return null;
                });
    }

    public static CompletableFuture<Void> cleanupUser(@NotNull UUID uuid) {
        var removedUser = users.remove(uuid);
        if (removedUser == null) return CompletableFuture.completedFuture(null);
        return database.saveUser(removedUser)
                .exceptionally(throwable -> {
                    logger.log(Level.SEVERE, "Failed to save user data for UUID: " + uuid + ", data might be lost.", throwable);
                    return null;
                });
    }
}
