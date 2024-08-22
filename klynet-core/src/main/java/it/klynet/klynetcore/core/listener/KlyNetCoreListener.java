package it.klynet.klynetcore.core.listener;

import it.klynet.adventurousklynetmsg.MessageFormatter;
import it.klynet.klynetcore.KlyNetCore;
import it.klynet.klynetcore.api.event.crystals.GenerateCrystalsEvent;
import it.klynet.klynetcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.klynet.klynetcore.plugindata.PluginDataRegistry;
import it.klynet.klynetcore.api.event.player.PlayerJoinEventPostDBLoad;
import it.klynet.klynetcore.api.event.player.PlayerQuitEventPostDBSave;
import it.klynet.klynetcore.api.registry.KlyNetNamespaceKeys;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class KlyNetCoreListener implements Listener {

    private final KlyNetCore klyNetCore;

    public KlyNetCoreListener(KlyNetCore klyNetCore) {
        this.klyNetCore = klyNetCore;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.joinMessage(null);
        int port = Bukkit.getServer().getPort();
        if (String.valueOf(port).startsWith("9")) {
            Bukkit.getScheduler().runTaskLater(klyNetCore, () -> {
                player.sendMessage(MessageFormatter.parse(
                        "<@yellow><b>You are currently on the test server.</b> Please report any bugs you encounter to the staff.</@>"));
            }, 20);
        }
        klyNetCore.getPlayerManager().asyncLoadData(player.getUniqueId()).thenAcceptAsync(pData -> {
            System.out.println("Player data: " + pData);
            if (pData == null) {
                return;
            }
            PlayerJoinEventPostDBLoad dataLoadedEvent = new PlayerJoinEventPostDBLoad(player, pData);
            Bukkit.getScheduler().callSyncMethod(klyNetCore, () -> {
                Bukkit.getPluginManager().callEvent(dataLoadedEvent);
                return null;
            });
            boolean isServerLobby = port == KlyNetCore.LIVE_LOBBY_PORT ||
                    klyNetCore.getServer().getPort() == KlyNetCore.TEST_LOBBY_PORT;
            if (isServerLobby) {
                Timestamp firstLogin = pData.getData(KlyNetNamespaceKeys.PLAYER_FIRST_LOGIN);
                Timestamp lastSeen = pData.getData(KlyNetNamespaceKeys.PLAYER_LAST_SEEN);
                if (firstLogin.equals(lastSeen)) {
                    Bukkit.broadcast(MessageFormatter.parse(
                            "<@green><b>Welcome to the server,</@> <@aqua>" + player.getName() + "</@>!</b>"));
                } else {
                    Bukkit.broadcast(MessageFormatter.parse(
                            "<@yellow>Welcome back,</@> <@white>" + player.getName() + "</@>!"));
                }
            }
        });
        if (port == KlyNetCore.LIVE_LOBBY_PORT || port == KlyNetCore.TEST_LOBBY_PORT) {
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
        if (klyNetCore.getPlayerManager().getData(player.getUniqueId()) == null) {
            return;
        }
        klyNetCore.getPlayerManager().getData(player.getUniqueId())
                .updateData(KlyNetNamespaceKeys.PLAYER_LAST_SEEN, new Timestamp(System.currentTimeMillis()));
        klyNetCore.getPlayerManager().asyncSaveData(player.getUniqueId()).thenAcceptAsync(playerData -> {
            if (playerData == null) {
                return;
            }
            PlayerQuitEventPostDBSave dataSavedEvent = new PlayerQuitEventPostDBSave(player, playerData);
            Bukkit.getScheduler().callSyncMethod(klyNetCore, () -> {
                Bukkit.getPluginManager().callEvent(dataSavedEvent);
                return null;
            });
            klyNetCore.getPlayerManager().removeData(player.getUniqueId());
        });
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        PluginDataRegistry.freezeRegistries();
        klyNetCore.getDatabaseManager().initServerDataManager();
        klyNetCore.getDatabaseManager().initTables();
        klyNetCore.getDatabaseManager().initPlayerDataManager();
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            CompletableFuture<Boolean> future = klyNetCore.getPlayerManager().loadAllData();
            future.thenAccept(success -> {
                if (success) {
                    ServerLoadPostDBSetupEvent dbSetupEvent = new ServerLoadPostDBSetupEvent();
                    Bukkit.getScheduler().callSyncMethod(klyNetCore, () -> {
                        Bukkit.getPluginManager().callEvent(dbSetupEvent);
                        return null;
                    });
                }
            });
        }
        klyNetCore.getDatabaseManager().getServerDataManager()
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
        klyNetCore.getDatabaseManager().getServerDataManager().incrementCrystalsGenerated(amount);
    }

}
