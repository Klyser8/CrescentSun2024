package it.crescentsun.crescentcore.core.listener;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRegistry;
import it.crescentsun.crescentcore.api.event.server.ProtoweaverConnectionEstablishedEvent;
import it.crescentsun.crescentcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.crescentsun.crescentcore.api.data.player.PlayerDataRegistry;
import it.crescentsun.crescentcore.api.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.crescentcore.api.event.player.PlayerQuitEventPostDBSave;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.crescentsun.crescentcore.core.lang.CrescentCoreLocalization;
import it.crescentsun.crescentmsg.api.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.Timestamp;
import java.util.Objects;

public class CrescentCoreListener implements Listener {

    private final CrescentCore crescentCore;

    public CrescentCoreListener(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        int port = Bukkit.getServer().getPort();
        if (String.valueOf(port).equals("25564")) {
            Bukkit.getScheduler().runTaskLater(crescentCore, () -> {
                player.sendMessage(MessageFormatter.parse(
                        "<@yellow><b>You are currently on the test server.</b> Please report any bugs you encounter to the staff.</@>"));
            }, 20);
        }
        loadPlayerDataAsync(player);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS, 50, 0, false, false));
    }

    private void loadPlayerDataAsync(Player player) {
        crescentCore.getPlayerDBManager().asyncLoadData(player.getUniqueId()).thenAcceptAsync(pData -> {
            if (pData == null) {
                return;
            }
            PlayerJoinEventPostDBLoad dataLoadedEvent = new PlayerJoinEventPostDBLoad(player, pData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataLoadedEvent);
                return null;
            });
            // Check if the server is a lobby server
            Timestamp firstLogin = pData.getDataValue(CrescentNamespaceKeys.PLAYER_FIRST_LOGIN);
            Timestamp lastSeen = pData.getDataValue(CrescentNamespaceKeys.PLAYER_LAST_SEEN);
            if (firstLogin.equals(lastSeen)) {
                Bukkit.broadcast(MessageFormatter.parse(
                        "<@green><b>Welcome to the Crescent Sun Network,</@> <@aqua>" + player.getName() + "</@>!</b>"));
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRegisterChannelEvent(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals("BungeeCord")) {
            Bukkit.getScheduler().runTaskLater(crescentCore, () -> {
                Player player = event.getPlayer();
                player.sendMessage(CrescentCoreLocalization.SERVER_JOIN_MESSAGE_PLAYER.getFormattedMessage(player.locale(), crescentCore.getServerName()));
                // Notify other players of the player's join
                Bukkit.getOnlinePlayers().forEach(p -> {
                    if (!p.equals(player)) {
                        p.sendMessage(CrescentCoreLocalization.SERVER_JOIN_MESSAGE_OTHER.getFormattedMessage(p.locale(), player.getName()));
                    }
                });
                // Notify the console of the player's join
                Bukkit.getConsoleSender().sendMessage(CrescentCoreLocalization.SERVER_JOIN_MESSAGE_OTHER.getFormattedMessage(null, player.getName()));
            }, 5);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerSpawnPostJoin(PlayerSpawnLocationEvent event) {
        if (crescentCore.getServerName().contains("lobby")) {
            //noinspection DataFlowIssue
            event.setSpawnLocation(Bukkit.getWorld("world").getSpawnLocation());
        }
    }

    /**
     * Saves the player data to the database upon quitting the server.
     * Additionally, it will trigger the {@link PlayerQuitEventPostDBSave} event.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (crescentCore.getPlayerDBManager().getData(player.getUniqueId()) == null) {
            return;
        }
        crescentCore.getPlayerDBManager().getData(player.getUniqueId())
                .updateDataValue(CrescentNamespaceKeys.PLAYER_LAST_SEEN, new Timestamp(System.currentTimeMillis()));
        crescentCore.getPlayerDBManager().asyncSaveData(player.getUniqueId()).thenAcceptAsync(playerData -> {
            if (playerData == null) {
                return;
            }
            PlayerQuitEventPostDBSave dataSavedEvent = new PlayerQuitEventPostDBSave(player, playerData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataSavedEvent);
                return null;
            });
            crescentCore.getPlayerDBManager().removeData(player.getUniqueId());
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerLoad(ServerLoadEvent event) {
        PlayerDataRegistry.freezeRegistries();
        PluginDataRegistry.freezeRegistries();
        crescentCore.getDatabaseManager().initTables();
        crescentCore.getDatabaseManager().initPluginDataManager();
        crescentCore.getDatabaseManager().initPlayerDataManager();
        crescentCore.getPluginDBManager().loadAllData();
        for (Player player : Bukkit.getOnlinePlayers()) {
            crescentCore.getPlayerDBManager().loadData(player.getUniqueId());
        }
        ServerLoadPostDBSetupEvent dbSetupEvent = new ServerLoadPostDBSetupEvent(event.getType());
        Bukkit.getPluginManager().callEvent(dbSetupEvent);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProtoWeaverConnectionEstablished(ProtoweaverConnectionEstablishedEvent event) {
        // After loading all jumpwarps, initialize them.
        for (PluginData pluginData : crescentCore.getPluginDataRepository().getAllData().values()) {
            if (pluginData.shouldInit()) {
                pluginData.init();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {
        if (!(event.message() instanceof TextComponent textComponent)) {
            return;
        }
        if (textComponent.content().startsWith("/")) {
            return;
        }
        Component parsedComponent = MessageFormatter.parse(textComponent.content());
        if (parsedComponent == null) {
            return;
        }
        event.message(parsedComponent);
    }

}
