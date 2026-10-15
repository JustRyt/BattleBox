package net.justryt.battlebox.database;

import lombok.Getter;
import net.justryt.battlebox.BattleBox;
import net.justryt.battlebox.user.User;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Database {
    protected BattleBox plugin;

    protected Database(@NotNull BattleBox plugin) {
        this.plugin = plugin;
    }

    @Blocking
    public abstract void initialize() throws IllegalStateException;

    public abstract void shutdown();

    public abstract CompletableFuture<Optional<User>> getUser(@NotNull UUID uuid);

    public abstract CompletableFuture<User> ensureUser(@NotNull UUID uuid);

    public abstract CompletableFuture<Void> saveUser(@NotNull User user);

    @Getter
    public enum Type {
        MYSQL("MySQL", "mysql"),
        MARIADB("MariaDB", "mariadb"),
        POSTGRES("PostgreSQL", "postgresql"),
        MONGO("MongoDB", "mongo");

        private final String displayName;
        private final String protocol;

        Type(@NotNull String displayName, @NotNull String protocol) {
            this.displayName = displayName;
            this.protocol = protocol;
        }
    }
}
