package it.crescentsun.channelHandler;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.proxy.api.ProtoProxy;
import me.mrnavastar.protoweaver.proxy.api.ProtoServer;

import java.util.List;
import java.util.Optional;

public class ClientHandler implements ProtoConnectionHandler {

    private final ChannelHandler channelHandler = ChannelHandler.getInstance();

    @Override
    public void onReady(ProtoConnection connection) {
        String serverNamespace = "server_name:";
        sendAllServerNames(connection, serverNamespace);
        sendServerName(connection, serverNamespace);
    }

    private void sendAllServerNames(ProtoConnection connection, String serverNamespace) {
        List<String> serverList = channelHandler.getProxyServer().getAllServers().stream().map(RegisteredServer::getServerInfo).map(serverInfo -> {
            String hostString = serverInfo.getAddress().getHostString();
            return serverNamespace + hostString;
        }).toList();
        byte[] bytes = channelHandler.getObjectSerializer().serialize(serverList);
        connection.send(bytes);
    }

    private void sendServerName(ProtoConnection connection, String serverNamespace) {
        String serverName = connection.getRemoteAddress().getHostString();
        serverNamespace += serverName;
        byte[] bytes = channelHandler.getObjectSerializer().serialize(serverNamespace);
        connection.send(bytes);
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        // Packet filtering recognition is done on backend
        for (ProtoServer server : ProtoProxy.getConnectedServers(ChannelHandler.getInstance().getForwarderProtocol())) {
            Optional<ProtoConnection> optionalConnection = server.getConnection(connection.getProtocol());
            if (optionalConnection.isEmpty() || optionalConnection.get().getRemoteAddress().getHostString().equals(connection.getRemoteAddress().getHostString())) {
                continue;
            }
            if (packet instanceof byte[] bytes) {
                optionalConnection.get().send(bytes);
            } else {
                throw new IllegalStateException("Unexpected packet: " + packet);
            }
        }
    }

    /*@Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        // Packet filtering recognition is done on backend
        for (ProtoServer server : ProtoProxy.getConnectedServers(ChannelHandler.getInstance().getForwarderProtocol())) {
            if (server.getConnection().getRemoteAddress().getHostString().equals(connection.getRemoteAddress().getHostString())) {
                continue;
            }
            if (packet instanceof byte[] bytes) {
                server.getConnection().send(bytes);
            } else {
                throw new IllegalStateException("Unexpected packet: " + packet);
            }
        }
    }*/
}
