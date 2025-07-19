package it.crescentsun.mobmadness.data.entity;

import it.crescentsun.mobmadness.MobMadness;
import it.crescentsun.mobmadness.data.Game;
import it.crescentsun.mobmadness.data.entity.aura.Aura;
import it.crescentsun.mobmadness.data.entity.aura.AuraType;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a stronger beast which displays a {@link BossBar} once spawned.
 *
 * @param <T> what mob the boss will be.
 */
public abstract class BossBeast<T extends Mob> extends Beast<T> {

    public static final String MM_BOSS_TAG = "mm.boss";

    protected BossBar bossBar;
    protected int playerCount;

    protected final Map<EntityDamageEvent.DamageCause, Double> dmgCauseModifiers;
    protected final Map<AuraType, Double> auraModifiers;
    protected final List<PotionEffectType> potionImmunityList;

    public BossBeast(MobMadness plugin, T mob, BeastType type, Game game, Location spawnLoc,
                     Map<AuraType, Double> auraModifiers, Map<EntityDamageEvent.DamageCause, Double> damageMultipliers, List<PotionEffectType> potionImmunityList) {
        super(plugin, mob, type, game, spawnLoc);
        this.dmgCauseModifiers = damageMultipliers;
        this.auraModifiers = auraModifiers;
        this.potionImmunityList = potionImmunityList;
        playerCount = game.getSurvivors().size();
    }

    /**
     * See {@link Beast}
     */
    @Override
    protected void setup() {
        super.setup();
        mob.setMetadata(MM_BOSS_TAG, new FixedMetadataValue(plugin, game.getTier().getNumericTier()));
    }

    @Override
    public boolean handleDeath() {
        return true;
    }

    /**
     * Method used to apply modifiers to the damage taken by the boss beast.
     * Uses the values in {@link #dmgCauseModifiers} to alter the final damage applied to the beast.
     *
     * @param event a reference of a {@link EntityDamageEvent} event.
     */
    public void handleDamage(EntityDamageEvent event) {
        if (dmgCauseModifiers.containsKey(event.getCause())) {
            if (dmgCauseModifiers.get(event.getCause()) == 0) {
                event.setCancelled(true);
            }
            event.setDamage(event.getDamage() * dmgCauseModifiers.get(event.getCause()));
        }
    }

    /**
     * Method used to apply modifiers on a specific soulstate to the boss beast.
     * When
     *
     * @param aura the soulstate to apply the modifier on
     * @return the updated soulstate
     */
    public Aura handleSoulState(Aura aura) {
        if (auraModifiers.containsKey(aura.getType())) {
            aura.setDuration((int) (aura.getDuration() * auraModifiers.get(aura.getType())));
            if (auraModifiers.get(aura.getType()) < 0.33 && aura.getLevel() > 1) {
                aura.setLevel(aura.getLevel() - 1);
            }
        }
        return aura;
    }

    /**
     * Method used to remove any potion effect which the beast is immune to.
     */
    public void handlePotionEffects() {
        Collection<PotionEffect> potionEffects = mob.getActivePotionEffects().stream().dropWhile(
                potionEffect -> potionImmunityList.contains(potionEffect.getType())).collect(Collectors.toList());
        mob.addPotionEffects(potionEffects);
        for (PotionEffectType potionType : potionImmunityList) {
            if (mob.hasPotionEffect(potionType)) {
                mob.removePotionEffect(potionType);
            }
        }
    }
}