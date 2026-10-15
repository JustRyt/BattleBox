package net.justryt.battlebox;

import lombok.Getter;
import net.justryt.battlebox.database.ConnectionCredentials;
import net.justryt.battlebox.database.Database;
import net.justryt.battlebox.database.implementations.MySqlDatabase;
import net.justryt.battlebox.listeners.UserListener;
import net.justryt.battlebox.user.UserManager;
import net.justryt.battlebox.utils.Messages;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class BattleBox extends JavaPlugin {
    @Getter
    private Database database;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!Messages.initialize(this)) return;
        if (!setupDatabase()) return;

        UserManager.setDatabase(database);
        UserManager.setLogger(getLogger());
        UserManager.startCleanupTask(this);

        getServer().getPluginManager().registerEvents(new UserListener(), this);
    }

    @Override
    public void onDisable() {
        if (database != null) database.shutdown();
    }

    private boolean setupDatabase() {
        try {
            var configDbType = Database.Type.valueOf(getConfig().getString("db.type"));

            getLogger().info("Trying to connect to %s.".formatted(configDbType.getDisplayName()));

            var credentials = new ConnectionCredentials(
                    getConfig().getString("db.username"),
                    getConfig().getString("db.password"),
                    getConfig().getString("db.host"),
                    getConfig().getInt("db.port"),
                    getConfig().getString("db.database"),
                    getConfig().getString("db.parameters")
            );

            switch (configDbType) {
                case MYSQL -> database = new MySqlDatabase(this, credentials, Database.Type.MYSQL);
                case MARIADB -> database = new MySqlDatabase(this, credentials, Database.Type.MARIADB);
            }

            database.initialize();

            return true;
        } catch (IllegalArgumentException e) {
            getLogger().severe("Incorrect DB type in config.yml field 'db.type', supported: mysql, mariadb, postgres, mongo.");

            getServer().getPluginManager().disablePlugin(this);
            return false;
        } catch (IllegalStateException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database.", e);

            getServer().getPluginManager().disablePlugin(this);
            return false;
        }
    }
}
