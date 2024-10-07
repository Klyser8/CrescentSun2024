package it.crescentsun.crystals.sound;

import it.crescentsun.api.crescentcore.CrescentPlugin;
import it.crescentsun.api.crescentcore.sound.CompositeSoundEffect;
import it.crescentsun.api.crescentcore.sound.SoundEffect;
import it.crescentsun.api.crescentcore.sound.TimedSoundEffect;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class CrystalsSFX {

    public final SoundEffect crystalAppear;
    public final CompositeSoundEffect crystalPickUp;
    public final SoundEffect crystalHover;
    public final CompositeSoundEffect circlingExplosion;

    public CrystalsSFX(CrescentPlugin plugin) {
        crystalAppear = new SoundEffect(plugin, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.75f, 2.0f);
        crystalPickUp = new CompositeSoundEffect(
                new TimedSoundEffect(plugin, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.189f, 0),
                new TimedSoundEffect(plugin, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.414f, 1),
                new TimedSoundEffect(plugin, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.888f, 2),
                new TimedSoundEffect(plugin, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.587f, 3)
        );
        crystalHover = new SoundEffect(plugin, Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.PLAYERS, 1.5f, 1.5f);
        circlingExplosion = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.5f, 1.5f),
                new SoundEffect(plugin, Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 0.4f, 0.75f),
                new SoundEffect(plugin, Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 0.4f, 0.5f)
        );
    }
}
