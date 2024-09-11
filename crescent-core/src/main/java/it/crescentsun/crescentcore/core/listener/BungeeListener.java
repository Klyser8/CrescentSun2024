package it.crescentsun.crescentcore.core.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataRepository;
import it.crescentsun.crescentcore.core.network.BungeeConstants;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BungeeListener implements PluginMessageListener {

    private final CrescentCore crescentCore;

    public BungeeListener(CrescentCore crescentCore) {
        this.crescentCore = crescentCore;
    }


    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (channel.equals(BungeeConstants.MESSAGING_CHANNEL_BUNGEE)) {
            if (subChannel.equals("GetServer")) {
                handleGetServer(in);
            }
        } else if (channel.equals(BungeeConstants.MESSAGING_CHANNEL_CRESCENTSUN_DB)) {
            if (subChannel.equals(BungeeConstants.SUBCHANNEL_PLUGIN_DATA_SYNC_LOAD)) {
                String contents = in.readUTF();
                String tableName = contents.split(":")[0];
                String uuidString = contents.split(":")[1];
                CompletableFuture<Boolean> booleanCompletableFuture = crescentCore.getPluginDBManager()
                        .asyncLoadData(PluginDataRepository.getPluginDataClassFromFullTableName(tableName), UUID.fromString(uuidString));
                booleanCompletableFuture.thenAcceptAsync(success -> {
                    if (success) {
                        crescentCore.getLogger().info("- [SYNC] Loaded data from database: " + tableName + " with primary key: " + uuidString);
                    } else {
                        crescentCore.getLogger().warning("- [SYNC] Failed to load data from database: " + tableName + " with primary key: " + uuidString);
                    }
                });
            } else if (subChannel.equals(BungeeConstants.SUBCHANNEL_PLUGIN_DATA_SYNC_DELETE)) {
                String contents = in.readUTF();
                String tableName = contents.split(":")[0];
                String uuidString = contents.split(":")[1];
                Class<? extends PluginData> dataClass = PluginDataRepository.getPluginDataClassFromFullTableName(tableName);
                PluginData pluginData = crescentCore.getPluginDataRepository().removeData(dataClass, UUID.fromString(uuidString));
                if (pluginData != null) {
                    crescentCore.getLogger().info("- [SYNC] Deleted data from repository: " + tableName + " with primary key: " + uuidString);
                } else {
                    crescentCore.getLogger().warning("- [SYNC] Failed to delete data from repository: " + tableName + " with primary key: " + uuidString);
                }
            }
        }
    }

    private void handleGetServer(ByteArrayDataInput in) {
        String serverNameUTF = in.readUTF();
        CompletableFuture<String> futureServerName = CompletableFuture.completedFuture(serverNameUTF);
        futureServerName.thenAcceptAsync(crescentCore::setServerName);
    }

}
