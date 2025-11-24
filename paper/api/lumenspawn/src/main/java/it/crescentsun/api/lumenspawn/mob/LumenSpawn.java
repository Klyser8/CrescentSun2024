package it.crescentsun.api.lumenspawn.mob;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public abstract class LumenSpawn<E extends Entity> {

    private final E entity;
    private final CrescentPlugin plugin;

    public LumenSpawn(CrescentPlugin plugin, E entity) {
        this.plugin = plugin;
        this.entity = entity;
    }

    public E getEntity() {
        return entity;
    }

    public boolean interactRight() {
        return false;
    }

    public boolean interactLeft() {
        return false;
    }

    public boolean interactShiftRight() {
        return false;
    }

    public boolean interactShiftLeft() {
        return false;
    }

    public E spawnEntity(Location spawnLocation) {
        World world = spawnLocation.getWorld();
        if (world == null) {
            plugin.getLogger().warning("World is null at location: " + spawnLocation);
            return null;
        }
        return (E) world.spawnEntity(spawnLocation, entity.getType());
    }
}
