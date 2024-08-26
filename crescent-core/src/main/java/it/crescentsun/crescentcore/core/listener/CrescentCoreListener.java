package it.crescentsun.crescentcore.core.listener;

import it.crescentsun.crescentmsg.MessageFormatter;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.event.crystals.GenerateCrystalsEvent;
import it.crescentsun.crescentcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.crescentsun.crescentcore.plugindata.PluginDataRegistry;
import it.crescentsun.crescentcore.api.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.crescentcore.api.event.player.PlayerQuitEventPostDBSave;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CrescentCoreListener implements Listener {

    private final CrescentCore crescentCore;

    public CrescentCoreListener(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        crescentCore.getLogger().info("Player joined the server. Port is: " + Bukkit.getServer().getPort()); //Change accordingly
        Player player = event.getPlayer();
        event.joinMessage(null);
        int port = Bukkit.getServer().getPort();
        if (String.valueOf(port).startsWith("9")) {
            Bukkit.getScheduler().runTaskLater(crescentCore, () -> {
                player.sendMessage(MessageFormatter.parse(
                        "<@yellow><b>You are currently on the test server.</b> Please report any bugs you encounter to the staff.</@>"));
            }, 20);
        }
        crescentCore.getPlayerManager().asyncLoadData(player.getUniqueId()).thenAcceptAsync(pData -> {
            System.out.println("Player data: " + pData);
            if (pData == null) {
                return;
            }
            PlayerJoinEventPostDBLoad dataLoadedEvent = new PlayerJoinEventPostDBLoad(player, pData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataLoadedEvent);
                return null;
            });
            boolean isServerLobby = port == CrescentCore.LIVE_LOBBY_PORT ||
                    crescentCore.getServer().getPort() == CrescentCore.TEST_LOBBY_PORT;
            if (isServerLobby) {
                Timestamp firstLogin = pData.getData(CrescentNamespaceKeys.PLAYER_FIRST_LOGIN);
                Timestamp lastSeen = pData.getData(CrescentNamespaceKeys.PLAYER_LAST_SEEN);
                if (firstLogin.equals(lastSeen)) {
                    Bukkit.broadcast(MessageFormatter.parse(
                            "<@green><b>Welcome to the server,</@> <@aqua>" + player.getName() + "</@>!</b>"));
                } else {
                    Bukkit.broadcast(MessageFormatter.parse(
                            "<@yellow>Welcome back,</@> <@white>" + player.getName() + "</@>!"));
                }
            }
        });
        if (port == CrescentCore.LIVE_LOBBY_PORT || port == CrescentCore.TEST_LOBBY_PORT) {
            player.teleport(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation());
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS, 50, 0, false, false));
    }

    /**
     * Saves the player data to the database upon quitting the server.
     * Additionally, it will trigger the {@link PlayerQuitEventPostDBSave} event.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (crescentCore.getPlayerManager().getData(player.getUniqueId()) == null) {
            return;
        }
        crescentCore.getPlayerManager().getData(player.getUniqueId())
                .updateData(CrescentNamespaceKeys.PLAYER_LAST_SEEN, new Timestamp(System.currentTimeMillis()));
        crescentCore.getPlayerManager().asyncSaveData(player.getUniqueId()).thenAcceptAsync(playerData -> {
            if (playerData == null) {
                return;
            }
            PlayerQuitEventPostDBSave dataSavedEvent = new PlayerQuitEventPostDBSave(player, playerData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataSavedEvent);
                return null;
            });
            crescentCore.getPlayerManager().removeData(player.getUniqueId());
        });
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        PluginDataRegistry.freezeRegistries();
        crescentCore.getDatabaseManager().initServerDataManager();
        crescentCore.getDatabaseManager().initTables();
        crescentCore.getDatabaseManager().initPlayerDataManager();
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            CompletableFuture<Boolean> future = crescentCore.getPlayerManager().loadAllData();
            future.thenAccept(success -> {
                if (success) {
                    ServerLoadPostDBSetupEvent dbSetupEvent = new ServerLoadPostDBSetupEvent();
                    Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                        Bukkit.getPluginManager().callEvent(dbSetupEvent);
                        return null;
                    });
                }
            });
        }
        crescentCore.getDatabaseManager().getServerDataManager()
                .setLastLaunch(new Timestamp(System.currentTimeMillis()));
    }

    @EventHandler
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

    @EventHandler
    public void onCrystalSpawn(GenerateCrystalsEvent event) {
        int amount = event.getAmount();
        System.out.println("Amount: " + amount);
        System.out.println("Generation reason: " + event.getGenerationSource());
        System.out.println("Player: " + event.getPlayer());
        crescentCore.getDatabaseManager().getServerDataManager().incrementCrystalsGenerated(amount);
    }

}
