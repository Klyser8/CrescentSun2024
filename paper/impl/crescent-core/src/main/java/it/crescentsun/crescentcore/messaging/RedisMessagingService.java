package it.crescentsun.crescentcore.messaging;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/** Redis Pub/Sub transport for notifications about SQL-backed plugin data. */
public final class RedisMessagingService implements MessagingService {
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> publisher;
    private final StatefulRedisPubSubConnection<String, String> subscriber;
    private final String channel;
    private final JavaPlugin plugin;
    private static final long HEARTBEAT_TTL_MILLIS = 30_000;
    private final String onlineServersKey;

    public RedisMessagingService(JavaPlugin plugin, String host, int port, String password, int database,
                                 String channel, String onlineServersKey) {
        this.plugin = plugin;
        this.channel = channel;
        this.onlineServersKey = onlineServersKey;

        RedisURI.Builder uri = RedisURI.Builder.redis(host, port).withDatabase(database);
        if (password != null && !password.isBlank()) {
            uri.withPassword(password.toCharArray());
        }

        client = RedisClient.create(uri.build());
        publisher = client.connect();
        subscriber = client.connectPubSub();
    }

    @Override
    public CompletionStage<Void> publish(PluginDataChange change) {
        String payload = encode(change);
        return publisher.async().publish(channel, payload).thenApply(ignored -> null);
    }

    @Override
    public CompletionStage<Set<String>> updateServerHeartbeat(String serverName) {
        long now = System.currentTimeMillis();
        return publisher.async().zadd(onlineServersKey, now, serverName)
                .thenCompose(ignored -> publisher.async().zremrangebyscore(
                        onlineServersKey, Double.NEGATIVE_INFINITY, now - HEARTBEAT_TTL_MILLIS))
                .thenCompose(ignored -> publisher.async().zrange(onlineServersKey, 0, -1))
                .thenApply(Set::copyOf);
    }

    @Override
    public CompletionStage<Set<String>> getOnlineServers() {
        long now = System.currentTimeMillis();
        return publisher.async().zremrangebyscore(onlineServersKey, Double.NEGATIVE_INFINITY,
                        now - HEARTBEAT_TTL_MILLIS)
                .thenCompose(ignored -> publisher.async().zrange(onlineServersKey, 0, -1))
                .thenApply(Set::copyOf);
    }

    @Override
    public void subscribe(Consumer<PluginDataChange> listener) {
        subscriber.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String receivedChannel, String message) {
                if (!channel.equals(receivedChannel)) return;
                try {
                    listener.accept(decode(message));
                } catch (RuntimeException exception) {
                    plugin.getLogger().warning("Ignoring invalid plugin-data message from Redis: " + exception.getMessage());
                }
            }
        });
        subscriber.sync().subscribe(channel);
    }

    @Override
    public void close() {
        subscriber.close();
        publisher.close();
        client.shutdown();
    }

    private static String encode(PluginDataChange change) {
        String typeId = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(change.typeId().getBytes(StandardCharsets.UTF_8));
        return "1|" + change.operation().name() + "|" + typeId + "|" + change.recordId();
    }

    private static PluginDataChange decode(String payload) {
        String[] parts = payload.split("\\|", -1);
        if (parts.length != 4 || !"1".equals(parts[0])) {
            throw new IllegalArgumentException("unsupported message format");
        }
        PluginDataChange.Operation operation = PluginDataChange.Operation.valueOf(parts[1]);
        String typeId = new String(Base64.getUrlDecoder().decode(parts[2]), StandardCharsets.UTF_8);
        UUID recordId = UUID.fromString(parts[3]);
        return new PluginDataChange(operation, typeId, recordId);
    }
}
