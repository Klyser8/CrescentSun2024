package it.crescentsun.crescentcore.core.protoweaver;

import it.crescentsun.crescentcore.CrescentCore;
import me.mrnavastar.protoweaver.api.ProtoConnectionHandler;
import me.mrnavastar.protoweaver.api.netty.ProtoConnection;

public class CrescentSunClientHandler implements ProtoConnectionHandler {

    private final CrescentCore crescentCore = CrescentCore.getInstance();

    @Override
    public void onReady(ProtoConnection connection) {
        crescentCore.getLogger().info("CrescentSun Client connection established!");
        crescentCore.setCrescentSunConnection(connection); //TODO: test this. look at the authors DMs on discord.
    }

    @Override
    public void onDisconnect(ProtoConnection connection) {
        ProtoConnectionHandler.super.onDisconnect(connection);
    }

    @Override
    public void handlePacket(ProtoConnection connection, Object packet) {

    }
}
