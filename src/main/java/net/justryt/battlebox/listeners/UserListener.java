package net.justryt.battlebox.listeners;

import net.justryt.battlebox.user.UserManager;
import net.justryt.battlebox.utils.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UserListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        try {
            UserManager.getUser(event.getUniqueId()).join();
        } catch (Exception e) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Messages.getComponent("user-load-failure-kick"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UserManager.cleanupUser(event.getPlayer().getUniqueId());
    }
}
