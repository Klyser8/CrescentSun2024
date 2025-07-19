package it.crescentsun.mobmadness.data.entity.aura;

import it.crescentsun.mobmadness.MobMadness;
import it.crescentsun.mobmadness.data.Game;
import it.crescentsun.mobmadness.data.entity.AuraHolder;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * An aura is a status effect applied to an entity.
 * These are applied to monsters usually, but also to players if the subclasses are
 * written correctly.
 */
public abstract class Aura {

    protected final MobMadness plugin;
    protected final Game game;
    protected final AuraType type;
    protected final AuraHolder auraHolder;
    protected int level;
    protected int duration; //in ticks
    protected int currentDuration; //in ticks

    protected final boolean applied;

    protected Aura(MobMadness plugin, Game game, AuraType type, AuraHolder auraHolder,
                   int level, int duration) {
        this.plugin = plugin;
        this.game = game;
        this.type = type;
        this.auraHolder = auraHolder;
        this.level = level;
        this.duration = duration;
        this.currentDuration = duration;
        //Checks whether the entity already has a soul state of this type. If the entity does, the soul state is stopped
        //And replaced with this new instance (only if this new one is judged to be stronger).
        if (!auraHolder.getCurrentAuras().containsKey(type)
                || auraHolder.getCurrentAuras().get(type).level < level
                || auraHolder.getCurrentAuras().get(type).getCurrentDuration() < currentDuration) {
            if (auraHolder.getCurrentAuras().containsKey(type)) {
                auraHolder.getCurrentAuras().get(type).endSoulState();
            }
            auraHolder.getCurrentAuras().put(type, this);
            applied = true;
        } else {
            applied = false;
        }
    }

    /**
     * This is what runs the SoulState's code every tick.
     * However, this will start only if the soul state was applied correctly.
     */
    protected void runAura(int particleInterval, int soundInterval) {
        startAura();
        new BukkitRunnable() {
            @Override
            public void run() {
                applyAuraLoop();
                if (particleInterval > 0 && currentDuration % particleInterval == 0) {
                    playAuraParticles();
                }
                if (soundInterval > 0 && currentDuration % soundInterval == 0) {
                    playAuraSounds();
                }
                currentDuration--;
                if (currentDuration == 0 || auraHolder.getLivingEntity().isDead()) {
                    cancel();
                    endSoulState();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    /**
     * Logic to start the soul state. Only runs once, at the beginning.
     */
    protected abstract void startAura();

    /**
     * Logic to run the soul state itself. This is where the code dictating the soul state's behavior is written.
     */
    protected abstract void applyAuraLoop();

    /**
     * Logic which plays particle effects for the soul state
     */
    protected abstract void playAuraParticles();

    /**
     * Logic which plays sound effects for the soul state
     */
    protected abstract void playAuraSounds();

    public void endSoulState() {
        auraHolder.getCurrentAuras().remove(getType());
    }

    public AuraType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
        if (currentDuration > this.duration) {
            currentDuration = this.duration;
        }
    }

    public int getCurrentDuration() {
        return currentDuration;
    }
}