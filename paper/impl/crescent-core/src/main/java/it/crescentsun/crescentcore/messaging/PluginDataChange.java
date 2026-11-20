package it.crescentsun.crescentcore.messaging;

import java.util.UUID;

public record PluginDataChange(
        Operation operation,
        String typeId,
        UUID recordId
) {
    public enum Operation {
        UPSERT,
        DELETE
    }
}