package it.crescentsun.crescentcore.core.network;

import it.crescentsun.crescentcore.CrescentCore;
import it.crescentsun.crescentcore.api.data.plugin.PluginData;
import it.crescentsun.crescentcore.api.data.plugin.PluginDataIdentifier;
import it.crescentsun.crescentcore.api.event.server.ProtoweaverConnectionEstablishedEvent;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.util.ObjectSerializer;
import org.bukkit.NamespacedKey;

import java.util.List;


public class CrescentSunServerHandler implements ProtoConnectionHandler {

    private final CrescentCore crescentCore = CrescentCore.getInstance();

    @Override
    public void onReady(ProtoConnection connection) {
        crescentCore.setCrescentSunConnection(connection);
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
        ProtoConnectionHandler.super.onDisconnect(connection);
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        ObjectSerializer objectSerializer = CrescentCore.PLUGIN_DATA_REGISTRY.getPluginDataSerializer();
        if (packet instanceof byte[] bytes) {
            Object obj = objectSerializer.deserialize(bytes);
            switch (obj) {
                case PluginData pluginData -> {
                    crescentCore.getPluginDataRepository().addDataInstance(pluginData.getClass(), pluginData.getUuid(), pluginData);
                    crescentCore.getLogger().info("Received plugin data: " + pluginData.getClass().getSimpleName() + " with UUID: "
                            + pluginData.getUuid() + " from " + connection.getRemoteAddress());
                    if (pluginData.shouldInit()) {
                        pluginData.init();
                    }
                }

                case PluginDataIdentifier dataIdentifier -> {
                    crescentCore.getPluginDataRepository().removeData(dataIdentifier.classType(), dataIdentifier.uuid());
                    crescentCore.getLogger().info("Removed plugin data: " + dataIdentifier.classType().getSimpleName() + " with UUID: "
                            + dataIdentifier.uuid() + " due to request from " + connection.getRemoteAddress());
                }

                case String string -> {
                    NamespacedKey namespacedKey = NamespacedKey.fromString(string);
                    if (namespacedKey == null) {
                        return;
                    }
                    if (!namespacedKey.getNamespace().equalsIgnoreCase("server_name")) {
                        return;
                    }
                    crescentCore.setServerName(namespacedKey.getKey());
                    crescentCore.getServer().getPluginManager().callEvent(new ProtoweaverConnectionEstablishedEvent());
                }

                case List<?> list -> {
                    // Check if the contents of list are strings
                    if (list.isEmpty() || !(list.getFirst() instanceof String)) {
                        return;
                    }
                    // Check that all strings start with server_name:
                    for (Object o : list) {
                        if (!(o instanceof String s) || !s.startsWith("server_name:")) {
                            return;
                        }
                    }
                    List<String> serverList = list.stream()
                                                  .filter(String.class::isInstance)
                                                  .map(String.class::cast) //Split server_name: out
                                                  .map(s -> s.substring(11))
                                                  .toList();
                    crescentCore.setServerList(serverList);
                }
                default -> throw new IllegalStateException("Received unexpected value from Velocity: " + obj);
            }
        }
    }
}
