package net.justryt.battlebox.database.implementations;

import com.zaxxer.hikari.HikariDataSource;
import net.justryt.battlebox.BattleBox;
import net.justryt.battlebox.database.Database;
import net.justryt.battlebox.database.ConnectionCredentials;
import net.justryt.battlebox.user.User;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MySqlDatabase extends Database {
    private final ConnectionCredentials settings;
    private final Type type;
    private final String driverClass;

    private HikariDataSource dataSource;

    public MySqlDatabase(@NotNull BattleBox plugin, @NotNull ConnectionCredentials credentials, @NotNull Type type) {
        super(plugin);

        this.settings = credentials;
        this.type = type;
        this.driverClass = type == Type.MARIADB ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public void initialize() throws IllegalStateException {
        dataSource = new HikariDataSource();

        dataSource.setDriverClassName(driverClass);
        dataSource.setJdbcUrl(String.format("jdbc:%s://%s:%s/%s%s",
                type.getProtocol(),
                settings.host(),
                settings.port(),
                settings.database(),
                settings.parameters()
        ));

        dataSource.setUsername(settings.username());
        dataSource.setPassword(settings.password());

        final Properties properties = new Properties();

        properties.putAll(Map.of(
                "cachePrepStmts", "true",
                "prepStmtCacheSize", "250",
                "prepStmtCacheSqlLimit", "2048",
                "useServerPrepStmts", "true",
                "useLocalSessionState", "true",
                "useLocalTransactionState", "true",
                "rewriteBatchedStatements", "true",
                "cacheResultSetMetadata", "true",
                "cacheServerConfiguration", "true"
        ));

        properties.putAll(Map.of(
                "elideSetAutoCommits", "true",
                "maintainTimeStats", "false"
        ));

        dataSource.setDataSourceProperties(properties);

        final var resource = plugin.getResource("db/schema_mysql.sql");
        if (resource == null) {
            throw new IllegalStateException("MySQL schema not found, broken JAR?");
        }

        try (var reader = new BufferedReader(new InputStreamReader(resource));
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            final var sqlScript = reader.lines().collect(Collectors.joining("\n"));
            final var sqlStmtsStr = sqlScript.split(";");

            for (String strStmt : sqlStmtsStr) {
                String trimmedStmt = strStmt.trim();

                if (!trimmedStmt.isEmpty()) {
                    statement.execute(trimmedStmt);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create database tables. Please ensure you are running MySQL v8.0+ and that your connecting user account has privileges to create tables.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the internal schema SQL file.", e);
        }
    }

    @Override
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public CompletableFuture<Optional<User>> getUser(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM users WHERE uuid = ?;"
            )) {
                statement.setString(1, uuid.toString());

                try (var rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }

                    var user = buildUser(rs);

                    return Optional.of(user);
                }
            } catch (SQLException e) {
                throw new RuntimeException("SQL error occurred while fetching user data.", e);
            }
        });
    }

    @Override
    public CompletableFuture<User> ensureUser(@NotNull UUID uuid) {
        return getUser(uuid)
                .thenCompose(optionalUser -> {
                    if (optionalUser.isPresent()) {
                        return CompletableFuture.completedFuture(optionalUser.get());
                    } else {
                        var newUser = new User(uuid);
                        return saveUser(newUser).thenApply(_ -> newUser);
                    }
                });
    }

    private User buildUser(@NotNull ResultSet rs) throws SQLException {
        var user = new User(UUID.fromString(rs.getString("uuid")));

        // Add other properties using the result set to the user object.

        return user;
    }

    @Override
    public CompletableFuture<Void> saveUser(@NotNull User user) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement("" +
                    "INSERT INTO users(uuid) " +
                    "VALUES (?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "uuid = uuid;"
            )) {
                statement.setString(1, user.getPlayerId().toString());

                statement.execute();
            } catch (SQLException e) {
                throw new RuntimeException("SQL error occurred while saving user data for UUID: " + user.getPlayerId(), e);
            }
        });
    }

    @NotNull
    private Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("The database has not been initialized");
        }

        return dataSource.getConnection();
    }
}
