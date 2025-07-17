package it.crescentsun.api.crescentcore.sound;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a composite sound effect that is composed of multiple sound effects.
 *
 * @see SoundEffect
 * @see TimedSoundEffect
 */
public class CompositeSoundEffect {

    private final List<SoundEffect> soundEffects = new ArrayList<>();

    public CompositeSoundEffect(SoundEffect... soundEffects) {
        this.soundEffects.addAll(List.of(soundEffects));
    }

    public void playForPlayerAtLocation(Player player) {
        for (SoundEffect soundEffect : soundEffects) {
            soundEffect.playForPlayerAtLocation(player);
        }
    }

    public void playAtLocation(Location location) {
        for (SoundEffect soundEffect : soundEffects) {
            soundEffect.playAtLocation(location);
        }
    }

}
