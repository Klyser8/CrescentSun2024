package it.crescentsun.mobmadness.data.entity;

import it.crescentsun.mobmadness.MobMadness;
import it.crescentsun.mobmadness.data.Game;
import org.bukkit.Location;
import org.bukkit.entity.Mob;

/**
 * Represents a beast in a mob madness game.
 * @param <T> the type of mob that this beast represents
 */
public abstract class Beast<T extends Mob> implements AuraHolder {

    protected final T mob;
    protected final BeastType beastType;
    protected final Game game;
    protected final Location spawnLocation;
    protected final MobMadness plugin;

    public Beast(MobMadness plugin, T mob, BeastType beastType, Game game, Location spawnLocation) {
        this.mob = mob;
        this.beastType = beastType;
        this.game = game;
        this.spawnLocation = spawnLocation;
        this.plugin = plugin;
    }

    /**
     * Gets the mob associated with this beast.
     * @return the mob
     */
    public T getMob() {
        return mob;
    }

    /**
     * Any logic that needs to be run when the beast is killed should be implemented here.
     * @return true if the logic was successfully handled, false otherwise. The beast will be killed either way.
     */
    public abstract boolean handleDeath(); //TODO: Basic implementation

    /**
     * Removes the beast from the game.
     * Unlike a normal death, the entity won't play a death animation or drop items. Override to apply additional logic.
     * @return true if the mob is removed without issues, false otherwise. The beast gets removed either way.
     */
    public boolean remove() {
        mob.remove();
        //TODO: Entities should be removed from game data too
        return true;
    }

    protected void setup() {
        //TODO: Basic implementation
    }
}
