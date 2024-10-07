package it.crescentsun.api.crescentcore.sound;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * Represents a sound effect that can be played for a player or at a location.
 */
public class SoundEffect {

    protected final CrescentPlugin plugin;
    private final Sound sound;
    private final SoundCategory soundCategory;
    private final float volume;
    private final float pitch;

    public SoundEffect(CrescentPlugin plugin, Sound sound, SoundCategory soundCategory, float volume, float pitch) {
        this.plugin = plugin;
        this.sound = sound;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playForPlayerAtLocation(Player player) {
        player.playSound(player.getLocation(), sound, soundCategory, volume, pitch);
    }

    public void playAtLocation(Location location) {
        location.getWorld().playSound(location, sound, soundCategory, volume, pitch);
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

}
