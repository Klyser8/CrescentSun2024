package it.crescentsun.crescentcore.core.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import it.crescentsun.crescentcore.api.BungeeUtils;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRegistry;
import it.crescentsun.crescentcore.api.event.server.ServerLoadPostDBSetupEvent;
import it.crescentsun.crescentcore.api.data.player.PlayerDataRegistry;
import it.crescentsun.crescentcore.api.event.player.PlayerJoinEventPostDBLoad;
import it.crescentsun.crescentcore.api.event.player.PlayerQuitEventPostDBSave;
import it.crescentsun.crescentcore.api.registry.CrescentNamespaceKeys;
import io.papermc.paper.event.player.AsyncChatEvent;
import it.crescentsun.crescentcore.core.data.JumpWarp;
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

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static it.crescentsun.crescentcore.CrescentCore.PLUGIN_DATA_REGISTRY;

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
        crescentCore.getPlayerDataManager().asyncLoadData(player.getUniqueId()).thenAcceptAsync(pData -> {
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
        /*if (isServerLobby) {
            player.teleport(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation());
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }*/
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS, 50, 0, false, false));
    }

    @EventHandler
    public void onPlayerRegisterChannelEvent(PlayerRegisterChannelEvent event) {
        if (event.getChannel().equals("BungeeCord")) {
            BungeeUtils.sendGetServerMessage(event.getPlayer());
        }
    }

    /**
     * Saves the player data to the database upon quitting the server.
     * Additionally, it will trigger the {@link PlayerQuitEventPostDBSave} event.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (crescentCore.getPlayerDataManager().getData(player.getUniqueId()) == null) {
            return;
        }
        crescentCore.getPlayerDataManager().getData(player.getUniqueId())
                .updateDataValue(CrescentNamespaceKeys.PLAYER_LAST_SEEN, new Timestamp(System.currentTimeMillis()));
        crescentCore.getPlayerDataManager().asyncSaveData(player.getUniqueId()).thenAcceptAsync(playerData -> {
            if (playerData == null) {
                return;
            }
            PlayerQuitEventPostDBSave dataSavedEvent = new PlayerQuitEventPostDBSave(player, playerData);
            Bukkit.getScheduler().callSyncMethod(crescentCore, () -> {
                Bukkit.getPluginManager().callEvent(dataSavedEvent);
                return null;
            });
            crescentCore.getPlayerDataManager().removeData(player.getUniqueId());
        });
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        PlayerDataRegistry.freezeRegistries();
        PluginDataRegistry.freezeRegistries();
        crescentCore.getDatabaseManager().initTables();
        crescentCore.getDatabaseManager().initPluginDataManager();
        crescentCore.getDatabaseManager().initPlayerDataManager();
        crescentCore.getPluginDataManager().loadAllData();
        if (!Bukkit.getOnlinePlayers().isEmpty()) {
            CompletableFuture<Boolean> future = crescentCore.getPlayerDataManager().loadAllData();
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
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        JumpWarp jumpWarp = new JumpWarp(
                PLUGIN_DATA_REGISTRY.getDataRepository().getAllData(JumpWarp.class).size() + 1,
                event.getPlayer() + String.valueOf(crescentCore.getRandom().nextInt(20000)),
                event.getPlayer().getClientBrandName() + crescentCore.getRandom().nextInt(20000),
                UUID.randomUUID(),
                event.getPlayer().getLocation().getBlockX(),
                event.getPlayer().getLocation().getBlockY(),
                event.getPlayer().getLocation().getBlockZ(),
                event.getPlayer().locale().getCountry() + crescentCore.getRandom().nextInt(20000),
                UUID.randomUUID(),
                -event.getPlayer().getLocation().getBlockX(),
                -event.getPlayer().getLocation().getBlockY(),
                -event.getPlayer().getLocation().getBlockZ()
        );

        PLUGIN_DATA_REGISTRY.getDataRepository().addData(JumpWarp.class, jumpWarp.getId(), jumpWarp);
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

}
