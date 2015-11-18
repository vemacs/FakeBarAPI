package me.confuser.barapi.actionbar;

import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class GetVersionUtil {
    public static boolean hasNewProtocol(Player player) {
        return ((CraftPlayer) player).getHandle().
                playerConnection.networkManager.getVersion() > 5;
    }
}
