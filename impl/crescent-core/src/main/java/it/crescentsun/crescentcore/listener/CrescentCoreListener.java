package it.crescentsun.crescentcore.listener;

import it.crescentsun.api.common.DatabaseNamespacedKeys;
import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.data.player.PlayerData;
import it.crescentsun.api.crescentcore.event.player.PlayerDataSavedPostQuitEvent;
import it.crescentsun.api.crescentcore.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.api.crescentcore.event.server.DataLoadedEvent;
import it.crescentsun.api.crescentcore.event.server.ProxyConnectedEvent;
import it.crescentsun.crescentcore.CrescentCore;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.crescentsun.crescentcore.event.*;
import it.crescentsun.crescentcore.lang.CrescentCoreLocalization;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.bukkit.Bukkit.getServer;

public class CrescentCoreListener implements Listener {

    private final CrescentCore crescentCore;

    public CrescentCoreListener(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        int port = getServer().getPort();
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
        crescentCore.getPlayerDataManager().loadDataAsync(player.getUniqueId()).thenAcceptAsync(playerData -> {
            if (playerData == null) {
                return;
            }
            PlayerJoinEventPostDBLoad dataLoadedEvent = new PlayerJoinEventPostDBLoad(player, playerData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataLoadedEvent);
                return null;
            });
            // Set player's username
            playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_USERNAME, player.getName());
            // Check if the server is a lobby server
            Optional<Timestamp> firstLogin = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_FIRST_LOGIN);
            Optional<Timestamp> lastSeen = playerData.getDataValue(DatabaseNamespacedKeys.PLAYER_LAST_SEEN);
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
                PlayerData playerData = crescentCore.getPlayerDataManager().getData(player);
                // Update the player's play time on login
                playerData.updateDataValue(DatabaseNamespacedKeys.PLAYER_LAST_LOGIN, new Timestamp(System.currentTimeMillis()));
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
     * Additionally, it will trigger the {@link PlayerDataSavedPostQuitEvent} event.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (crescentCore.getPlayerDataManager().getData(player.getUniqueId()) == null) {
            return;
        }

        crescentCore.getPlayerDataManager().updatePlayerSessionData(player);
        crescentCore.getPlayerDataManager().saveDataAsync(player.getUniqueId()).thenAcceptAsync(pData -> {
            if (pData == null) {
                return;
            }
            PlayerDataSavedPostQuitEvent dataSavedEvent = new PlayerDataSavedPostQuitEvent(player, pData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataSavedEvent);
                return null;
            });
            crescentCore.getPlayerDataManager().removeData(player.getUniqueId());
        });
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerLoad(ServerLoadEvent event) {
        getServer().getPluginManager().callEvent(new PlayerDataEntryRegistrationEvent(crescentCore.getPlayerDataRegistry()));
        getServer().getPluginManager().callEvent(new PluginDataRegistrationEvent(crescentCore.getPluginDataRegistry()));

        crescentCore.getPlayerDataRegistry().freezeRegistry();
        getServer().getPluginManager().callEvent(new PlayerDataRegistryFreezeEvent());

        crescentCore.getPluginDataRegistry().freezeRegistry();
        getServer().getPluginManager().callEvent(new PluginDataRegistryFreezeEvent());

        crescentCore.getDatabaseManager().initTables();

        crescentCore.getPlayerDataManager().init();
        crescentCore.getPluginDataManager().init();
        for (Player player : Bukkit.getOnlinePlayers()) {
            crescentCore.getPlayerDataManager().loadData(player.getUniqueId());
        }
        crescentCore.getPluginDataManager().loadAllData();
        DataLoadedEvent dataLoadedEvent = new DataLoadedEvent(event.getType()); // plugin data service is needed here.
        Bukkit.getPluginManager().callEvent(dataLoadedEvent);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDataEntryRegister(PlayerDataEntryRegistrationEvent event) {
        triggerEventForPlugins(
                CrescentPlugin::onPlayerDataRegister,
                crescentCore.getPlayerDataRegistry()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginDataEntryRegister(PluginDataRegistrationEvent event) {
        triggerEventForPlugins(
                CrescentPlugin::onPluginDataRegister,
                crescentCore.getPluginDataRegistry()
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDataRegistryFreeze(PlayerDataRegistryFreezeEvent event) {
        triggerEventForPlugins(CrescentPlugin::onPlayerDataRegistryFreeze);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginDataRegistryFreeze(PluginDataRegistryFreezeEvent event) {
        triggerEventForPlugins(CrescentPlugin::onPluginDataRegistryFreeze);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerLoadPostDB(DataLoadedEvent event) {
        triggerEventForPlugins(CrescentPlugin::onDataLoad);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProtoWeaverConnectionEstablished(ProxyConnectedEvent event) {
        triggerEventForPlugins(CrescentPlugin::onProxyConnected);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAutoSave(AutoSaveEvent event) {
        triggerEventForPlugins(CrescentPlugin::onAutoSave);
    }

    private <T> void triggerEventForPlugins(BiConsumer<CrescentPlugin, T> eventTrigger, T parameter) {
        // Call the event trigger for crescentCore first
        try {
            eventTrigger.accept(crescentCore, parameter);
        } catch (Exception e) {
            crescentCore.getLogger().severe("Error in CrescentCore during event: " + e.getMessage());
            e.printStackTrace();
        }

        // Loop over all loaded plugins
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof CrescentPlugin crescentPlugin && !(plugin instanceof CrescentCore)) {
                try {
                    eventTrigger.accept(crescentPlugin, parameter);
                } catch (Exception e) {
                    crescentCore.getLogger().severe("Error in plugin " + plugin.getName() + " during event: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void triggerEventForPlugins(Consumer<CrescentPlugin> eventTrigger) {
        // Call the event trigger for crescentCore first
        eventTrigger.accept(crescentCore);

        // Loop over all loaded plugins
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof CrescentPlugin crescentPlugin && !(plugin instanceof CrescentCore)) {
                eventTrigger.accept(crescentPlugin);
            }
        }
    }

}
