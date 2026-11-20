package it.crescentsun.crescentcore.messaging;

import java.util.concurrent.CompletionStage;
import java.util.Set;
import java.util.function.Consumer;

public interface MessagingService extends AutoCloseable {

    CompletionStage<Void> publish(PluginDataChange change);

    CompletionStage<Set<String>> updateServerHeartbeat(String serverName);

    CompletionStage<Set<String>> getOnlineServers();

    void subscribe(Consumer<PluginDataChange> listener);

    @Override
    void close();

}
