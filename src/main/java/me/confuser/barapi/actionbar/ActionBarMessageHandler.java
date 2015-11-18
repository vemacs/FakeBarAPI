package me.confuser.barapi.actionbar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActionBarMessageHandler {
    private Map<UUID, String> persistentMessages = new ConcurrentHashMap<>();
    private BukkitTask task;

    public ActionBarMessageHandler(Plugin plugin) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                Set<UUID> toRemove = new HashSet<>();
                for (Map.Entry<UUID, String> entry : persistentMessages.entrySet()) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    if (p != null) {
                        ActionBarAPI.send(p, entry.getValue());
                    } else {
                        toRemove.add(entry.getKey());
                    }
                }
                for (UUID uuid : toRemove) {
                    persistentMessages.remove(uuid);
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public boolean hasMessage(Player p) {
        return persistentMessages.containsKey(p.getUniqueId());
    }

    public String getMessage(Player p) {
        return persistentMessages.get(p.getUniqueId());
    }

    public void setMessage(Player p, String message) {
        persistentMessages.put(p.getUniqueId(), message);
    }

    public void removeMessage(Player p) {
        persistentMessages.remove(p.getUniqueId());
        ActionBarAPI.send(p, "");
    }

    public void stop() {
        task.cancel();
    }
}
