package it.crescentsun.crescentcore.api.data.plugin;

import java.util.UUID;

public record PluginDataIdentifier(Class<? extends PluginData> classType, UUID uuid) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginDataIdentifier that = (PluginDataIdentifier) o;
        return classType.equals(that.classType) && uuid.equals(that.uuid);
    }

}