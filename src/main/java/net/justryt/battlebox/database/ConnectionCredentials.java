package net.justryt.battlebox.database;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ConnectionCredentials(
        @NotNull String username,
        @NotNull String password,
        @NotNull String host,
        @NotNull Integer port,
        @NotNull String database,
        @NotNull String parameters
) {
    public ConnectionCredentials {
        Objects.requireNonNull(username, "Username cannot be null, check config.yml for field 'db.username'");
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank, check config.yml for field 'db.username'");
        }

        Objects.requireNonNull(password, "Password cannot be null, check config.yml for field 'db.password'");

        Objects.requireNonNull(host, "Host cannot be null, check config.yml for field 'db.host'");
        if (host.isBlank()) {
            throw new IllegalArgumentException("Host cannot be blank, check config.yml for field 'db.host'");
        }

        Objects.requireNonNull(port, "Port cannot be null, check config.yml for field 'db.port'");
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535, check config.yml for field 'db.port', Provided: " + port);
        }

        Objects.requireNonNull(database, "Database cannot be null, check config.yml for field 'db.database'");
        if (database.isBlank()) {
            throw new IllegalArgumentException("Database name cannot be blank, check config.yml for field 'db.database'");
        }

        if (parameters == null) {
            parameters = "";
        }
    }
}