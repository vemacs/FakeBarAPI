package me.confuser.barapi;

import me.confuser.barapi.actionbar.ActionBarMessageHandler;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TODO: Hook in expbar for percentage
// Original https://github.com/confuser/BarAPI/blob/master/src/me/confuser/barapi/BarAPI.java
@SuppressWarnings("unused")
public class BarAPI extends JavaPlugin {
    private static ActionBarMessageHandler handler;
    private static Map<UUID, BukkitTask> timers = new HashMap<>();
    private static BarAPI instance;

    @Override
    public void onEnable() {
        instance = this;
        handler = new ActionBarMessageHandler(this);
    }

    @Override
    public void onDisable() {
        instance = null;
        handler.stop();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelTimer(event.getPlayer());
    }

    public static void setMessage(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            setMessage(p, message);
        }
    }

    public static void setMessage(Player player, String message) {
        cancelTimer(player);
        handler.setMessage(player, message);
    }

    public static void setMessage(String message, float percent) { // Percent argument dropped
        for (Player player : Bukkit.getOnlinePlayers()) {
            setMessage(player, message, percent);
        }
    }

    public static void setMessage(Player player, String message, float percent) { // Percent argument dropped
        setMessage(player, message);
    }

    public static void setMessage(String message, int seconds) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setMessage(player, message, seconds);
        }
    }

    public static void setMessage(final Player player, String message, int seconds) {
        Validate.isTrue(seconds > 0, "Seconds must be above 1 but was: ", seconds);
        final UUID uuid = player.getUniqueId();
        cancelTimer(player);

        timers.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(instance, new Runnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) handler.removeMessage(p);
            }
        }, seconds * 20L));

        setMessage(player, message);
    }

    public static boolean hasBar(Player player) {
        return handler.hasMessage(player);
    }

    public static void removeBar(Player player) {
        if (!hasBar(player))
            return;

        handler.removeMessage(player);
        cancelTimer(player);
    }

    public static void setHealth(Player player, float percent) {
        // do nothing
    }

    public static float getHealth(Player player) {
        return 100F;
    }

    public static String getMessage(Player player) {
        if (!hasBar(player))
            return "";

        return handler.getMessage(player);
    }

    private static String cleanMessage(String message) {
        if (message.length() > 64)
            message = message.substring(0, 63);

        return message;
    }

    public static void cancelTimer(Player player) {
        BukkitTask task = timers.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
}
