package it.klynet.klynetcore.api;

import it.klynet.adventurousklynetmsg.MessageFormatter;
import it.klynet.adventurousklynetmsg.MessageType;
import it.klynet.klynetcore.KlyNetCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandSenderUtil {

    /**
     * Sends a formatted message to a CommandSender.
     * @param sender The CommandSender to send the message to.
     * @param messageType The MessageType of the message.
     * @param message The message to send.
     * @param keywords The keywords to replace in the message.
     */
    public static void sendFormattedMessage(CommandSender sender, MessageType messageType,
                                            String message, String... keywords) {
        TextComponent text = MessageFormatter.formatCommandMessage(messageType, Component.text(message), keywords);
        sender.sendMessage(text);
    }

    /**
     * Sends a debug message to the console, if the debug level is high enough.
     * @param obj The object to send to the console.
     * @param debugLevel The debug level required to send the message.
     */
    public static void debugMessage(Object obj, int debugLevel) {
        KlyNetCore plugin = KlyNetCore.getInstance();
        if (plugin.getDebugLevel() >= debugLevel) {
            Bukkit.getLogger().info(obj.toString());
        }
    }

}
