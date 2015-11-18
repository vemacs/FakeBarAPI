package me.confuser.barapi.actionbar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActionBarAPI {
    private static String version = "";

    private static Method getHandle;
    private static Method sendPacket;
    private static Field playerConnection;
    private static Class<?> nmsChatSerializer;
    private static Constructor chatConstructor;

    static {
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> packetType = Class.forName(getPacketPlayOutChat());
            Class<?> typeCraftPlayer = Class.forName(getCraftPlayerClasspath());
            Class<?> typeNMSPlayer = Class.forName(getNMSPlayerClasspath());
            Class<?> typePlayerConnection = Class.forName(getPlayerConnectionClasspath());
            nmsChatSerializer = Class.forName(getChatSerializerClasspath());
            Class<?> nmsIChatBaseComponent = Class.forName(getIChatBaseComponentClasspath());
            getHandle = typeCraftPlayer.getMethod("getHandle");
            playerConnection = typeNMSPlayer.getField("playerConnection");
            sendPacket = typePlayerConnection.getMethod("sendPacket", Class.forName(getPacketClasspath()));
            if (version.startsWith("v1_7")) {
                chatConstructor = packetType.getConstructor(nmsIChatBaseComponent, int.class);
            } else {
                chatConstructor = packetType.getConstructor(nmsIChatBaseComponent, byte.class);
            }
        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | NoSuchFieldException ex) {
            Bukkit.getLogger().severe(ex.getMessage());
        }
    }

    public static void send(Player receivingPacket, String msg) {
        try {
            Object serialized = nmsChatSerializer.getMethod("a", String.class).invoke(null, "{\"text\": \"" + msg + "\"}");
            Object packet;
            if (version.startsWith("v1_7")) {
                if (!hasNewProtocol(receivingPacket)) {
                    return;
                }
                packet = chatConstructor.newInstance(serialized, 2);
            } else {
                packet = chatConstructor.newInstance(serialized, (byte) 2);
            }
            Object player = getHandle.invoke(receivingPacket);
            Object connection = playerConnection.get(player);
            sendPacket.invoke(connection, packet);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
            Bukkit.getLogger().severe(ex.getMessage());
        }
    }

    private static String getCraftPlayerClasspath() {
        return "org.bukkit.craftbukkit." + version + ".entity.CraftPlayer";
    }

    private static String getPlayerConnectionClasspath() {
        return "net.minecraft.server." + version + ".PlayerConnection";
    }

    private static String getNMSPlayerClasspath() {
        return "net.minecraft.server." + version + ".EntityPlayer";
    }

    private static String getPacketClasspath() {
        return "net.minecraft.server." + version + ".Packet";
    }

    private static String getIChatBaseComponentClasspath() {
        return "net.minecraft.server." + version + ".IChatBaseComponent";
    }

    private static String getChatSerializerClasspath() {
        int minorVersion = Integer.parseInt(version.split("R")[1]);
        if (version.startsWith("v1_8_R") && minorVersion >= 2) {
            return "net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer";
        }
        return "net.minecraft.server." + version + ".ChatSerializer";
    }

    private static String getPacketPlayOutChat() {
        return "net.minecraft.server." + version + ".PacketPlayOutChat";
    }

    private static boolean hasNewProtocol(Player player) {
        try {
            return GetVersionUtil.hasNewProtocol(player);
        } catch (Throwable t) {
            return true;
        }
    }
}