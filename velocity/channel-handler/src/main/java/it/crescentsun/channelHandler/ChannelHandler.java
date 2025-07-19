package it.crescentsun.channelHandler;

import com.google.inject.Inject;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelRegistrar;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.mrnavastar.protoweaver.api.protocol.CompressionType;
import me.mrnavastar.protoweaver.api.protocol.Protocol;
import me.mrnavastar.protoweaver.core.util.ObjectSerializer;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

@Plugin(id = "channelhandler", name = "ChannelHandler", version = "1.0.1", dependencies = @Dependency(id = "protoweaver"))
public class ChannelHandler {

    public static final MinecraftChannelIdentifier IDENTIFIER_CRESCENTSUN_DB = MinecraftChannelIdentifier.create("crescentsun", "db");
    private Protocol forwarder = Protocol.create("crescentsun", "msg")
            .setClientHandler(ClientHandler.class)
            .setMaxPacketSize(67108864)
            .setCompression(CompressionType.GZIP)
            .addPacket(byte[].class)
            .load();
    private static ChannelHandler instance;
    private final ObjectSerializer objectSerializer = new ObjectSerializer();

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer proxyServer;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        objectSerializer.register(String.class);
        objectSerializer.register(List.class);
        ChannelRegistrar channelRegistrar = proxyServer.getChannelRegistrar();
        channelRegistrar.register(IDENTIFIER_CRESCENTSUN_DB);
        logger.info("Registered new plugin messaging channel: {}", IDENTIFIER_CRESCENTSUN_DB);
    }

    @Subscribe
    public void onPluginMessageFromPlayer(PluginMessageEvent event) {
        if (event.getIdentifier() != IDENTIFIER_CRESCENTSUN_DB) {
            return;
        }
        // ALWAYS set the result to handled to prevent it from being forwarded to the client or other unwanted servers.
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection serverConnection)) {
            return;
        }
        Collection<RegisteredServer> servers = proxyServer.getAllServers();
        for (RegisteredServer server : servers) {
            if (server.equals(serverConnection.getServer())) {
                continue;
            }
            server.sendPluginMessage(IDENTIFIER_CRESCENTSUN_DB, event.getData());
        }
    }

    public Protocol getForwarderProtocol() {
        return forwarder;
    }

    public Logger getLogger() {
        return logger;
    }

    public static ChannelHandler getInstance() {
        return instance;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    public ObjectSerializer getObjectSerializer() {
        return objectSerializer;
    }

}