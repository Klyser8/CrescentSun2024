package it.crescentsun.crescentcore.core.protoweaver;

import it.crescentsun.crescentcore.CrescentCore;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;
import me.mrnavastar.protoweaver.core.util.ObjectSerializer;

import java.net.InetSocketAddress;

public class CrescentSunServerHandler implements ProtoConnectionHandler {

    private final CrescentCore crescentCore = CrescentCore.getInstance();

    @Override
    public void onReady(ProtoConnection connection) {
        crescentCore.getLogger().info("CrescentSun Server connection established!");
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {
        System.out.println("Received packet from velocity!");
        InetSocketAddress remoteAddress = connection.getRemoteAddress();
        System.out.println("Remote address: " + remoteAddress);
    }
}