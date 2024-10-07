package it.crescentsun.api.crescentcore.sound;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * Represents a {@link SoundEffect}, that is played after the specified delay.
 */
public class TimedSoundEffect extends SoundEffect {

    private final int delayInTicks;

    public TimedSoundEffect(CrescentPlugin plugin, Sound sound, SoundCategory soundCategory, float volume, float pitch, int delayInTicks) {
        super(plugin, sound, soundCategory, volume, pitch);
        this.delayInTicks = delayInTicks;
    }

    @Override
    public void playForPlayerAtLocation(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> super.playForPlayerAtLocation(player), getDelayInTicks());
    }

    @Override
    public void playAtLocation(Location location) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> super.playAtLocation(location), getDelayInTicks());
    }

    public int getDelayInTicks() {
        return delayInTicks;
    }
}
