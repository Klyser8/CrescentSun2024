package it.crescentsun.crescentcore.api;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * Represents a sound effect that can be played for a player or at a location.
 */
public class SoundEffect {

    private final Sound sound;
    private final SoundCategory soundCategory;
    private final float volume;
    private final float pitch;

    public SoundEffect(Sound sound, SoundCategory soundCategory, float volume, float pitch) {
        this.sound = sound;
        this.soundCategory = soundCategory;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playSoundForPlayerAtLocation(Player player) {
        player.playSound(player.getLocation(), sound, soundCategory, volume, pitch);
    }

    public void playSoundAtLocation(Location location) {
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
