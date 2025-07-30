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
    public final CompositeSoundEffect vaultCreate;
    public final CompositeSoundEffect vaultBreak;
    public final CompositeSoundEffect vaultOpen;
    public final CompositeSoundEffect vaultClose;

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
        vaultCreate = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.ENTITY_BREEZE_CHARGE, SoundCategory.BLOCKS, 1, 0.5f),
                new TimedSoundEffect(plugin, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1, 2, 16),
                new TimedSoundEffect(plugin, Sound.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 1, 0.75f, 16)
        );
        vaultBreak = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1, 0.75f),
                new SoundEffect(plugin, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 0.75f, 2),
                new SoundEffect(plugin, Sound.BLOCK_CONDUIT_DEACTIVATE, SoundCategory.BLOCKS, 1, 2.0f)
        );
        vaultOpen = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0f, 1.5f),
                new TimedSoundEffect(plugin, Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1, 1.5f, 2)
        );
        vaultClose = new CompositeSoundEffect(
                new SoundEffect(plugin, Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.66f),
                new TimedSoundEffect(plugin, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1, 1.2f, 2)
        );
    }
}
